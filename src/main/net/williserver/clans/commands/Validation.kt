package net.williserver.clans.commands

import net.williserver.clans.model.ClanList
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

// Helpers for the subcommands.

/**
 * Determine whether a command sender is a player.
 * If not, message them a warning.
 *
 * @param s Sender to check
 * @return whether the sender is a player -- true indicates correct behavior, false indicates.
 */
fun validPlayer(s: CommandSender): Boolean =
    if (s !is Player) {
        sendErrorMessage(s, "This command can only be run by players.")
        false
    } else true

/**
 * Check whether a player is in a clan. if not, send an error message.
 *
 * @param s Sender to report errors to.
 * @param clans List of clans for this session.
 * @param player UUID of player to search for.
 *
 * @return whether the player was in one of the clans in list @clans.
 */
fun assertPlayerInClan(s: CommandSender, clans: ClanList, player: UUID) =
    if (!clans.playerInClan(player)) {
        sendErrorMessage(s, "You must be in a clan to invoke this command.")
        false
    } else true