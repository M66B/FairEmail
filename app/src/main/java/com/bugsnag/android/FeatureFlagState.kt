package com.bugsnag.android

internal data class FeatureFlagState(
    val featureFlags: FeatureFlags = FeatureFlags()
) : BaseObservable(), FeatureFlagAware {
    override fun addFeatureFlag(name: String) {
        this.featureFlags.addFeatureFlag(name)
        updateState {
            StateEvent.AddFeatureFlag(name)
        }
    }

    override fun addFeatureFlag(name: String, variant: String?) {
        this.featureFlags.addFeatureFlag(name, variant)
        updateState {
            StateEvent.AddFeatureFlag(name, variant)
        }
    }

    override fun addFeatureFlags(featureFlags: Iterable<FeatureFlag>) {
        featureFlags.forEach { (name, variant) ->
            addFeatureFlag(name, variant)
        }
    }

    override fun clearFeatureFlag(name: String) {
        this.featureFlags.clearFeatureFlag(name)
        updateState {
            StateEvent.ClearFeatureFlag(name)
        }
    }

    override fun clearFeatureFlags() {
        this.featureFlags.clearFeatureFlags()
        updateState {
            StateEvent.ClearFeatureFlags
        }
    }

    fun emitObservableEvent() {
        val flags = toList()

        flags.forEach { (name, variant) ->
            updateState { StateEvent.AddFeatureFlag(name, variant) }
        }
    }

    fun toList(): List<FeatureFlag> = featureFlags.toList()
    fun copy() = FeatureFlagState(featureFlags.copy())
}
