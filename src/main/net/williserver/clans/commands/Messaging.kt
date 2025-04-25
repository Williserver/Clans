package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.pluginMessagePrefix
import org.bukkit.Bukkit.broadcast
import org.bukkit.command.CommandSender

/**
 * Broadcast a colored string message.
 *
 * @param message Message to format and broadcast.
 * @param color kyori component color to broadcast message in.
 */
fun broadcastPrefixedMessage(message: String, color: NamedTextColor)
        = broadcast(prefixedMessage(Component.text(message, color)))


/**
 * Append a message prefix component onto a message component.
 *
 * @param message Message to append the plugin prefix to.
 * @return A new component with the plugin prefix appended.
 */
fun prefixedMessage(message: Component)
        = Component.text("$pluginMessagePrefix: ", NamedTextColor.GOLD).append(message)

/**
 * Convert a string into a colored message component and prefix it.
 *
 * @param message Message to format.
 * @param color Color for message.
 * @return A component of the prefixed message.
 */
fun prefixedMessage(message: String, color: NamedTextColor)
        = prefixedMessage(Component.text(message, color))

/**
 * Send a prefixed message component to a target.
 *
 * @param target Entity to receive message.
 * @param message Message to format and send to target.
 */
fun sendPrefixedMessage(target: CommandSender, message: Component)
        = target.sendMessage(prefixedMessage(message))

/**
 * Send a colored string message to a target.
 *
 * @param target Entity to receive message.
 * @param message Message to format and send to target.
 * @param color kyori component color to send message in.
 */
fun sendPrefixedMessage(target: CommandSender, message: String, color: NamedTextColor)
        = target.sendMessage(prefixedMessage(message, color))

/**
 * Send a red-colored error message to a target.
 *
 * @param target Entity to receive error.
 * @param message Error to format and send to target.
 */
fun sendErrorMessage(target: CommandSender, message: String)
        = target.sendMessage(prefixedMessage(Component.text(message, NamedTextColor.RED)))