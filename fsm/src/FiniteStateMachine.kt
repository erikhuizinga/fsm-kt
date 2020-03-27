import State.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.random.Random

@ExperimentalCoroutinesApi
class FiniteStateMachine(private val coroutineScope: CoroutineScope) {
    /**
     * The internal state
     */
    private var retries: Int = 0

    private val stateChannel: Channel<State> = Channel(Channel.BUFFERED)

    val states: Flow<State> = channelFlow {
        for (state in this@FiniteStateMachine.stateChannel) {
            println("Processing...")
            delay(Random.nextLong(from = 100L, until = 1000L)) // Simulate a slow state machine
            println("Sending state")
            send(state)
        }
    }
        .onStart { println("Flow started") }
        .onCompletion { println("Flow completed") }

    private val initialState: State by lazy { Idle(fetch) }

    init {
        coroutineScope.launch { stateChannel.send(initialState) }
    }

    private val fetch: () -> Unit = {
        println("FETCH")
        onFetch()
    }

    private val resolve: () -> Unit = {
        println("RESOLVE")
        onResolve()
    }

    private val reject: () -> Unit = {
        println("REJECT")
        onReject()
    }

    private val retry: () -> Unit = {
        println("RETRY")
        onRetry()
    }

    fun close() {
        val wasClosed = stateChannel.close()
        if (!wasClosed) {
            println("Closed")
        }
    }

    private fun onFetch() {
        coroutineScope.launch { stateChannel.send(Loading(resolve, reject, retries)) }
    }

    private fun onResolve() {
        coroutineScope.launch { stateChannel.send(Success(retries)) }
    }

    private fun onReject() {
        coroutineScope.launch { stateChannel.send(Failure(retry)) }
    }

    private fun onRetry() {
        retries++ // Update internal state
        onFetch()
    }
}

sealed class State {
    class Idle internal constructor(val fetch: () -> Unit) : State() {
        override fun toString() = "Idle, please FETCH"
    }

    class Loading internal constructor(
        val resolve: () -> Unit,
        val reject: () -> Unit,
        private val retries: Int
    ) : State() {
        override fun toString() = "Loading(retries=$retries), please RESOLVE or REJECT"
    }

    class Failure internal constructor(val retry: () -> Unit) : State() {
        override fun toString() = "Failure, please RETRY"
    }

    class Success internal constructor(private val retries: Int) : State() {
        override fun toString() = "Success, you reached the final state after $retries retries!"
    }
}

private fun println(message: String) {
    kotlin.io.println("[FSM] $message")
}
