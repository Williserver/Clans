package net.williserver.clans.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ClansTabCompleter: TabCompleter {

    /**
     * Tab completion for clans command.
     *
     * @param sender Entity which sent the command and will experience tab completion.
     * @param command command being completed for; if not "clans", this tab completer will ignore.
     * @param alias Alternate name for command; unused.
     * @param args Arguments passed to command.
     *
     * @author Willmo3
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val completions = mutableListOf<String>()

        if (!command.name.equals("clans", ignoreCase = true)) {
            return completions
        }

        // Add all possible suggestions for when the user hasn't finished their first subcommand.
        if (args.size == 1) {
            completions.add("help")
            completions.add("create")
            completions.add("disband")
            completions.add("info")
            // Only add completions that correspond with the subcommand they're typing.
            completions.removeAll{ !it.startsWith(args[0].lowercase()) }
        } else if (args.size == 2) {
            if (args[0] == "disband") {
                completions.add("confirm")
            }
            completions.removeAll{ !it.startsWith(args[1].lowercase()) }

        }

        return completions
    }
}