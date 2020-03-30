import State.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    val scope = CoroutineScope(Job())
    val jobs = mutableListOf<Job>()
    val fsm = FiniteStateMachine()

    jobs += scope.launch {
        // Run for a limited time
        val result = withTimeoutOrNull(6000L) {
            println("Starting state flow collector")
            lateinit var collector: Job

            val action: suspend (State) -> Unit = { state ->
                println("Collected state $state")

                if (state !is Success) {
                    println("Processing $state...")
                    // Simulate slow main loop
                    delay(Random.nextLong(from = 100L, until = 1000L))
                }

                @Suppress("UNUSED_VARIABLE")
                val exhaustive = when (state) {
                    is Idle -> {
                        println("Fetching")
                        state.fetch()
                    }
                    is Loading -> {
                        if (Random.nextLong(from = 0L, until = 3L) == 2L) {
                            println("Resolving")
                            state.resolve()
                        } else {
                            println("Rejecting")
                            state.reject()
                        }
                    }
                    is Failure -> {
                        println("Retrying, retries before now = ${state.retries}")
                        state.retry()
                    }
                    is Success -> {
                        println("🎉 Cancelling collector")
                        collector.cancel()
                    }
                }
            }

            collector = fsm.states.onEach(action).launchIn(this)

            "🙂"
        }
        println(result ?: "🙁 Timed out!")
        fsm.close()
    }

    // Start another state collector after a while
    runBlocking { delay(2000) }
    jobs += fsm
        .states
        .onStart { println("Starting another state flow collector") }
        .onEach { println("Another collector collected $it") }
        .launchIn(scope)

    runBlocking { jobs.joinAll() }
}

private fun println(message: Any?) {
    kotlin.io.println("[Main] $message")
}
