package net.williserver.clans.lifecycle

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import java.util.*

/**
 * Add a player to the clan.
 *
 * @param model List of clans -- @clan should be in here.
 * @param clan Clan to add agent to.
 * @param agent UUID of player to add to clan.
 * @throws NullPointerException if agent is null.
 */
fun joinAddPlayerToClan(model: ClanList, clan: Clan, agent: UUID?) {
    clan.join(agent!!)
}