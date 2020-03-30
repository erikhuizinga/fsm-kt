sealed class State(protected val fsm: FiniteStateMachine) {
    class Idle internal constructor(fsm: FiniteStateMachine) : State(fsm) {
        fun fetch() {
            fsm.setState(Loading(fsm))
        }

        override fun toString() = "Idle"
    }

    class Loading internal constructor(
        fsm: FiniteStateMachine,
        private val retries: Int = 0
    ) : State(fsm) {
        fun reject() {
            fsm.setState(Failure(fsm, retries))
        }

        fun resolve() {
            fsm.setState(Success(fsm, retries))
        }

        override fun toString() = "Loading"
    }

    class Failure internal constructor(
        fsm: FiniteStateMachine,
        val retries: Int
    ) : State(fsm) {
        fun retry() {
            fsm.setState(Loading(fsm, retries + 1))
        }

        override fun toString() = "Failure"
    }

    class Success internal constructor(fsm: FiniteStateMachine, val retries: Int) : State(fsm) {
        override fun toString() = "Success"
    }
}
