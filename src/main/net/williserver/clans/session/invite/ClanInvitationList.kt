package net.williserver.clans.session.invite

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
     * Add an invitation mapping a player to a clan.
     */
    fun add(player: UUID, invitation: ClanInvitation) {
        playerToInvitations.putIfAbsent(player, emptySet())
//        if (invitation in playerToInvitations[player]!!) {
//
//        }
    }

}

