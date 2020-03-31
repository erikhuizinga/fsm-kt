sealed class State {
    @Suppress("CanSealedSubClassBeObject")
    class Idle internal constructor() : State() {
        override fun toString() = "Idle"
    }

    data class Loading internal constructor(val retries: Int = 0) : State()
    data class Failure internal constructor(val retries: Int) : State()
    data class Success internal constructor(val retries: Int) : State()
}
