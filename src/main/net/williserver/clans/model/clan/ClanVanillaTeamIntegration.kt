package net.williserver.clans.model.clan

import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit

/**
 * Listeners to manage membership in scoreboard teams.
 *
 * @author Willmo3
 */

/**
 * Construct a listener that creates a new scoreboard team when a new clan is created.
 * @return the listener.
 */
fun constructCreateAddTeamListener(): ClanLifecycleListener = { clan, creator ->
    val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    if (scoreboard.getTeam(clan.name) != null) {
        throw IllegalStateException("Team already registered -- how did we get here?")
    }
    scoreboard.registerNewTeam(clan.name)
    scoreboard.getTeam(clan.name)!!.addPlayer(Bukkit.getOfflinePlayer(creator))
}

