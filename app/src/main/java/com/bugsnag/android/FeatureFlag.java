package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Represents a single feature-flag / experiment marker within Bugsnag. Each {@code FeatureFlag}
 * object has a {@link #getName() name} and an optional {@link #getVariant() variant} which can be
 * used to identify runtime experiments and groups when reporting errors.
 *
 * @see Bugsnag#addFeatureFlag(String, String)
 * @see Event#addFeatureFlag(String, String)
 */
public final class FeatureFlag implements Map.Entry<String, String> {
    private final String name;

    private final String variant;

    /**
     * Create a named {@code FeatureFlag} with no variant
     *
     * @param name the identifying name of the new {@code FeatureFlag} (not {@code null})
     * @see Bugsnag#addFeatureFlag(String)
     * @see Event#addFeatureFlag(String)
     */
    public FeatureFlag(@NonNull String name) {
        this(name, null);
    }

    /**
     * Create a new {@code FeatureFlag} with a name and (optionally) a variant.
     *
     * @param name    the identifying name of the new {@code FeatureFlag} (not {@code null})
     * @param variant the feature variant
     */
    public FeatureFlag(@NonNull String name, @Nullable String variant) {
        if (name == null) {
            throw new NullPointerException("FeatureFlags cannot have null name");
        }

        this.name = name;
        this.variant = variant;
    }

    /**
     * Create a new {@code FeatureFlag} based on an existing {@code Map.Entry}. This is the same
     * as {@code new FeatureFlag(mapEntry.getKey(), mapEntry.getValue())}.
     *
     * @param mapEntry an existing {@code Map.Entry} to copy the feature flag from
     */
    public FeatureFlag(@NonNull Map.Entry<String, String> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getVariant() {
        return variant;
    }

    /**
     * Same as {@link #getName()}.
     *
     * @return the name of this {@code FeatureFlag}
     * @see #getName()
     */
    @NonNull
    @Override
    public String getKey() {
        return name;
    }

    /**
     * Same as {@link #getVariant()}.
     *
     * @return the variant of this {@code FeatureFlag} (may be {@code null})
     * @see #getVariant()
     */
    @Nullable
    @Override
    public String getValue() {
        return variant;
    }

    /**
     * Throws {@code UnsupportedOperationException} as {@code FeatureFlag} is considered immutable.
     *
     * @param value ignored
     * @return nothing
     */
    @Override
    @Nullable
    public String setValue(@Nullable String value) {
        throw new UnsupportedOperationException("FeatureFlag is immutable");
    }

    @Override
    public int hashCode() {
        // Follows the Map.Entry contract
        return getKey().hashCode() ^ (getValue() == null ? 0 : getValue().hashCode());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        // This follows the contract defined in Map.Entry exactly
        if (!(other instanceof Map.Entry)) {
            return false;
        }

        Map.Entry<? extends Object, ? extends Object> e2 =
                (Map.Entry<? extends Object, ? extends Object>) other;

        return getKey().equals(e2.getKey())
                && (getValue() == null ? e2.getValue() == null : getValue().equals(e2.getValue()));
    }

    @Override
    public String toString() {
        return "FeatureFlag{"
                + "name='" + name + '\''
                + ", variant='" + variant + '\''
                + '}';
    }
}
