import State.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.random.Random

@ExperimentalCoroutinesApi
fun main() {
    val scope = CoroutineScope(Job())
    val fsm = FiniteStateMachine(scope)
    val job = scope.launch {
        // Run for a limited time
        println(withTimeoutOrNull(5000L) {
            fsm.states.collect { state ->
                println("Collected state: $state")

                if (state !is Success) {
                    // Simulate slow main loop
                    delay(Random.nextLong(from = 100L, until = 1000L))
                }

                when (state) {
                    is Idle -> state.fetch()
                    is Loading -> {
                        if (Random.nextLong(from = 0L, until = 3L) == 2L) {
                            state.resolve()
                        } else {
                            state.reject()
                        }
                    }
                    is Failure -> state.retry()
                    is Success -> {
                        println("🎉 Closing state machine")
                        fsm.close()
                    }
                }
            }
            "🙂"
        } ?: "🙁 Timed out!")
    }

    runBlocking { job.join() }
    fsm.close()
    scope.cancel()
}

private fun println(message: String) {
    kotlin.io.println("[Main] $message")
}
