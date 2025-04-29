package net.williserver.clans.commands

import net.williserver.clans.model.*
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanPermission
import net.williserver.clans.model.clan.ClanRank
import net.williserver.clans.model.clan.validClanName
import net.williserver.clans.session.ClanEvent
import net.williserver.clans.session.SessionManager
import org.bukkit.Bukkit.getPlayer
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
 * Check whether a given playername corresponds to an online player. If not, send an error message.
 *
 * @param s Sender to report errors to.
 * @param name Name to check if player online.
 *
 * @return Whether some player with the name is online.
 */
fun assertPlayerNameOnline(s: CommandSender, name: String) =
    if (getPlayer(name) == null) {
        sendErrorMessage(s, "Player \"${name}\" not found -- are they online?")
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
fun assertPlayerInAClan(s: CommandSender, clans: ClanList, player: UUID) =
    if (!clans.playerInClan(player)) {
        sendErrorMessage(s, "You must be in a clan to invoke this command.")
        false
    } else true

/**
 * Check whether a player is not in a clan. if they are in a clan, send an error message.
 *
 * @param s Sender to report errors to.
 * @param clans List of clans for this session.
 * @param player UUID of player to search for.
 *
 * @return whether the player was in one of the clans in list @clans.
 */
fun assertPlayerNotInAClan(s: CommandSender, clans: ClanList, player: UUID, message: String) =
    if (clans.playerInClan(player)) {
        sendErrorMessage(s, message)
        false
    } else true

/**
 * Check whether a player has a given permission. If not, send an error message.
 *
 * @param s Sender to report errors to.
 * @param clan Clan to check permissions against.
 * @param player Player to check permissions for.
 * @param permission Permission to check if player has.
 *
 * @return Whether the player has the specified permission.
 * @throws IllegalArgumentException if player is not in clan.
 */
fun assertHasPermission(s: CommandSender, clan: Clan, player: UUID, permission: ClanPermission) =
    if (!clan.rankOfMember(player).hasPermission(permission)) {
        sendErrorMessage(s, "You need clan permission \"$permission\" to use this command.")
        false
    } else true

/**
 * Check whether a clan name is syntactically valid, according to the data model. If not, send command invoker an error message.
 *
 * @param s Sender to report errors to.
 * @param name Name of clan to validate.
 *
 * @return Whether the name is valid.
 */
fun assertValidClanName(s: CommandSender, name: String) =
    if (!validClanName(name)) {
        sendErrorMessage(s, "You have specified an invalid clan name.")
        sendErrorMessage(s, "Use only alphanumeric characters, underscore, and dash.")
        false
    } else true

/**
 * Check whether a clan name is unique. If another clan already has the name, send an error message.
 *
 * @param s Sender to report errors to.
 * @param clans List of clans to check duplicate names from.
 * @param name Name to check duplicates for.
 *
 * @return Whether the name is unique.
 */
fun assertUniqueClanName(s: CommandSender, clans: ClanList, name: String) =
    if (name in clans) {
        sendErrorMessage(s, "The name \"$name\" is already taken, try a new one!")
        false
    } else true

/**
 * Check whether a clan name is in this list. If not, send an error message.
 *
 * @param s Sender to report errors to.
 * @param name Name to check if is in list.
 *
 * @return Whether the clan name is in this list.
 */
fun assertClanNameInList(s: CommandSender, list: ClanList, name: String) =
    if (name !in list) {
        sendErrorMessage(s, "There is no clan named \"$name\".")
        false
    } else true

/**
 * Check whether a player is leader of the clan. If they are, send an error message.
 *
 * @param s Sender to report errors to.
 * @param clan Clan player is a member of.
 * @param player Player to check leadership status of.
 *
 * @return Whether the player is not leader.
 * @throws IllegalArgumentException if player is not in clan.
 */
fun assertPlayerNotLeader(s: CommandSender, clan: Clan, player: UUID) =
    if (clan.rankOfMember(player) == ClanRank.LEADER) {
        sendErrorMessage(s, "You may not execute this command as leader.")
        sendErrorMessage(s, "Promote another member to leader first.")
        false
    } else true

/**
 * Check whether some timer is in bounds. If not, report the issue to the player.
 *
 * @param s Sender to report errors to.
 * @param session Session to check active timer with.
 * @param event Event timer is registered under,
 * @param key Entity timer is registered under.
 * @param subcommand name of subcommand to issue error for.
 */
fun assertTimerInBounds(s: CommandSender, session: SessionManager, event: ClanEvent, key: Any, subcommand: String) =
    if (!session.isTimerRegistered(event, key)) {
        sendErrorMessage(s, "You have attempted to execute \"/clans $subcommand confirm\" before starting the timer.")
        sendErrorMessage(s, "Please start the timer with \"/clans $subcommand\" first, or ignore this message to change nothing.")
        false
    } else if (!session.isTimerInBounds(event, key)) {
        sendErrorMessage(s, "The timer to $subcommand has expired!")
        sendErrorMessage(s, "Enter \"/clans $subcommand\" to start again, or ignore this message to change nothing.")
        false
    } else true