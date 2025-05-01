package net.williserver.clans.model.clan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.commands.sendClanMessage
import net.williserver.clans.commands.sendPrefixedMessage
import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit

/**
 * @return a listener that sends a message to all clan members when a player joins.
 */
fun constructJoinMessageListener(): ClanLifecycleListener = { clan, _, joiningPlayer ->
    // Getting offline player in case they leave instantly after joining.
    val message = prefix(clan.name)
        .append(Component.text("${Bukkit.getOfflinePlayer(joiningPlayer).name} has joined the clan!",
            NamedTextColor.GREEN))
    sendClanMessage(clan, message)
}

/**
 * @return a listener that sends a message to all clan members when a player leaves the clan.
 */
fun constructLeaveMessageListener(): ClanLifecycleListener = { clan, _, leavingPlayer ->
    val message = prefix(clan.name)
        .append(Component.text("${Bukkit.getOfflinePlayer(leavingPlayer).name} has left the clan.",
            NamedTextColor.DARK_RED))
    sendClanMessage(clan, message)
}

/**
 * @return a listener that sends a message to all clan members when a player is kicked from the clan.
 */
fun constructKickMessageListener(): ClanLifecycleListener = { clan, agent, kickedPlayer ->
    val message = prefix(clan.name)
        .append(Component.text(
            "${Bukkit.getOfflinePlayer(kickedPlayer).name} has been kicked from the clan by ${Bukkit.getOfflinePlayer(agent).name}",
            NamedTextColor.DARK_RED))
    sendClanMessage(clan, message)

    // If the kicked player is online, send them a message.
    Bukkit.getPlayer(kickedPlayer)?.
        let { player -> sendPrefixedMessage(player, Component.text("You were kicked from clan ${clan.name}!",
            NamedTextColor.DARK_RED)) }
}

/**
 * @param name Clan name to format
 * @return a formatted prefix component for a clan name.
 */
fun prefix(name: String) = Component.text("[$name] ", NamedTextColor.DARK_AQUA)