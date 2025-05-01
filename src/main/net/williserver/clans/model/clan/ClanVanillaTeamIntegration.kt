package net.williserver.clans.model.clan

import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit

/**
 * Listeners to manage membership in scoreboard teams.
 *
 * @author Willmo3
 */

/**
 * @return a listener that creates a new scoreboard team when a new clan is created.
 */
fun constructCreateAddTeamListener(): ClanLifecycleListener = { clan, creator, _ ->
    val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    if (scoreboard.getTeam(clan.name) != null) {
        throw IllegalStateException("Team already registered -- how did we get here?")
    }
    scoreboard.registerNewTeam(clan.name)
    scoreboard.getTeam(clan.name)!!.addPlayer(Bukkit.getOfflinePlayer(creator))
}

/**
 * @return a listener that removes the scoreboard team when a clan is deleted.
 */
fun constructDisbandRemoveTeamListener(): ClanLifecycleListener = { clan, _, _ ->
    val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        ?: throw IllegalStateException("Team not registered -- how did we get here?")
    team.unregister()
}

/**
 * @return a listener that adds a player to a scoreboard team when they join the clan.
 */
fun constructJoinTeamListener(): ClanLifecycleListener = { clan, _, joiner ->
    val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        ?: throw IllegalStateException("Clan not registered to a team -- how did we get here?")
    team.addPlayer(Bukkit.getOfflinePlayer(joiner))
}

/**
 * @return a listener that removes a player from a scoreboard team when they leave the clan.
 */
fun constructLeaveTeamListener(): ClanLifecycleListener = { clan, _, leaver ->
    val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        ?: throw IllegalStateException("Clan not registered to a team -- how did we get here?")
    if (!team.removePlayer(Bukkit.getOfflinePlayer(leaver))) {
        throw IllegalStateException("Player is not a member of this team -- how did we get here?")
    }
}