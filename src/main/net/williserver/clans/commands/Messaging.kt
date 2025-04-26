package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.pluginMessagePrefix
import org.bukkit.Bukkit.broadcast
import org.bukkit.command.CommandSender

/*
 * Pre-formatted message senders.
 */

/**
 * Broadcast a string message. Color will be purple.
 *
 * @param message Message to format and broadcast.
 */
fun broadcastPrefixedMessage(message: String)
        = broadcast(prefixedMessage(Component.text(message, NamedTextColor.DARK_PURPLE)))

/**
 * Send a light-purple colored info message to a target.
 *
 * @param target Entity to receive info message.
 * @param message Message to format and send to target.
 */
fun sendInfoMessage(target: CommandSender, message: String)
    = target.sendMessage(prefixedMessage(Component.text(message, NamedTextColor.LIGHT_PURPLE)))

/**
 * Send a green colored congratulatory message to a target.
 *
 * @param target Entity to recieve message.
 * @param message Message to format and send to target.
 */
fun sendCongratsMessage(target: CommandSender, message: String)
    = target.sendMessage(prefixedMessage(Component.text(message, NamedTextColor.GREEN)))

/**
 * Send a red-colored error message to a target.
 *
 * @param target Entity to receive error.
 * @param message Error to format and send to target.
 */
fun sendErrorMessage(target: CommandSender, message: String)
        = target.sendMessage(prefixedMessage(Component.text(message, NamedTextColor.RED)))

/*
 * Manually colored message senders.
 */

/**
 * Append a message prefix component onto a message component.
 *
 * @param message Message to append the plugin prefix to.
 * @return A new component with the plugin prefix appended.
 */
fun prefixedMessage(message: Component)
        = Component.text("$pluginMessagePrefix: ", NamedTextColor.GOLD).append(message)

/**
 * Send a prefixed message component to a target.
 *
 * @param target Entity to receive message.
 * @param message Message to format and send to target.
 */
fun sendPrefixedMessage(target: CommandSender, message: Component)
        = target.sendMessage(prefixedMessage(message))