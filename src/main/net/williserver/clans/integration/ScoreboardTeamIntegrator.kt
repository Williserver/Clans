package net.williserver.clans.integration

import net.williserver.clans.LogHandler
import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Team

/**
 * Manages integration with Vanilla scoreboard teams, creating and disbanding groups when needed.
 *
 * @author Willmo3
 */
class ScoreboardTeamIntegrator(private val logger: LogHandler) {
    companion object {
        const val LOG_PREFIX = "[Scoreboard Teams]"
    }

    /**
     * @return a listener that creates a new scoreboard team when a new clan is created.
     */
    fun constructCreateAddTeamListener(): ClanLifecycleListener = { clan, creator, _ ->
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        if (scoreboard.getTeam(clan.name) != null) {
            warnTeamRegistered(clan.name)
        } else {
            scoreboard.registerNewTeam(clan.name)
            logger.info("$LOG_PREFIX: added team ${clan.name}")

            val player = Bukkit.getOfflinePlayer(creator)
            scoreboard.getTeam(clan.name)!!.addPlayer(player)
            logger.info("$LOG_PREFIX: added player ${player.name} to team ${clan.name}.")
        }
    }

    /**
     * @return a listener that removes the scoreboard team when a clan is deleted.
     */
    fun constructDisbandRemoveTeamListener(): ClanLifecycleListener = { clan, _, _ ->
        val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        if (team == null) {
            warnTeamNotRegistered(clan.name)
        } else {
            team.unregister()
            logger.info("$LOG_PREFIX: removed team ${clan.name}.")
        }
    }

    /**
     * @return a listener that adds a player to a scoreboard team when they join the clan.
     */
    fun constructJoinTeamListener(): ClanLifecycleListener = { clan, _, joiner ->
        val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        if (team == null) {
            warnTeamNotRegistered(clan.name)
        } else {
            val player = Bukkit.getOfflinePlayer(joiner)
            team.addPlayer(player)
            logger.info("$LOG_PREFIX: added ${player.name} to team ${clan.name}.")
        }
    }

    /**
     * @return a listener that removes a player from a scoreboard team when they leave the clan.
     */
    fun constructLeaveTeamListener(): ClanLifecycleListener = { clan, _, leaver ->
        val team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(clan.name)
        if (team == null) {
            warnTeamNotRegistered(clan.name)
        } else {
            val player = Bukkit.getOfflinePlayer(leaver)
            if (!team.removePlayer(Bukkit.getOfflinePlayer(leaver))) {
                warnPlayerNotInTeam(team.name, player.name?:"INVALID")
            } else {
                logger.info("$LOG_PREFIX: removed ${player.name} from team ${clan.name}.")
            }
        }
    }

    /**
     * @param name Name of team already registered
     */
    private fun warnTeamRegistered(name: String) {
        logger.err("$LOG_PREFIX: a team is already registered under $name.")
        logger.err("$LOG_PREFIX: this may indicate that teams integration was turned off at some point.")
        logger.err("$LOG_PREFIX: please delete the previous team.")
    }

    /**
     * @param name Name of team not registered.
     */
    private fun warnTeamNotRegistered(name: String) {
        logger.err("$LOG_PREFIX: no team is registered under $name.")
        logger.err("$LOG_PREFIX: this may indicate the clan was created before teams integration was turned on.")
        logger.err("$LOG_PREFIX: consider manually creating the team if you wish to integrate.")
    }

    /**
     * @param teamName Name of team player should be a member of.
     * @param playerName Name of player who should be in the team.
     */
    private fun warnPlayerNotInTeam(teamName: String, playerName: String) {
        logger.err("$LOG_PREFIX: player $playerName is not already in team $teamName.")
        logger.err("$LOG_PREFIX: this may indicate that teams integration was turned off at some point.")
    }
}