package net.williserver.clans.session

import net.williserver.clans.model.Clan

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

    /**
     * Register a disband timer for a clan.
     * NOTE: the first timer registered this way will remain for the rest of the session.
     *
     * @param clan Clan to register timer for.
     * @param maxTime Time threshold for confirmation timer.
     * @return whether a new timer was registered.
     */
    fun registerClanDisbandTimer(clan: Clan, maxTime: Long) =
        if (!clanDisbandTimerRegistered(clan)) {
            clanConfirmDeleteMap[clan] = ConfirmTimer(maxTime)
            true
        } else false

    /**
     * @param clan Clan to check timer for.
     * @return whether a disband timer has been registered for the clan.
     */
    fun clanDisbandTimerRegistered(clan: Clan) = clan in clanConfirmDeleteMap

    /**
     * Reset and start the disband timer for a given clan.
     *
     * @param clan Clan to start disband timer for.
     * @throws NullPointerException if timer has not already been registered.
     */
    fun startClanDisbandTimer(clan: Clan) {
        clanConfirmDeleteMap[clan]!!.reset()
        clanConfirmDeleteMap[clan]!!.startTimer()
    }

    /**
     * @param clan Clan to check timer for.
     * @return whether the timer is running and in bounds
     * @throws NullPointerException if timer has not already been registered.
     */
    fun checkDisbandTimerInBounds(clan: Clan) =
        clanConfirmDeleteMap[clan]!!.isRunning() && clanConfirmDeleteMap[clan]!!.inBounds()
}