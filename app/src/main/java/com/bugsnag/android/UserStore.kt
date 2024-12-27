package com.bugsnag.android

import com.bugsnag.android.internal.StateObserver
import com.bugsnag.android.internal.dag.Provider
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * This class is responsible for persisting and retrieving user information.
 */
internal class UserStore(
    private val persist: Boolean,
    private val persistentDir: Provider<File>,
    private val deviceIdStore: Provider<DeviceIdStore.DeviceIds?>,
    file: File = File(persistentDir.get(), "user-info"),
    private val sharedPrefMigrator: Provider<SharedPrefMigrator>,
    private val logger: Logger
) {

    private val synchronizedStreamableStore: SynchronizedStreamableStore<User>
    private val previousUser = AtomicReference<User?>(null)

    init {
        this.synchronizedStreamableStore = SynchronizedStreamableStore(file)
    }

    /**
     * Loads the user state which should be used by the [Client]. This is supplied either from
     * the [Configuration] value, or a file in the [Configuration.getPersistenceDirectory] if
     * [Configuration.getPersistUser] is true.
     *
     * If no user is stored on disk, then a default [User] is used which uses the device ID
     * as its ID (unless the generateAnonymousId config option is set to false, in which case the
     * device ID and therefore the user ID is set to
     * null).
     *
     * The [UserState] provides a mechanism for observing value changes to its user property,
     * so to avoid interfering with this the method should only be called once for each [Client].
     */
    fun load(initialUser: User): UserState {
        val validConfigUser = validUser(initialUser)

        val loadedUser = when {
            validConfigUser -> initialUser
            persist -> loadPersistedUser()
            else -> null
        }

        val userState = when {
            loadedUser != null && validUser(loadedUser) -> UserState(loadedUser)
            // if generateAnonymousId config option is false, the deviceId should already be null
            // here
            else -> UserState(User(deviceIdStore.get()?.deviceId, null, null))
        }

        userState.addObserver(
            StateObserver { event ->
                if (event is StateEvent.UpdateUser) {
                    save(event.user)
                }
            }
        )
        return userState
    }

    /**
     * Persists the user if [Configuration.getPersistUser] is true and the object is different
     * from the previously persisted value.
     */
    fun save(user: User) {
        if (persist && user != previousUser.getAndSet(user)) {
            try {
                synchronizedStreamableStore.persist(user)
            } catch (exc: Exception) {
                logger.w("Failed to persist user info", exc)
            }
        }
    }

    private fun validUser(user: User) =
        user.id != null || user.name != null || user.email != null

    private fun loadPersistedUser(): User? {
        return if (sharedPrefMigrator.get().hasPrefs()) {
            val legacyUser = sharedPrefMigrator.get().loadUser(deviceIdStore.get()?.deviceId)
            save(legacyUser)
            legacyUser
        } else if (
            synchronizedStreamableStore.file.canRead() &&
            synchronizedStreamableStore.file.length() > 0L &&
            persist
        ) {
            try {
                synchronizedStreamableStore.load(User.Companion::fromReader)
            } catch (exc: Exception) {
                logger.w("Failed to load user info", exc)
                null
            }
        } else {
            null
        }
    }
}
