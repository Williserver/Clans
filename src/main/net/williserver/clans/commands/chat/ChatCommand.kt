package net.williserver.clans.commands.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.commands.assertSenderInAClan
import net.williserver.clans.commands.sendClanMessage
import net.williserver.clans.commands.assertValidPlayer
import net.williserver.clans.model.ClanSet
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Clan chat command; wrapper around vanilla teams chats, since scoreboard teams underlie clans.
 *
 * @param clans List of clans for this session.
 * @author Willmo3
 */
class ChatCommand(private val clans: ClanSet): CommandExecutor {
    override fun onCommand(s: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Argument structure validation.
        // Should be at least one message to send.
        if (args.isEmpty()) {
            return false
        }
        // Argument semantics validation.
        if (!assertValidPlayer(s)
            || !assertSenderInAClan(s, clans)) {
            return true
        }

        // Format the message and log it in the server.
        val clan = clans.clanOf((s as Player).uniqueId)
        // Send a message to each online player in the clan.
        sendClanMessage(clan, Component.text("[${clan.name}] ${s.name}: ${args.joinToString(" ")}", NamedTextColor.DARK_AQUA))
        return true
    }
}