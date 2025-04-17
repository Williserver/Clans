package net.williserver.clans.session.invite

import net.williserver.clans.model.Clan
import net.williserver.clans.session.ConfirmTimer
import java.util.*

/**
 * A single invitation to a clan, with the time it was created.
 *
 * @param player ID of invited player.
 * @param clan Clan invitation is for.
 *
 * @author Willmo3
 */
open class ClanInvitation(val player: UUID, val clan: Clan) {
    /**
     * Determine whether an open invitation is valid.
     * By default, always true, but subclasses may implement their own
     * @return whether this invitation is valid.
     */
    open fun validInvite(): Boolean = true

    /**
     * @return whether the invitations refer to the same clan and are both still valid
     */
    override fun equals(other: Any?): Boolean = other is ClanInvitation
            && player == other.player && clan == other.clan

    /*
     * Automatically generated hashcode implementation.
     */
    override fun hashCode(): Int {
        var result = player.hashCode()
        result = 31 * result + clan.hashCode()
        return result
    }
}

/**
 * A clan invitation with a timer. Usable by commands to implement invitation timeouts.
 *
 * @param player ID of invited player.
 * @param clan Clan invitation is for.
 * @param timer preconfigured timer representing how long the invite may remain valid.
 * We will start the timer at construction time.
 */
class TimedClanInvitation(player: UUID, clan: Clan, val timer: ConfirmTimer): ClanInvitation(player, clan) {
    init {
        if (timer.isRunning()) {
            throw IllegalStateException("Provided timer has already been started!")
        }
        // The actual time bound of the timer will be provided elsewhere, but we start the timer here.
        timer.startTimer()
    }

    /**
     * Timed invitations are only equal if they also share the same timer.
     */
    override fun equals(other: Any?): Boolean =
        super.equals(other) && other is TimedClanInvitation && timer == other.timer

    /**
     * @return Whether this invitation is still open, or if the timer has expired.
     */
    override fun validInvite() = timer.inBounds()

    /*
     * Automatically generated hashcode implementation.
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + timer.hashCode()
        return result
    }
}