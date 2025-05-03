package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import java.util.*

/**
 * Manage a single server session's non-persistent data, like confirmation timers.
 *
 * @author Willmo3
 */
class SessionManager {
    /*
     * Map of events to registrations of each value.
     */
    private val timerTable = mapOf<ClanEvent, MutableMap<Any, ConfirmTimer>>(
        // Norm: Pair<UUID, Clan>
        ClanEvent.JOIN to mutableMapOf(),
        // Norm: UUID
        ClanEvent.LEAVE to mutableMapOf(),
        // Norm: clan
        ClanEvent.DISBAND to mutableMapOf(),
        // Norm: Pair<UUID, UUID> (player kicking, player being kicked)
        ClanEvent.KICK to mutableMapOf(),
        // Norm: Pair<UUID, UUID> (player promoting, player being promoted)
        ClanEvent.PROMOTE to mutableMapOf(),
        // TODO: add demote map
    )

    /**
     * Register a timer for some event and key.
     *
     * @param event Event corresponding to timer.
     * @param key Key to register timer under. UUID, Clan, or (UUID, Clan)
     * @param maxTime Time for timer.
     *
     * @return Whether the timer was registered -- do not registered if one already registered.
     * @throws NullPointerException if event is not tracked.
     */
    fun registerTimer(event: ClanEvent, key: Any, maxTime: Long) =
        if (key in timerTable[event]!!) {
            // Fail with error (but not crash) if key already registered.
            false
        } else {
            timerTable[event]!![key] = ConfirmTimer(maxTime)
            true
        }

    /**
     * Deregister a timer for some event and key.
     *
     * @param event Event corresponding to timer.
     * @param key Key to deregister timer for.
     *
     * @return Whether the timer was deregistered.
     * @throws NullPointerException if event is not tracked.
     */
    fun deregisterTimer(event: ClanEvent, key: Any) =
        if (key !in timerTable[event]!!) {
            // Fail with error (but not crash) if key not present
            false
        } else {
            timerTable[event]!! -= key
            true
        }

    /**
     * Evaluate whether a timer is registered under some event and key.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to check if timer is registered to.
     *
     * @return whether a timer is registered under the given key for the given event.
     * @throws NullPointerException if the event is not tracked.
     */
    fun isTimerRegistered(event: ClanEvent, key: Any) = key in timerTable[event]!!

    /**
     * Start a timer corresponding to some event and key.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to the map.
     *
     * @throws NullPointerException if timer has not yet been registered or event is not tracked.
     */
    fun startTimer(event: ClanEvent, key: Any) {
        val timer = timerTable[event]!![key]!!
        timer.reset()
        timer.startTimer()
    }

    /**
     * Check whether a timer corresponding to some event and key is in bounds.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to the map.
     * @return whether the timer is registered, running, and in bounds.
     *
     * @throws NullPointerException if event is not tracked.
     */
    fun isTimerInBounds(event: ClanEvent, key: Any): Boolean {
        val timer = timerTable[event]!![key]
        return timer != null && timer.isRunning() && timer.inBounds()
    }

    /*
     * Listener factories
     */

    /**
     * @return A listener function that, when a player joins the clan, removes the corresponding invite.
     * This prevents repeatedly leaving and rejoining a clan.
     */
    fun constructDeregisterInviteListener(): ClanLifecycleListener =
        { clan: Clan, agent: UUID, _: UUID -> deregisterTimer(ClanEvent.JOIN, Pair(agent, clan))}
}