package net.williserver.clans.model.clan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.commands.sendClanMessage
import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit

/**
 * @return a listener that sends a message to all clan members when a player joins.
 */
fun constructJoinMessageListener(): ClanLifecycleListener = { clan, agent ->
    // Getting offline player in case they leave instantly after joining.
    val message = prefix(clan.name)
        .append(Component.text("${Bukkit.getOfflinePlayer(agent).name} has joined the clan!", NamedTextColor.GREEN))
    sendClanMessage(clan, message)
}

/**
 * @return a listener that sends a message to all clan members when a player leaves the clan.
 */
fun constructLeaveMessageListener(): ClanLifecycleListener = { clan, agent ->
    val message = prefix(clan.name)
        .append(Component.text("${Bukkit.getOfflinePlayer(agent).name} has left the clan.", NamedTextColor.DARK_RED))
    sendClanMessage(clan, message)
}

/**
 * @param name Clan name to format
 * @return a formatted prefix component for a clan name.
 */
fun prefix(name: String) = Component.text("[$name] ", NamedTextColor.DARK_AQUA)