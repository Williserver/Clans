package net.williserver.clans.commands

import net.williserver.clans.model.ClanList
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * State for clans tab completion.
 *
 * @param clanList list of clans in this session.
 */
class ClansTabCompleter(private val clanList: ClanList): TabCompleter {

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
            completions.add("invite")
            completions.add("join")
            completions.add("kick")
            completions.add("leave")
            completions.add("list")
            // Only add completions that correspond with the subcommand they're typing.
            completions.removeAll{ !it.startsWith(args[0].lowercase()) }
        } else if (args.size == 2) {
            when (args[0].lowercase()) {
                "info" -> clanList.clans().forEach { completions.add(it.name) }
                "invite" -> getOnlinePlayers().forEach { completions.add(it.name) }
                "join" -> clanList.clans().forEach { completions.add(it.name) }
                "kick" ->
                    if (sender is Player && clanList.playerInClan(sender.uniqueId)) {
                        // Notice: all UUIDs must map to a name, as otherwise they would not have been in a clan.
                        clanList.playerClan(sender.uniqueId)
                            .members()
                            .forEach { playerUUID -> completions.add(getOfflinePlayer(playerUUID).name!!) }
                    }
                "disband" -> completions.add("confirm")
                "leave" -> completions.add("confirm")
                else -> {}
            }
            completions.removeAll{ !it.startsWith(args[1].lowercase()) }
        } else if (args.size == 3) {
            when (args[0].lowercase()) {
                "kick" -> completions.add("confirm")
            }
            completions.removeAll{ !it.startsWith(args[2].lowercase()) }
        }

        return completions
    }
}