sealed class State {
    @Suppress("CanSealedSubClassBeObject")
    class Idle internal constructor(): State() {
        override fun toString() = "Idle"
    }

    class Loading internal constructor(val retries: Int = 0) : State() {
        override fun toString() = "Loading"
    }

    class Failure internal constructor(val retries: Int) : State() {
        override fun toString() = "Failure"
    }

    class Success internal constructor(val retries: Int) : State() {
        override fun toString() = "Success"
    }
}
