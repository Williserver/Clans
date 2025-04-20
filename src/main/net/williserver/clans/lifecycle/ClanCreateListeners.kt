package net.williserver.clans.lifecycle

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import java.util.*

/**
 * Add a player to the clan in the model.
 *
 * @param model List of clans to add this new clan to.
 * @param clan Clan being joined.
 * @throws IllegalArgumentException if this clan is already in the model.
 */
fun createAddClanToModel(model: ClanList, clan: Clan, uuid: UUID?) {
    model.addClan(clan)
}