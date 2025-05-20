package net.williserver.clans.commands.base

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.commands.*
import net.williserver.clans.model.*
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanPermission
import net.williserver.clans.model.clan.ClanRank
import net.williserver.clans.ClansPlugin.Companion.pluginMessagePrefix
import net.williserver.clans.session.ClanEvent
import net.williserver.clans.session.SessionManager
import net.williserver.clans.session.ClanEvent.*
import net.williserver.clans.session.ClanEventBus
import org.bukkit.Bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Base clans command for viewing and modifying clans.
 *
 * @param clanSet clan model for this session.
 * @param config Configuration options for this session.
 * @param session Session-specific data, like timers.
 * @param bus Event bus with registered listeners for events in clan lifecycle that may be caused by command invocation.
 *
 * @author Willmo3
 */
class ClansCommand(private val clanSet: ClanSet,
                   private val config: ClansConfig,
                   private val session: SessionManager,
                   private val bus: ClanEventBus
): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>) =
        if (args.isNotEmpty()) {
            val subcommand = args[0]
            val execute = ClansSubcommandExecutor(sender, args.drop(1), clanSet, config, session, bus)

            when(subcommand.lowercase()) {
                "crown"   -> execute.crown()
                "create"  -> execute.create()
                "demote"  -> execute.demote()
                "disband" -> execute.disband()
                "info"    -> execute.info()
                "invite"  -> execute.invite()
                "join"    -> execute.join()
                "kick"    -> execute.kick()
                "help"    -> execute.help()
                "leave"   -> execute.leave()
                "list"    -> execute.list()
                "promote" -> execute.promote()
                else      -> false
            }

        } else ClansSubcommandExecutor(sender, args.toList(), clanSet, config, session, bus).help()

/**
 * Shares state needed to execute subcommands of the base clans command.
 * New instance constructed when subcommand run.
 *
 * @param s Entity command is executed on behalf of.
 * @param args List of arguments to invoke subcommand with.
 * @param clanSet clan model for this session.
 * @param config Configuration options for this session.
 * @param session Session-specific data, like timers.
 * @param bus Event bus with registered listeners for events in clan lifecycle that may be caused by command invocation.
 * @author Willmo3
 */
private class ClansSubcommandExecutor(
    private val s: CommandSender,
    private val args: List<String>,
    private val clanSet: ClanSet,
    private val config: ClansConfig,
    private val session: SessionManager,
    private val bus: ClanEventBus
) {
    private val v = ClansCommandValidator(s)

    /**
     * Usage information for plugin commands.
     **/
    fun help(): Boolean {
        val header = prefixedMessage(Component.text("Commands:"))
        val bullet = Component.text("\n- /clans ", NamedTextColor.GOLD)

        fun generateCommandHelp(name: String, text: String)
                = bullet.append(Component.text("$name: ", NamedTextColor.RED).append(Component.text(text, NamedTextColor.GRAY)))

        // Special case: cc is outside of scope of normal clans command
        val chat = Component.text("\n- /cc: ", NamedTextColor.RED).append(Component.text("Send a message to only your clanmates", NamedTextColor.GRAY))
        // All other commands are prefixed by /clans
        val help = generateCommandHelp("help", "pull up this help menu.")
        val coronate = generateCommandHelp("coronate", "promote a co-leader to be the new leader of your clan.")
        val create = generateCommandHelp("create (name)", "create a new clan under your visionary leadership.")
        val demote = generateCommandHelp("demote (playername)", "demote a player in your clan.")
        val disband = generateCommandHelp("disband", "disband the clan you own.")
        val info = generateCommandHelp("info (name)", "get information about a clan.")
        val invite = generateCommandHelp("invite (user)", "invite a member to your clan.")
        val join = generateCommandHelp("join (clan name)", "join a clan.")
        val kick = generateCommandHelp("kick (playername)", "kick a player from your clan.")
        val leave = generateCommandHelp("leave", "leave your clan.")
        val list = generateCommandHelp("list (page)", "output a list of clans.")
        val promote = generateCommandHelp("promote (playername)", "Promote a player in your clan.")

        s.sendMessage(header
            .append(chat)
            .append(help)
            .append(coronate)
            .append(create)
            .append(demote)
            .append(disband)
            .append(info)
            .append(invite)
            .append(join)
            .append(kick)
            .append(leave)
            .append(list)
            .append(promote)
        )
        return true
    }

    /**
     * Send the invoker a message containing a list of all clans.
     */
    fun list(): Boolean {
        // Argument structure validation. One optional arg: page no.
        if (args.size > 1) {
            return false
        }

        val numClans = clanSet.clans().size
        // Should not be one, or the modular arithmetic will be ruined.
        val clansPerPage = 10
        var lastPageNumber = numClans / clansPerPage
        // Edge case: a multiple of clansPerPage clans -- last page is previous.
        if (lastPageNumber > 0 && numClans % clansPerPage == 0) {
            lastPageNumber--
        }

        val selectedPage = when (args.size) {
            0 -> 0
            1 -> {
                // Avoid negative pages or out of bounds pages.
                val parsedPage = max(args[0].toIntOrNull() ?: 0, 0)
                min(parsedPage, lastPageNumber)
            }
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans list -- this should have been caught earlier!")
        }

        val listTitle = Component.text("List:", NamedTextColor.AQUA)
        val sortedClans = clanSet.clans()
            .sortedBy { it.allClanmates().size }
            .subList(selectedPage * clansPerPage, min(selectedPage * clansPerPage + clansPerPage, numClans))
            .fold(Component.text())
            { text, thisClan ->
                text.append(Component.text("\n- ${thisClan.name}: ", NamedTextColor.GOLD))
                    .append(Component.text("${thisClan.allClanmates().size} clanmates", NamedTextColor.RED))
            }
            .append(Component.text("\n\nPage $selectedPage of $lastPageNumber", NamedTextColor.GRAY))

        sendPrefixedMessage(s, listTitle.append(sortedClans))
        return true
    }

    /**
     * Create a new clan.
     **/
    fun create(): Boolean {
        // Argument structure validation. One arg: new clan name.
        if (args.size > 1) {
            return false
        } else if (args.isEmpty()) {
            // Special case: they forgot to include a name. Send a small reminder rather than a full usage report.
            sendErrorMessage(s, "Your clan needs a name!")
            return true
        }
        // Argument semantics validation
        if (!v.assertValidPlayer()
            || !v.assertPlayerNotInAClan(clanSet, (s as Player).uniqueId, "You are already in a clan!")
            || !v.assertValidClanName(args[0])
            || !v.assertUniqueClanName(clanSet, args[0])) {
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val leader = s.uniqueId
        val newClan = Clan(args[0], leader)
        bus.fireEvent(CREATE, newClan, agent=leader, target=leader)
        return true
    }

    /**
     * Disband the sending player's clan, if they have the requisite permissions.
     * First, the user must execute /clans disband. This starts a confirmation timer.
     * Then, the user must execute /clans disband confirm within the required time.
     **/
    fun disband(): Boolean {
        // Argument structure validation.
        // Expect /clans disband or /clans disband confirm
        if (args.size > 1) {
            return false
        }
        // Argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertSenderHasPermission(clanSet.clanOf((s as Player).uniqueId), ClanPermission.DISBAND)) {
            return true
        }

        // Either initiate a new disband attempt, or confirm one if it's done in time.
        val clan = clanSet.clanOf(s.uniqueId)
        return when(args.size) {
            0 -> {
                session.registerTimer(DISBAND, clan, config.confirmTime.toLong())
                sendInfoMessage(s, "You have begun to disband your clan!")
                sendInfoMessage(s, "Enter \"/clans disband confirm\" within ${config.confirmTime} seconds to confirm this choice.")
                session.startTimer(DISBAND, clan)
                true
            }
            1 -> {
                /*
                 * Confirm that the clan should be disbanded after the timer started.
                 * If so, remove the clan from this list.
                 */
                // Argument structure validation: one arg, confirm
                if (args[0].lowercase() != "confirm") {
                    return false
                }
                if (v.assertTimerInBounds(session, DISBAND, clan, "disband")) {
                    bus.fireEvent(DISBAND, clan, agent=s.uniqueId, target=s.uniqueId)
                }
                true
            }
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans disband -- this should have been caught earlier!")
        }
    }

    /**
     * Invite a player to our clan.
     ** @return Whether the command was invoked with the correct number of arguments.
     */
    fun invite(): Boolean {
        // Argument structure validation: 1 arg: target
        if (args.size != 1) {
            return false
        }
        // Argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertSenderHasPermission(clanSet.clanOf((s as Player).uniqueId), ClanPermission.INVITE)
            || !v.assertPlayerNameOnline(args[0])
            || !v.assertPlayerNotInAClan(clanSet, getPlayer(args[0])!!.uniqueId,
                "${getPlayer(args[0])!!.name} is already in a clan!")) {
            return true
        }

        val targetClan = clanSet.clanOf(s.uniqueId)
        val targetPlayer = getPlayer(args[0])!!
        // Validate that player is not currently waiting on invitation.
        if (session.isTimerInBounds(JOIN, Pair(s.uniqueId, targetClan))) {
            sendErrorMessage(s, "${targetPlayer.name} is already waiting on an invitation from you.")
            return true
        }

        sendCongratsMessage(s, "You have invited ${targetPlayer.name} to your clan!")
        sendCongratsMessage(targetPlayer, "${s.name} has invited you to clan ${targetClan.name}!")
        sendCongratsMessage(targetPlayer, "You have ${config.confirmTime} seconds to accept your invitation using \"/clans join ${targetClan.name}\".")

        // Invitation timer starts immediately after timer registered.
        session.registerTimer(JOIN, Pair(targetPlayer.uniqueId, targetClan), config.confirmTime.toLong())
        session.startTimer(JOIN, Pair(targetPlayer.uniqueId, targetClan))
        return true
    }

    /**
     * Join a new clan.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    fun join(): Boolean {
        // Argument structure validation: 1 arg: target
        if (args.size != 1) {
            return false
        }
        // Argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertClanNameInList(clanSet, args[0])
            || !v.assertPlayerNotInAClan(clanSet, (s as Player).uniqueId, "You are already in a clan!")) {
            return true
        }
        // Ensure player has an active invite to the clan.
        val clan = clanSet.get(args[0])
        if (!session.isTimerInBounds(JOIN, Pair(s.uniqueId, clan))) {
            sendErrorMessage(s, "You do not have an active invite to ${clan.name}.")
            return true
        }
        // Add player to the clan.
        bus.fireEvent(JOIN, clan, agent=s.uniqueId, target=s.uniqueId)
        return true
    }

    /**
     * Promote a member of sender's clan.
     * @return Whether the command was invoked with the correct number of args.
     */
    fun promote() =
        changeRank(PROMOTE, ClanRank.COLEADER, "Target player has reached rank co-leader -- to make them leader, use \"/clans anoint (playername)\" instead!")

    /**
     * Demote a member of sender's clan.
     * @return Whether the command was invoked with the correct number of args.
     */
    fun demote() =
        changeRank(DEMOTE, ClanRank.MEMBER, "Target player has reached rank member -- to remove them from the clan, use \"/clans kick (playername)\".")

    /**
     * Internal implementation for promote, demote. Should not be called outside those contexts.
     */
    fun changeRank(event: ClanEvent, boundaryRank: ClanRank, boundaryMessage: String): Boolean {
        assert(event == DEMOTE || event == PROMOTE)
        // Argument structure validation. 1 arg: target to change rank of.
        if (args.size != 1) {
            return false
        }
        // argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertPlayerNameValid(args[0])) {
            return true
        }

        val ourClan = clanSet.clanOf((s as Player).uniqueId)
        val target = getOfflinePlayer(args[0])
        if (!v.assertPlayerInThisClan(ourClan, target.uniqueId,
                "Player ${args[0]} is not in our clan!")
            || !v.assertRankNotEquals(ourClan, target.uniqueId, boundaryRank, boundaryMessage)
            || !v.assertSenderOutranks(ourClan, target.uniqueId)) {
            return true
        }
        // Fire either promotion or demotion event.
        bus.fireEvent(event, ourClan, agent=s.uniqueId, target=target.uniqueId)
        return true
    }

    /**
     * Crown a player as leader of sender's clan. This entails sender demoting themself to co-leader.*
     * @return whether the command was invoked with the correct number of arguments.
     */
    fun crown(): Boolean {
        // Argument structure validation. Two args: target to anoint, and whether to confirm.
        if (args.size != 1 && args.size != 2) {
            return false
        }
        // argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertPlayerNameValid(args[0])) {
            return true
        }

        val ourClan = clanSet.clanOf((s as Player).uniqueId)
        val target = getOfflinePlayer(args[0])
        if (!v.assertPlayerInThisClan(ourClan, target.uniqueId,
                "Player ${args[0]} is not in our clan!")
            || !v.assertRankEquals(ourClan, s.uniqueId, ClanRank.LEADER, "You must be leader to execute this command!")
            || !v.assertRankEquals(ourClan, target.uniqueId, ClanRank.COLEADER, "The player you crown as leader must be a co-leader first!")) {
            return true
        }

        // Either add a confirm timer, or fire event if present.
        return when(args.size) {
            1 -> {
                session.registerTimer(CORONATE, Pair(s.uniqueId, target.uniqueId), config.confirmTime.toLong())
                sendInfoMessage(s, "You have begun to crown ${args[0]} as new leader of your clan!")
                sendInfoMessage(s, "Enter \"/clans crown ${args[0]} confirm\" within ${config.confirmTime} seconds to confirm this choice.")
                session.startTimer(CORONATE, Pair(s.uniqueId, target.uniqueId))
                true
            }
            2 -> {
                // Minor edge case: since timers are not registered to clan, if all invariants are met while in another clan,
                // we will be able to skip the confirm timer.
                // This is not a problem, though -- to do so would require a lot of change for a limited time.
                if (args[1].lowercase() != "confirm") {
                    return false
                }
                if (v.assertTimerInBounds(session, CORONATE, Pair(s.uniqueId, target.uniqueId), "coronate")) {
                    bus.fireEvent(CORONATE, ourClan, s.uniqueId, target.uniqueId)
                }
                true
            }
            else -> throw IllegalArgumentException("Illegal number of args -- should have been validated earlier.")
        }
    }

    /**
     * Make a player leave their clan.
     * First, the user executes /clans leave. This starts the leave timer.
     * Then the user executes /clans leave confirm.
     * @return Whether the command was invoked with the correct number of arguments.
     */
     fun leave(): Boolean {
        // Argument structure validation: one arg: (optional) confirm
        if (args.size > 1) {
            return false
        }
        // Argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertRankNotEquals(clanSet.clanOf((s as Player).uniqueId),
                s.uniqueId, ClanRank.LEADER,
                "Promote another player to leader first!")) {
            return true
        }

        return when(args.size) {
            // Prompt the user to confirm within the confirmation threshold.
            0 -> {
                sendInfoMessage(s, "Really leave your clan?")
                sendInfoMessage(s, "Type \"/clans leave confirm\" within ${config.confirmTime} seconds to leave.")
                session.registerTimer(LEAVE, s.uniqueId, config.confirmTime.toLong())
                session.startTimer(LEAVE, s.uniqueId)
                true
            }
            // Leave the user's clan.
            1 -> {
                // Argument structure validation: word must be "confirm"
                if (args[0].lowercase() != "confirm") {
                    return false
                }
                // Leave clan if the timer was started.
                if (v.assertTimerInBounds(session, LEAVE, s.uniqueId, "leave")) {
                    bus.fireEvent(LEAVE, clanSet.clanOf(s.uniqueId), agent=s.uniqueId, target=s.uniqueId)
                }
                true
            }
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans leave -- this should have been caught earlier!")
        }
    }

    /**
     * Kick a player from their clan.
     * A user executes /clans kick (player) to make a player they outrank leave.
     * Then that user executes /clans kick (player) confirm to confirm kicking that player.
     *
     * @return Whether the command was invoked with the correct number of arguments.
     */
    fun kick(): Boolean {
        // Argument structure validation. One required argument: player to kick. One optional argument: confirmation.
        if (args.isEmpty() || args.size > 2) {
            return false
        }
        // Argument semantics validation.
        if (!v.assertValidPlayer()
            || !v.assertSenderInAClan(clanSet)
            || !v.assertPlayerNameValid(args[0])) {
            return true
        }
        val clanToKickFrom = clanSet.clanOf((s as Player).uniqueId)
        val playerToKick = getOfflinePlayer(args[0])
        if (!v.assertSenderHasPermission(clanToKickFrom, ClanPermission.KICK)
            || !v.assertPlayerInThisClan(clanToKickFrom, playerToKick.uniqueId, "")
            || !v.assertSenderOutranks(clanToKickFrom, playerToKick.uniqueId)) {
            return true
        }

        return when (args.size) {
            // Prompt the user to confirm the kick within a certain timespan.
            1 -> {
                sendInfoMessage(s, "Really kick ${playerToKick.name} from your clan?")
                sendInfoMessage(s, "Type \"/clans kick ${playerToKick.name} confirm\" within ${config.confirmTime} seconds.")
                // Register confirm timer for this user to kick another.
                session.registerTimer(KICK, Pair(s.uniqueId, playerToKick.uniqueId), config.confirmTime.toLong())
                session.startTimer(KICK, Pair(s.uniqueId, playerToKick.uniqueId))
                true
            }
            2 -> {
                // Argument structure validation: 2nd argument must be confirm.
                if (args[1].lowercase() != "confirm") {
                    return false
                }
                // Kick player from clan if timer started.
                if (v.assertTimerInBounds(session, KICK, Pair(s.uniqueId, playerToKick.uniqueId), "kick")) {
                    bus.fireEvent(KICK, clanSet.clanOf(s.uniqueId), agent=s.uniqueId, target=playerToKick.uniqueId)
                }
                true
            } else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans kick -- this should have been caught earlier!")
        }
    }

    /**
     * Message the sender with a report about the given clan.
     * Format:
     *  Clan $clanName
     *  Leader: $leadername
     *  Members: $members
     **/
    fun info(): Boolean {
        // Argument structure validation. One optional arg: name
        if (args.size > 1) {
            return false // Malformed command -- clan info needs a name!
        }

        // Validate that a clan corresponds to this command/.
        val correspondingClan = when(args.size) {
            // Implicit argument: clanname of sender.
            0 -> if (!v.assertValidPlayer() || !v.assertSenderInAClan(clanSet)) {
                    return true
                 } else clanSet.clanOf((s as Player).uniqueId)
            // Explicit argument: assert it refers to a clan in this list
            1 -> if (!v.assertClanNameInList(clanSet, args[0])) {
                    return true
                 } else clanSet.get(args[0])
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans info -- this should have been caught earlier!")
        }

        // Internal helper: Convert a collection into a single component
        fun componentify(title: String, collection: Iterable<UUID>) = collection.fold(
            Component.text("$title: ", NamedTextColor.RED))
        { others, uuid ->
            others.append(Component.text("${getOfflinePlayer(uuid).name}, ", NamedTextColor.GREEN))
        }

        val header = Component.text("Clan \"${correspondingClan.name}\":", NamedTextColor.GOLD)
        val leaderTitle = Component.text("\nLeader: ", NamedTextColor.RED)
        val leaderName = Component.text("${getOfflinePlayer(correspondingClan.leader()).name}", NamedTextColor.GREEN)
        val coleaders = componentify("\nCo-leaders", correspondingClan.coLeaders())
        val elders = componentify("\nElders", correspondingClan.elders())
        val members = componentify("\nMembers", correspondingClan.members())

        sendPrefixedMessage(s, header.append(leaderTitle).append(leaderName).append(coleaders).append(elders).append(members))
        return true
    }
} // end private class subcommand executor
} // end