package net.williserver.clans.commands.base

import net.williserver.clans.commands.sendErrorMessage
import net.williserver.clans.model.*
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanPermission
import net.williserver.clans.model.clan.ClanRank
import net.williserver.clans.session.ClanEvent
import net.williserver.clans.session.SessionManager
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Bukkit.getPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Functions to validate a single invocation of a command.
 * Wrap around a sender.
 *
 * @param s Sender producing command.
 */
class ClansCommandValidator(private val s: CommandSender) {

    /**
     * Determine whether a command sender is a player.
     * If not, message them a warning.
     *
     * @return whether the sender is a player -- true indicates correct behavior, false indicates.
     */
    fun assertValidPlayer() =
        if (s !is Player) {
            sendErrorMessage(s, "This command can only be run by players.")
            false
        } else true

    /**
     * Check whether a given playername corresponds to an online player. If not, send an error message.
     *
     * @param name Name to check if player online.
     * @return Whether some player with the name is online.
     */
    fun assertPlayerNameOnline(name: String) =
        if (getPlayer(name) == null) {
            sendErrorMessage(s, "Player \"${name}\" not found -- are they online?")
            false
        } else true

    /**
     * Check whether this player has played before. If not, send an error message.
     *
     * @param name Name to check if valid.
     *
     * @return Whether the player with the given name has played before.
     */
    fun assertPlayerNameValid(name: String) =
        if (!getOfflinePlayer(name).hasPlayedBefore()) {
            sendErrorMessage(s, "\"${name}\" is not a recognized playername.")
            false
        } else true

    /**
     * Check whether sender has a higher rank than some player. If not, send an error message.
     *
     * @param clan Clan both players should be in.
     * @param shouldUnderrank Player who should have the lower rank in the clan.
     *
     * @return Whether @shouldOutrank has a higher rank than @shouldUnderrank
     * @throws IllegalArgumentException if either player is not in this clan.
     */
    fun assertSenderOutranks(clan: Clan, shouldUnderrank: UUID) =
        if (s !is Player || s.uniqueId !in clan || shouldUnderrank !in clan) {
            throw IllegalArgumentException("One of the players was not in the clan ${clan.name}.")
        } else if (clan.rankOf(s.uniqueId) <= clan.rankOf(shouldUnderrank)) {
            sendErrorMessage(s, "You don't outrank the target player!")
            false
        } else true

    /**
     * Check whether the sender is a player in a clan. If not, send an error message.
     *
     * @param clans List of clans for this session.
     *
     * @return whether the player was in one of the clans in list @clans.
     */
    fun assertSenderInAClan(clans: ClanSet) =
        if (s !is Player || !clans.isPlayerInClan(s.uniqueId)) {
            sendErrorMessage(s, "You must be in a clan.")
            false
        } else true

    /**
     * Check whether a player is not in a clan. if they are in a clan, send an error message.
     *
     * @param clans List of clans for this session.
     * @param player UUID of player to search for.
     *
     * @return whether the player was in one of the clans in list @clans.
     */
    fun assertPlayerNotInAClan(clans: ClanSet, player: UUID, message: String) =
        if (clans.isPlayerInClan(player)) {
            sendErrorMessage(s, message)
            false
        } else true

    /**
     * Check whether a player is in the given clan. If not, send an error message.
     *
     * @param clan Clan player should be in.
     * @param player UUID of player to check if in clan.
     * @param message Message to send if conditions fail.
     *
     * @return whether the player was in the clan.
     */
    fun assertPlayerInThisClan(clan: Clan, player: UUID, message: String) =
        if (player !in clan) {
            sendErrorMessage(s, message)
            false
        } else true

    /**
     * Check whether a player has a given permission in their clan. If not, send an error message.
     *
     * @param clan Clan to check permissions against.
     * @param permission Permission to check if player has.
     *
     * @return Whether the player has the specified permission.
     * @throws IllegalArgumentException if player is not in clan.
     */
    fun assertSenderHasPermission(clan: Clan, permission: ClanPermission) =
        if (s is Player && !clan.rankOf(s.uniqueId).hasPermission(permission)) {
            sendErrorMessage(s, "Your clan rank is not high enough for this command.")
            false
        } else true

    /**
     * Check whether a clan name is syntactically valid, according to the data model. If not, send command invoker an error message.
     *
     * @param name Name of clan to validate.
     *
     * @return Whether the name is valid.
     */
    fun assertValidClanName(name: String) =
        if (!Clan.validClanName(name)) {
            sendErrorMessage(s, "You have specified an invalid clan name.")
            sendErrorMessage(s, "Use only alphanumeric characters, underscore, and dash.")
            false
        } else true

    /**
     * Check whether a clan name is unique. If another clan already has the name, send an error message.
     *
     * @param clans List of clans to check duplicate names from.
     * @param name Name to check duplicates for.
     *
     * @return Whether the name is unique.
     */
    fun assertUniqueClanName(clans: ClanSet, name: String) =
        if (name in clans) {
            sendErrorMessage(s, "The name \"$name\" is already taken, try a new one!")
            false
        } else true

    /**
     * Check whether a clan name is in this list. If not, send an error message.
     *
     * @param name Name to check if is in list.
     *
     * @return Whether the clan name is in this list.
     */
    fun assertClanNameInList(list: ClanSet, name: String) =
        if (name !in list) {
            sendErrorMessage(s, "There is no clan named \"$name\".")
            false
        } else true

    // TODO: remove redundant assertSenderNotLeader
    /**
     * Check whether a player is leader of the clan. If they are, send an error message.
     *
     * @param clan Clan player is a member of.
     *
     * @return Whether the player is not leader.
     * @throws IllegalArgumentException if player is not in clan.
     */
    fun assertSenderNotLeader(clan: Clan) =
        if (s is Player && clan.rankOf(s.uniqueId) == ClanRank.LEADER) {
            sendErrorMessage(s, "You may not execute this command as leader.")
            sendErrorMessage(s, "Promote another member to leader first.")
            false
        } else true

    /**
     * Check whether a given player's rank is equal to some other rank. If so, send an error message.
     *
     * @param clan Clan to get rank from.
     * @param targetPlayer Player to check rank of.
     * @param message Error message to send in case of trouble.
     */
    fun assertRankNotEquals(clan: Clan, targetPlayer: UUID, rank: ClanRank, message: String) =
        if (targetPlayer in clan && clan.rankOf(targetPlayer) == rank) {
            sendErrorMessage(s, message)
            false
        } else true

    /**
     * Check whether a given player's rank is not equal to a given rank. If the ranks aren't equal, send an error message.
     *
     * @param clan Clan to get rank from.
     * @param targetPlayer Player to check rank of.
     * @param message Error message to send in case of trouble.
     */
    fun assertRankEquals(clan: Clan, targetPlayer: UUID, rank: ClanRank, message: String) =
        if (targetPlayer in clan && clan.rankOf(targetPlayer) != rank) {
            sendErrorMessage(s, message)
            false
        } else true

    /**
     * Check whether some timer is in bounds. If not, report the issue to the player.
     *
     * @param session Session to check active timer with.
     * @param event Event timer is registered under,
     * @param key Entity timer is registered under.
     * @param subcommand name of subcommand to issue error for.
     */
    fun assertTimerInBounds(session: SessionManager, event: ClanEvent, key: Any, subcommand: String) =
        if (!session.isTimerRegistered(event, key)) {
            sendErrorMessage(s, "You have attempted to execute \"/clans $subcommand confirm\" before starting the timer.")
            sendErrorMessage(s, "Please start the timer with \"/clans $subcommand\" first, or ignore this message to change nothing.")
            false
        } else if (!session.isTimerInBounds(event, key)) {
            sendErrorMessage(s, "The timer to $subcommand has expired!")
            sendErrorMessage(s, "Enter \"/clans $subcommand\" to start again, or ignore this message to change nothing.")
            false
        } else true
}