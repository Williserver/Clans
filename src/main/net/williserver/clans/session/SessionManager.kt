package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.ClanInvitation
import net.williserver.clans.session.invite.ClanInvitationList
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
    private val clanConfirmLeaveMap = hashMapOf<UUID, ConfirmTimer>()

    /*
     * List of active clan invitations.
     */
    private val clanInvitations = ClanInvitationList()

    /*
     * Clan invitation helpers.
     */

    /**
     * Register an invitation mapping a player to a clan.
     * @param invitation New invite to add to invite list.
     */
    fun addClanInvite(invitation: ClanInvitation) {
        clanInvitations.add(invitation)
    }

    /**
     * @param player Player whose invite should be checked.
     * @param clan Clan to check if invite open.
     * @return whether a clan invite is live.
     */
    fun activeClanInvite(player: UUID, clan: Clan) = clanInvitations.hasActiveInvitation(player, clan)

    /*
     * Clan leaving helpers.
     */

    /*

    /**
     * Register a clan leave timer for a player.
     * NOTE: this timer should be invalidated when the player leaves the clan!
     * NOTE: if invoked with a timer already registered, does nothing.
     *
     * @param player Player to register timer for.
     * @param maxTime time threshold for clan leave.
     * @return whether a new timer was registered.
     */
    fun registerClanLeaveTimer(player: UUID, maxTime: Long): Boolean {
        return true
    }

    /**
     * Deregister a clan leave timer.
     * NOTE: this should be done when a player leaves their clan.
     * NOTE: if invoked when no timer is yet registered, does nothing.
     *
     * @param player Player to deregister timer for.
     * @return whether a timer was removed.
     */
    fun deregisterClanLeaveTimer(player: UUID): Boolean {
        return true
    }

    /**
     * Reset and start the clan leave timer for a player.
     *
     * @param player Player whose timer should be started.
     * @throws NullPointerException if no timer has yet been registered.
     */
    fun startClanLeaveTimer(player: UUID) {

    }

    /**
     * @param player Player to check leave timer for.
     * @return whether the timer is running and in bounds.
     * @throws NullPointerException if the timer has not already been registered.
     */
    fun checkClanLeaveTimerInBounds(player: UUID): Boolean {
        return true
    }

    */

    /*
     * Clan disband helpers.
     */

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