package net.williserver.clans.commands.base

import net.williserver.clans.model.ClanSet
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * State for clans tab completion.
 *
 * @param clanSet list of clans in this session.
 */
class ClansTabCompleter(private val clanSet: ClanSet): TabCompleter {

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
        when (args.size) {
        1 -> {
            completions.add("help")
            completions.add("crown")
            completions.add("create")
            completions.add("demote")
            completions.add("disband")
            completions.add("info")
            completions.add("invite")
            completions.add("join")
            completions.add("kick")
            completions.add("leave")
            completions.add("list")
            completions.add("promote")
            completions.add("setPrefix")
            completions.add("setColor")
            // Only add completions that correspond with the subcommand they're typing.
            completions.removeAll{ !it.startsWith(args[0]) }
        }
        2 -> {
            when (args[0].lowercase()) {
                "info", "join"-> clanSet.clans().forEach { completions.add(it.name) }
                "invite" -> getOnlinePlayers().forEach { completions.add(it.name) }
                "kick", "promote", "demote", "crown" ->
                    if (sender is Player && clanSet.isPlayerInClan(sender.uniqueId)) {
                        // Notice: all UUIDs must map to a name, as otherwise they would not have been in a clan.
                        clanSet.clanOf(sender.uniqueId)
                            .allClanmates()
                            .forEach { playerUUID -> completions.add(getOfflinePlayer(playerUUID).name!!) }
                    }
                "disband", "leave" -> completions.add("confirm")
                else -> {}
            }
            // Do not include lowercase or playernames will not work!
            completions.removeAll{ !it.startsWith(args[1]) }
        }
        3 -> {
            // The third argument to kick will only ever be "confirm"
            when (args[0].lowercase()) {
                "kick", "crown" -> completions.add("confirm")
            }
            completions.removeAll{ !it.startsWith(args[2].lowercase()) }
        }
        }

        return completions
    }
}