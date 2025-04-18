package net.williserver.clans.session.invite

import net.williserver.clans.model.Clan
import java.util.*
import kotlin.collections.HashMap

/**
 * Represents a set of invitations from clans to players in this session.
 *
 * @author Willmo3
 */
class ClanInvitationList {
    private val playerToInvitations = HashMap<UUID, MutableSet<ClanInvitation>>()

    /**
     * Add an invitation mapping a player to a clan.
     *
     * @param invitation Invitation mapping UUID to clan, and potentially time.
     */
    fun add(invitation: ClanInvitation) {
        val invitedPlayer = invitation.player
        val clanInvitedTo = invitation.clan
        playerToInvitations.putIfAbsent(invitedPlayer, mutableSetOf())

        // This function should only be invoked if no invitation is yet active -- use the hasActiveInvitation helper!
        if (hasActiveInvitation(invitedPlayer, clanInvitedTo)) {
            throw IllegalArgumentException("Player $invitedPlayer is already invited to $clanInvitedTo")
        }
        playerToInvitations[invitedPlayer]!! += invitation
    }

    /**
     * @return Whether the specified player has an active invitation to target clan.
     */
    fun hasActiveInvitation(player: UUID, target: Clan) =
        playerToInvitations[player]?.any { it.clan == target && it.validInvite()} ?: false
}

