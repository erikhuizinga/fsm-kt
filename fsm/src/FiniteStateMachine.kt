import State.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FiniteStateMachine {
    private val broadcast = BroadcastChannel<State>(Channel.CONFLATED)

    init {
        broadcast.offer(Idle(this@FiniteStateMachine))
    }

    private var isHandlingStates = false

    private val handler = broadcast
        .asFlow()
        .onStart { println("Starting state handler") }
        .onEach(::handleState)
        .onCompletion { println("Completed state handler") }

    /**
     * Collect new states from this flow.
     * This is public for any observer.
     */
    val states = broadcast
        .asFlow()
        .onStart {
            println("State flow started")
            if (!isHandlingStates) {
                isHandlingStates = true
                emitAll(handler)
            }
        }
        .onCompletion { println("State flow completed") }

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
    private suspend fun handleState(state: State) {
        println("Handling $state...")
        // Simulate a slow state machine
        delay(Random.nextLong(from = 100L, until = 1000L))
        @Suppress("UNUSED_VARIABLE")
        val exhaustive = when (state) {
            is Idle -> println("Please fetch")
            is Loading -> println("Please reject or resolve")
            is Failure -> println("Please retry (retries = ${state.retries})")
            is Success -> println("Did ${state.retries} retries")
        }
    }

    private fun println(message: Any?) {
        kotlin.io.println("[FSM] $message")
    }
}
