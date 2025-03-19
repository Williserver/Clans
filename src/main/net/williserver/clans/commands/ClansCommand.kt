package net.williserver.clans.commands

import net.williserver.clans.LogHandler
import net.williserver.clans.model.ClanList
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

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
        help.append("-- /clans help: pull up this help menu\n")

        s.sendMessage(help.toString())
        return true
    }
}