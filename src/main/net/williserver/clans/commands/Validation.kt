package net.williserver.clans.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// Helpers for the subcommands.

/**
 * Determine whether a command sender is a player.
 * If not, message them a warning.
 *
 * @param s Sender to check
 * @return whether the sender is a player.
 */
fun validPlayer(s: CommandSender): Boolean =
    if (s !is Player) {
        sendErrorMessage(s, "This command can only be run by players.")
        false
    } else true
