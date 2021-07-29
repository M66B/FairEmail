package com.bugsnag.android

/**
 * Tracks the current context and allows observers to be notified whenever it changes.
 *
 * The default behaviour is to track [SessionTracker.getContextActivity]. However, any value
 * that the user sets via [Bugsnag.setContext] will override this and be returned instead.
 */
internal class ContextState : BaseObservable() {

    companion object {
        private const val MANUAL = "__BUGSNAG_MANUAL_CONTEXT__"
    }

    private var manualContext: String? = null
    private var automaticContext: String? = null

    fun setManualContext(context: String?) {
        manualContext = context
        automaticContext = MANUAL
        emitObservableEvent()
    }

    fun setAutomaticContext(context: String?) {
        if (automaticContext !== MANUAL) {
            automaticContext = context
            emitObservableEvent()
        }
    }

    fun getContext(): String? {
        return automaticContext.takeIf { it !== MANUAL } ?: manualContext
    }

    fun emitObservableEvent() = updateState { StateEvent.UpdateContext(getContext()) }
}
