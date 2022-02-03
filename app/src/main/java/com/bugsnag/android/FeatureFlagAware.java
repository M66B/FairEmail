package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

interface FeatureFlagAware {
    /**
     * Add a single feature flag with no variant. If there is an existing feature flag with the
     * same name, it will be overwritten to have no variant.
     *
     * @param name the name of the feature flag to add
     * @see #addFeatureFlag(String, String)
     */
    void addFeatureFlag(@NonNull String name);

    /**
     * Add a single feature flag with an optional variant. If there is an existing feature
     * flag with the same name, it will be overwritten with the new variant. If the variant is
     * {@code null} this method has the same behaviour as {@link #addFeatureFlag(String)}.
     *
     * @param name    the name of the feature flag to add
     * @param variant the variant to set the feature flag to, or {@code null} to specify a feature
     *                flag with no variant
     */
    void addFeatureFlag(@NonNull String name, @Nullable String variant);

    /**
     * Add a collection of feature flags. This method behaves exactly the same as calling
     * {@link #addFeatureFlag(String, String)} for each of the {@code FeatureFlag} objects.
     *
     * @param featureFlags the feature flags to add
     * @see #addFeatureFlag(String, String)
     */
    void addFeatureFlags(@NonNull Iterable<FeatureFlag> featureFlags);

    /**
     * Remove a single feature flag regardless of its current status. This will stop the specified
     * feature flag from being reported. If the named feature flag does not exist this will
     * have no effect.
     *
     * @param name the name of the feature flag to remove
     */
    void clearFeatureFlag(@NonNull String name);

    /**
     * Clear all of the feature flags. This will stop all feature flags from being reported.
     */
    void clearFeatureFlags();
}