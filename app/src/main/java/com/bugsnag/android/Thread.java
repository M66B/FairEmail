package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * A representation of a thread recorded in an {@link Event}
 */
@SuppressWarnings("ConstantConditions")
public class Thread implements JsonStream.Streamable {

    private final ThreadInternal impl;
    private final Logger logger;

    Thread(
            long id,
            @NonNull String name,
            @NonNull ThreadType type,
            boolean errorReportingThread,
            @NonNull Thread.State state,
            @NonNull Stacktrace stacktrace,
            @NonNull Logger logger) {
        this.impl = new ThreadInternal(
                id, name, type, errorReportingThread, state.getDescriptor(), stacktrace);
        this.logger = logger;
    }

    Thread(@NonNull ThreadInternal impl, @NonNull Logger logger) {
        this.impl = impl;
        this.logger = logger;
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to thread." + property + ", ignoring");
    }

    /**
     * Sets the unique ID of the thread (from {@link java.lang.Thread})
     */
    public void setId(long id) {
        impl.setId(id);
    }

    /**
     * Gets the unique ID of the thread (from {@link java.lang.Thread})
     */
    public long getId() {
        return impl.getId();
    }

    /**
     * Sets the name of the thread (from {@link java.lang.Thread})
     */
    public void setName(@NonNull String name) {
        if (name != null) {
            impl.setName(name);
        } else {
            logNull("name");
        }
    }

    /**
     * Gets the name of the thread (from {@link java.lang.Thread})
     */
    @NonNull
    public String getName() {
        return impl.getName();
    }

    /**
     * Sets the type of thread based on the originating platform (intended for internal use only)
     */
    public void setType(@NonNull ThreadType type) {
        if (type != null) {
            impl.setType(type);
        } else {
            logNull("type");
        }
    }

    /**
     * Gets the type of thread based on the originating platform (intended for internal use only)
     */
    @NonNull
    public ThreadType getType() {
        return impl.getType();
    }

    /**
     * Sets the state of thread (from {@link java.lang.Thread})
     */
    public void setState(@NonNull Thread.State threadState) {
        if (threadState != null) {
            impl.setState(threadState.getDescriptor());
        } else {
            logNull("state");
        }
    }

    /**
     * Gets the state of the thread (from {@link java.lang.Thread})
     */
    @NonNull
    public Thread.State getState() {
        return Thread.State.byDescriptor(impl.getState());
    }

    /**
     * Gets whether the thread was the thread that caused the event
     */
    public boolean getErrorReportingThread() {
        return impl.isErrorReportingThread();
    }

    /**
     * Sets a representation of the thread's stacktrace
     */
    public void setStacktrace(@NonNull List<Stackframe> stacktrace) {
        if (!CollectionUtils.containsNullElements(stacktrace)) {
            impl.setStacktrace(stacktrace);
        } else {
            logNull("stacktrace");
        }
    }

    /**
     * Gets a representation of the thread's stacktrace
     */
    @NonNull
    public List<Stackframe> getStacktrace() {
        return impl.getStacktrace();
    }

    @Override
    public void toStream(@NonNull JsonStream stream) throws IOException {
        impl.toStream(stream);
    }

    /**
     * The state of a reported {@link Thread}. These states correspond directly to
     * {@link java.lang.Thread.State}, except for {@code UNKNOWN} which indicates that
     * a state could not be captured or mapped.
     */
    public enum State {
        NEW("NEW"),
        BLOCKED("BLOCKED"),
        RUNNABLE("RUNNABLE"),
        TERMINATED("TERMINATED"),
        TIMED_WAITING("TIMED_WAITING"),
        WAITING("WAITING"),
        UNKNOWN("UNKNOWN");

        private final String descriptor;

        State(String descriptor) {
            this.descriptor = descriptor;
        }

        @NonNull
        public String getDescriptor() {
            return descriptor;
        }

        @NonNull
        public static State forThread(@NonNull java.lang.Thread thread) {
            java.lang.Thread.State state = thread.getState();
            return getState(state);
        }

        /**
         * Lookup the {@code State} for a given {@link #getDescriptor() descriptor} code. Unlike
         * {@link #valueOf(String) valueOf}, this method will return {@link #UNKNOWN} is no
         * matching {@code State} constant can be found.
         *
         * @param descriptor a consistent descriptor of the state constant to lookup
         * @return the requested {@link State} or {@link #UNKNOWN}
         */
        @NonNull
        public static State byDescriptor(@Nullable String descriptor) {
            if (descriptor == null) {
                return UNKNOWN;
            }

            for (State state : values()) {
                if (state.getDescriptor().equals(descriptor)) {
                    return state;
                }
            }

            return UNKNOWN;
        }

        @NonNull
        private static State getState(java.lang.Thread.State state) {
            switch (state) {
                case NEW:
                    return NEW;
                case BLOCKED:
                    return BLOCKED;
                case RUNNABLE:
                    return RUNNABLE;
                case TERMINATED:
                    return TERMINATED;
                case TIMED_WAITING:
                    return TIMED_WAITING;
                case WAITING:
                    return WAITING;
                default:
                    return UNKNOWN;
            }
        }
    }
}
