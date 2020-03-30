import State.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FiniteStateMachine {
    private val broadcast = BroadcastChannel<State>(Channel.CONFLATED)

    init {
        broadcast.offer(Idle(this@FiniteStateMachine))
    }

    /**
     * Collect new states from this flow.
     * This is public for any observer.
     */
    val states = broadcast
        .asFlow()
        .onStart { println("Flow started") }
        .onEach { state ->
            println("Received state $state")
            state.handle()
        }
        .onCompletion { println("Flow completed") }

    fun close() {
        broadcast.close()
        println("Closed")
    }

    internal fun setState(state: State) {
        broadcast.offer(state)
    }

    /**
     * Side effects for state go here.
     */
    private suspend fun State.handle() {
        println("Handling...")
        // Simulate a slow state machine
        delay(Random.nextLong(from = 100L, until = 1000L))
        val exhaustive = when (this) {
            is Idle -> println("Please fetch")
            is Loading -> println("Please reject or resolve")
            is Failure -> println("Please retry (retries = $retries)")
            is Success -> println("Did $retries retries")
        }
    }

    private fun println(message: Any?) {
        kotlin.io.println("[FSM] $message")
    }
}
