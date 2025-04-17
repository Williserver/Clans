package net.williserver.clans.session

import net.williserver.clans.model.ClanList
import java.util.*
import kotlin.collections.HashMap

/**
 * Represents a set of invitations from clans to players in this session.
 *
 * @param list List of clans for this session.
 * @author Willmo3
 */
class ClanInvitationList(private val list: ClanList) {
    private val playerToInvitations = HashMap<UUID, Set<ClanInvitation>>()

    /**
     * A single invitation to a clan, with the time it was created.
     *
     * @param clanName name of clan invitation is for.
     * @param initiationTime Time that invitation was initiated.
     */
    data class ClanInvitation(private val clanName: String, private val initiationTime: ConfirmTimer) {
        override fun equals(other: Any?): Boolean = other is ClanInvitation && clanName == other.clanName
        override fun hashCode(): Int = clanName.hashCode()
    }
}

