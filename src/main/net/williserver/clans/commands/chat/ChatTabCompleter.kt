package net.williserver.clans.commands.chat

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * Clan chat tab completer. Returns only empty string -- overrides default tab completion.
 * @author Willmo3
 */
class ChatTabCompleter: TabCompleter {

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf("")
    }
}