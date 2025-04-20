package net.williserver.clans.session.lifecycle

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import net.williserver.clans.model.ClanPermission
import java.util.*

/**
 * Remove a clan from the model.
 *
 * @param model List of clans to remove this clan from.
 * @param clan Clan being disbanded.
 * @param agent Player initating the disband.
 *
 * @throws IllegalStateException if the agent does not have permission to remove the clan.
 * @throws NoSuchElementException if this clan is not already in the list.
 */
fun disbandRemoveClanFromModel(model: ClanList, clan: Clan, agent: UUID) {
    if (!clan.rankOfMember(agent).hasPermission(ClanPermission.DISBAND)) {
        throw IllegalStateException("Player $agent does not have permission to delete this clan!")
    }
    model.removeClan(clan)
}