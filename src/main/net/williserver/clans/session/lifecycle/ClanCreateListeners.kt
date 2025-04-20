package net.williserver.clans.session.lifecycle

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import java.util.*

/**
 * Add a player to the clan in the model.
 *
 * @param model List of clans to add this new clan to.
 * @param clan Clan being joined.
 * @param agent Player initating the join.
 * @throws IllegalArgumentException if this clan is already in the model.
 */
fun createAddClanToModel(model: ClanList, clan: Clan, agent: UUID) {
    model.addClan(clan)
}