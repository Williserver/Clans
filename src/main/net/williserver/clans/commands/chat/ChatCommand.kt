package net.williserver.clans.commands.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.commands.assertPlayerInClan
import net.williserver.clans.commands.sendClanMessage
import net.williserver.clans.commands.validPlayer
import net.williserver.clans.model.ClanList
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Clan chat command; wrapper around vanilla teams chats, since scoreboard teams underlie clans.
 *
 * @param logger LogHandler to report clan messages to.
 * @param clans List of clans for this session.
 * @author Willmo3
 */
class ChatCommand(private val logger: LogHandler, private val clans: ClanList): CommandExecutor {
    override fun onCommand(s: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Argument structure validation.
        // Should be at least one message to send.
        if (args.isEmpty()) {
            return false
        }
        // Argument semantics validation.
        if (!validPlayer(s)
            || !assertPlayerInClan(s, clans, (s as Player).uniqueId)) {
            return true
        }

        // Format the message and log it in the server.
        val clan = clans.playerClan(s.uniqueId)
        var baseMessage = "[${clan.name}] ${s.name}: ${args.joinToString(" ")}"
        logger.info(baseMessage)

        // Send a message to each online player in the clan.
        sendClanMessage(clan, Component.text(baseMessage, NamedTextColor.DARK_AQUA))

        return true
    }
}