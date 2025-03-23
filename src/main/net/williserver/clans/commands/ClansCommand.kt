package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import net.williserver.clans.model.ClanPermission
import net.williserver.clans.model.validClanName
import org.bukkit.Bukkit.broadcast
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Base clans command for viewing and modifying clans.
 *
 * @param logger Logging manager
 * @param clanList clan model for this session.
 *
 * @author Willmo3
 */
class ClansCommand(private val logger: LogHandler,
                    private val clanList: ClanList): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean =
        if (args.isNotEmpty()) {
            when(args[0]) {
                "create" -> create(sender, args)
                "disband" -> disband(sender, args)
                else -> help(sender, args)
            }
        } else help(sender, args)

    /**
     * Usage information for plugin commands.
     *
     * @param s Command sender; will recieve help messages.
     * @param args Args for command. Since this is the help command, should only be one arg.
     */
    private fun help(s: CommandSender, args: Array<out String>): Boolean {
        if (args.size != 1) {
            return false
        }

        val help = StringBuilder()
        help.append("Clans commands:\n")
        help.append("-- /clans help: pull up this help menu.\n")
        help.append("-- /clans create (name): Create a new clan under your visionary leadership.\n")
        help.append("-- /clans disband: Disband the clan you own, if you have permission.\n")

        s.sendMessage(help.toString())
        return true
    }

    /**
     * Create a new clan.
     *
     * @param s Command sender; must be a player.
     * @param args Arguments to command, such as name.
     *
     * @return Whether the usage message should not be displayed.
     */
    private fun create(s: CommandSender, args: Array<String>): Boolean {
        // Initial validation.
        if (args.size > 2) {
            // Returing false here -- this is the only place where the server should send a usage message!
            return false
        } else if (args.size < 2) {
            s.sendMessage(Component.text("[CLANS]: Your clan needs a name!", NamedTextColor.RED))
            return true
        } else if (s !is Player) {
            s.sendMessage(Component.text("[CLANS]: You must be a player to create a clan!", NamedTextColor.RED))
            return true
        }

        // Check if the clan name is valid and unique.
        val name = args[1]
        if (!validClanName(name)) {
            s.sendMessage(Component.text("[CLANS]: You have specified an invalid clan name.", NamedTextColor.RED))
            s.sendMessage(Component.text("Use only alphanumeric characters, underscore, and dash.", NamedTextColor.RED))
            return true
        } else if (name in clanList) {
            s.sendMessage(Component.text("[CLANS]: The name $name is already taken, try a new one!", NamedTextColor.RED))
            return true
        }

        // Check if the leader is already in a clan.
        val leader = s.uniqueId
        if (clanList.playerInClan(leader)) {
            s.sendMessage(Component.text("[CLANS]: You are already in a clan!", NamedTextColor.RED))
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val newClan = Clan(name, leader, arrayListOf(leader))
        // Insert the clan into the main ClanList.
        clanList.addClan(newClan)

        broadcast(Component.text(
            "[CLANS]: Chief ${s.name} has formed the clan \"${newClan.name}\"!",
            NamedTextColor.GREEN))
        return true
    }

    /**
     * Disband the sending player's clan, if they have the requisite permissions.
     *
     * @param s Command sender; must be a player.
     * @param args Arguments to command. Should be none -- implicit argument is the clan player is a member of.
     */
    private fun disband(s: CommandSender, args: Array<out String>): Boolean {
        // API validation
        if (args.size != 1) {
            return false
        } else if (s !is Player) {
            s.sendMessage(Component.text("[CLANS]: You must be a player to disband your clan!", NamedTextColor.RED))
            return true
        }

        // Validate that player has appropriate permissions in some clan.
        if (!clanList.playerInClan(s.uniqueId)) {
            s.sendMessage(Component.text("[CLANS]: You must be in a clan!", NamedTextColor.RED))
            return true
        }
        val clan = clanList.playerClan(s.uniqueId)
        if (!clan.rankOfMember(s.uniqueId).hasPermission(ClanPermission.DISBAND)) {
            s.sendMessage(Component.text("[CLANS]: You don't have permission to disband this clan!", NamedTextColor.RED))
            return true
        }

        // Delete the clan by removing it from the associated ClanList.
        clanList.removeClan(clan)
        broadcast(Component.text("[CLANS]: Clan \"${clan.name}\" has disbanded!", NamedTextColor.DARK_PURPLE))
        return true
    }
}