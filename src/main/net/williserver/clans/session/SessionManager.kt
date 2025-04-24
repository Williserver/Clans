package net.williserver.clans.session

import net.williserver.clans.model.Clan
import java.util.*

/**
 * Manage a single server session's non-persistent data, like confirmation timers.
 *
 * @author Willmo3
 */
class SessionManager {
    /*
     * Map of clans to timers confirming their deletion.
     */
    private val clanConfirmDeleteMap = hashMapOf<Clan, ConfirmTimer>()

    /*
     * Map of players to timers confirming their decision to leave their clans.
     */
    private val playerConfirmLeaveMap = hashMapOf<UUID, ConfirmTimer>()

    /**
     * Map of clans and their invited players.
     */
    private val clanInvitePlayerMap = hashMapOf<Pair<UUID, Clan>, ConfirmTimer>()

    /**
     * Register a timer for some event and key.
     *
     * @param event Event corresponding to timer.
     * @param key Key to register timer under.
     * @param maxTime Time for timer.
     * @throws ClassCastException if key type does not correspond to event map.
     */
    fun registerTimer(event: ClanEvent, key: Any, maxTime: Long) =
        when (event) {
            ClanEvent.CREATE -> false // TODO: implement or remove
            ClanEvent.JOIN -> registerTimer(clanInvitePlayerMap, key as Pair<UUID, Clan>, maxTime)
            ClanEvent.LEAVE -> false // TODO: implement or remove
            ClanEvent.DISBAND -> registerTimer(clanConfirmDeleteMap, key as Clan, maxTime)
        }

    /**
     * Evaluate whether a timer is registered under some event and key.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to check if timer is registered to.
     * @throws ClassCastException if key type does not correspond to event map.
     */
    fun isTimerRegistered(event: ClanEvent, key: Any) = getTimer(event, key) != null

    /**
     * Start a timer corresponding to some event and key.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to the map.
     * @throws ClassCastException if key doesn't correspond to event map.
     * @throws NullPointerException if timer has not yet been registered.
     */
    fun startTimer(event: ClanEvent, key: Any) {
        val timer = getTimer(event, key)!!
        timer.reset()
        timer.startTimer()
    }

    /**
     * Check whether a timer corresponding to some event and key is in bounds.
     *
     * @param event Event to check corresponding timer for.
     * @param key Key to the map.
     * @throws ClassCastException if key doesn't correspond to event map.
     */
    fun isTimerInBounds(event: ClanEvent, key: Any): Boolean {
        val timer = getTimer(event, key)
        return timer != null && timer.isRunning() && timer.inBounds()
    }

    /*
     * Internal helpers.
     */

    // get timer corresponding to some event.
    private fun getTimer(event: ClanEvent, key: Any) = when(event) {
        ClanEvent.CREATE -> TODO()
        ClanEvent.JOIN -> clanInvitePlayerMap[key as Pair<UUID, Clan>]
        ClanEvent.LEAVE -> TODO()
        ClanEvent.DISBAND -> clanConfirmDeleteMap[key as Clan]
    }

    /**
     * @param map mutable map to add the element to.
     * @param key Key to add the timer under
     * @param maxTime Time to put in timer.
     */
    private fun <K> registerTimer(map: MutableMap<K, ConfirmTimer>, key: K, maxTime: Long) =
        if (key !in map) {
            map[key] = ConfirmTimer(maxTime)
            true
        } else false
}