package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean =
        if (args.isNotEmpty()) {
            when(args[0]) {
                "help" -> help(sender, args)
                "create" -> create(sender, args)
                else -> false
            }
        } else false

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

        s.sendMessage(help.toString())
        return true
    }

    /**
     * Create a new clan.
     *
     * @param s Command sender; must be a player.
     * @param args Arguments to command, such as name.
     */
    private fun create(s: CommandSender, args: Array<out String>): Boolean {
        if (s !is Player) {
            s.sendMessage(Component.text("[CLANS]: You must be a player to create a clan!", NamedTextColor.RED))
            return false
        } else if (args.size != 2) {
            // Special case: A clan name is omitted. Extra help info provided.
            if (args.size < 2) {
                s.sendMessage(Component.text("[CLANS]: Your clan needs a name!", NamedTextColor.RED))
            }
            return false
        }
        return true

        // Check if the clan name already exists.
        val name = args[1]
        if (name in clanList) {
            s.sendMessage(Component.text("[CLANS]: The name $name is already taken, try a new one!", NamedTextColor.RED))
            return true
        }

        // Check if the leader is already in a clan.
        val leader = s.uniqueId
        if (clanList.playerClan(leader)!= null) {
            s.sendMessage(Component.text("[CLANS]: You are already in a clan!", NamedTextColor.RED))
            return true
        }

        // Create a new clan with this player as its leader and starting member.

        // Insert the clan into the main ClanList.

        return true
    }
}