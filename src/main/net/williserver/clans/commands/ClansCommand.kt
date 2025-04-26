package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.model.*
import net.williserver.clans.pluginMessagePrefix
import net.williserver.clans.session.SessionManager
import net.williserver.clans.session.ClanEvent
import net.williserver.clans.session.ClanEventBus
import org.bukkit.Bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Base clans command for viewing and modifying clans.
 *
 * @param logger Logging manager
 * @param config Configuration options for this session.
 * @param clanList clan model for this session.
 * @param session Session-specific data, like timers.
 * @param bus Event bus with registered listeners for events in clan lifecycle that may be caused by command invocation.
 *
 * @author Willmo3
 */
class ClansCommand(private val logger: LogHandler,
                   private val config: ClansConfig,
                   private val clanList: ClanList,
                   private val session: SessionManager,
                   private val bus: ClanEventBus
): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean =
        if (args.isNotEmpty()) {
            val subcommand = args[0]
            val args = args.drop(1)
            when(subcommand.lowercase(Locale.getDefault())) {
                "create" -> create(sender, args)
                "disband" -> disband(sender, args)
                "info" -> info(sender, args)
                "invite" -> invite(sender, args)
                "join" -> join(sender, args)
                "help" -> help(sender, args)
                "leave" -> leave(sender, args)
                "list" -> list(sender, args)
                else -> false
            }
        } else help(sender, args.toList())

    /*
     * Subcommands
     */

    /**
     * Usage information for plugin commands.
     *
     * @param s Command sender; will recieve help messages.
     * @param args Args for command. Since this is the help command, should only be one arg.
     */
    private fun help(s: CommandSender, args: List<String>): Boolean {
        // Help should not be invoked with any args beyond the subcommand
        if (args.isNotEmpty()) {
            return false
        }

        val header = prefixedMessage(Component.text("Commands:"))
        val bullet = Component.text("\n- /clans ", NamedTextColor.GOLD)

        fun generateCommandHelp(name: String, text: String)
            = bullet.append(Component.text("$name: ", NamedTextColor.RED).append(Component.text(text, NamedTextColor.GRAY)))

        val help = generateCommandHelp("help", "pull up this help menu.")
        val create = generateCommandHelp("create (name)", "create a new clan under your visionary leadership.")
        val disband = generateCommandHelp("disband", "begin to disband the clan you own.")
        val disbandConfirm = generateCommandHelp("disband confirm", "finish disbanding the clan you own.")
        val info = generateCommandHelp("info (name)", "get information about a clan.")
        val invite = generateCommandHelp("invite (user)", "invite a member to your clan.")
        val join = generateCommandHelp("join (clan name)", "join a clan.")
        val leave = generateCommandHelp("leave", "begin to leave your clan.")
        val leaveConfirm = generateCommandHelp("leave confirm", "finish leaving your clan.")
        val list = generateCommandHelp("list", "output a list of clans.")

        s.sendMessage(header
            .append(help)
            .append(create)
            .append(disband)
            .append(disbandConfirm)
            .append(info)
            .append(invite)
            .append(join)
            .append(leave)
            .append(leaveConfirm)
            .append(list)
        )
        return true
    }

    /**
     * Send the invoker a message containing a list of all clans.
     *
     * @param s Entity invoking commnad; will recieve the list.
     * @param args Args for list; should be only one.
     *
     * TODO: add pages when too many clans.
     */
    private fun list(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation. No args
        if (args.isNotEmpty()) {
            return false
        }

        val listTitle = Component.text("List:", NamedTextColor.AQUA)
        val sortedClans = clanList.clans()
            .sortedBy { it.members().size }
            .fold(Component.text())
                { text, thisClan ->
                    text.append(Component.text("\n- ${thisClan.name}: ", NamedTextColor.GOLD))
                        .append(Component.text("${thisClan.members().size} members", NamedTextColor.RED))
                }

        sendPrefixedMessage(s, listTitle.append(sortedClans))
        return true
    }

    /**
     * Create a new clan.
     *
     * @param s Command sender; must be a player.
     * @param args Arguments to command, such as name.
     */
    private fun create(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation. One arg: new clan name.
        if (args.size > 1) {
            return false
        } else if (args.isEmpty()) {
            // Special case: they forgot to include a name. Send a small reminder rather than a full usage report.
            sendErrorMessage(s, "Your clan needs a name!")
            return true
        }
        // Argument semantics validation
        if (!validPlayer(s)
            || !assertPlayerNotInClan(s, clanList, (s as Player).uniqueId, "You are already in a clan!")
            || !assertValidClanName(s, args[0])
            || !assertUniqueClanName(s, clanList, args[0])) {
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val leader = s.uniqueId
        val newClan = Clan(args[0], leader, arrayListOf(leader))
        bus.fireEvent(ClanEvent.CREATE, newClan, leader)
        broadcastPrefixedMessage("Chief ${s.name} has formed the clan \"${newClan.name}\"!")
        return true
    }

    /**
     * Disband the sending player's clan, if they have the requisite permissions.
     * First, the user must execute /clans disband. This starts a confirmation timer.
     * Then, the user must execute /clans disband confirm within the required time.
     *
     * @param s Command sender; must be a player.
     * @param args Arguments to command. Should be none -- implicit argument is the clan player is a member of.
     */
    private fun disband(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation.
        // Expect /clans disband or /clans disband confirm
        if (args.size > 1) {
            return false
        }
        // Argument semantics validation.
        if (!validPlayer(s)
            || !assertPlayerInClan(s, clanList, (s as Player).uniqueId)
            || !assertHasPermission(s, clanList.playerClan(s.uniqueId), s.uniqueId, ClanPermission.DISBAND)) {
            return true
        }

        // Either initiate a new disband attempt, or confirm one if it's done in time.
        val clan = clanList.playerClan(s.uniqueId)
        return when(args.size) {
            0 -> {
                session.registerTimer(ClanEvent.DISBAND, clan, config.confirmDisbandTime.toLong())
                sendPrefixedMessage(s, "You have begun to disband your clan!", NamedTextColor.LIGHT_PURPLE)
                sendPrefixedMessage(s, "Enter \"/clans disband confirm\" within ${config.confirmDisbandTime} seconds to confirm this choice.", NamedTextColor.LIGHT_PURPLE)
                session.startTimer(ClanEvent.DISBAND, clan)
                true
            }
            1 -> {
                /*
                 * Confirm that the clan should be disbanded after the timer started.
                 * If so, remove the clan from this list.
                 */
                // Argument structure validation: one arg, confirm
                if (args[0].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                }
                if (assertTimerInBounds(s, session, ClanEvent.DISBAND, clan, "disband")) {
                    bus.fireEvent(ClanEvent.DISBAND, clan, s.uniqueId)
                    broadcastPrefixedMessage("Clan \"${clan.name}\" has disbanded!")
                }
                true
            }
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans disband -- this should have been caught earlier!")
        }
    }

    /**
     * Invite a player to our clan.
     *
     * @param s Player who invoked the command.
     * @param args Arguments to command. Should be one: the player invited.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun invite(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation: 1 arg: target
        if (args.size != 1) {
            return false
        }
        // Argument semantics validation.
        if (!validPlayer(s)
            || !assertPlayerInClan(s, clanList, (s as Player).uniqueId)
            || !assertHasPermission(s, clanList.playerClan(s.uniqueId), s.uniqueId, ClanPermission.INVITE)
            || !assertPlayerNameOnline(s, args[0])
            || !assertPlayerNotInClan(s, clanList, getPlayer(args[0])!!.uniqueId,
                "${getPlayer(args[0])!!.name} is already in a clan!")) {
            return true
        }

        val targetClan = clanList.playerClan(s.uniqueId)
        val targetPlayer = getPlayer(args[0])!!
        // Validate that player is not currently waiting on invitation.
        if (session.isTimerInBounds(ClanEvent.JOIN, Pair(s.uniqueId, targetClan))) {
            sendErrorMessage(s, "${targetPlayer.name} is already waiting on an invitation from you.")
            return true
        }

        sendPrefixedMessage(s, "You have invited ${targetPlayer.name} to your clan!", NamedTextColor.GREEN)
        sendPrefixedMessage(targetPlayer, "${s.name} has invited you to clan ${targetClan.name}!", NamedTextColor.GREEN)
        sendPrefixedMessage(targetPlayer, "You have 30 seconds to accept your invitation.", NamedTextColor.GREEN)

        // TODO: configure time for invite.
        // Invitation timer starts immediately after timer registered.
        session.registerTimer(ClanEvent.JOIN, Pair(targetPlayer.uniqueId, targetClan), 30)
        session.startTimer(ClanEvent.JOIN, Pair(targetPlayer.uniqueId, targetClan))
        return true
    }

    /**
     * Join a new clan.
     *
     * @param s Player who invoked the command, will join the target clan if an invitation is active.
     * @param args Arguments to command. Should be one: the name of the clan to join.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun join(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation: 1 arg: target
        if (args.size != 1) {
            return false
        }
        // Argument semantics validation.
        if (!validPlayer(s)
            || !assertClanNameInList(s, clanList, args[0])
            || !assertPlayerNotInClan(s, clanList, (s as Player).uniqueId, "You are already in a clan!")) {
            return true
        }
        // Ensure player has an active invite to the clan.
        val clan = clanList.get(args[0])
        if (!session.isTimerInBounds(ClanEvent.JOIN, Pair(s.uniqueId, clan))) {
            sendErrorMessage(s, "You do not have an active invite to ${clan.name}.")
            return true
        }
        // Add player to the clan.
        bus.fireEvent(ClanEvent.JOIN, clan, s.uniqueId)
        broadcastPrefixedMessage("${s.name} has joined clan ${clan.name}!")
        sendPrefixedMessage(s, "Welcome to clan ${clan.name}!", NamedTextColor.GREEN)
        return true
    }

    /**
     * Make a player leave their clan.
     * First, the user executes /clans leave. This starts the leave timer.
     * Then the user executes /clans leave confirm.
     *
     * @param s Player who invoked the command, will leave their clan.
     * @param args Arguments to command. Should be none.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun leave(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation: one arg: (optional) confirm
        if (args.size > 1) {
            return false
        }
        // Argument semantics validation.
        if (!validPlayer(s)
            || !assertPlayerInClan(s, clanList, (s as Player).uniqueId)
            || !assertPlayerNotLeader(s, clanList.playerClan(s.uniqueId), s.uniqueId)) {
            return true
        }

        return when(args.size) {
            // Prompt the user to confirm within the confirmation threshold.
            0 -> {
                sendPrefixedMessage(s, "Really leave your clan?", NamedTextColor.LIGHT_PURPLE)
                // TODO: configure time to leave
                sendPrefixedMessage(s, "Type \"/clans leave confirm\" within 30 seconds to leave.", NamedTextColor.LIGHT_PURPLE)
                session.registerTimer(ClanEvent.LEAVE, s.uniqueId, 30)
                session.startTimer(ClanEvent.LEAVE, s.uniqueId)
                true
            }
            // Leave the user's clan.
            1 -> {
                // Argument structure validation: second word must be "confirm"
                if (args[0].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                }
                // Leave clan if the timer was started.
                if (assertTimerInBounds(s, session, ClanEvent.LEAVE, s.uniqueId, "leave")) {
                    bus.fireEvent(ClanEvent.LEAVE, clanList.playerClan(s.uniqueId), s.uniqueId)
                    sendPrefixedMessage(s, "You have left your clan.", NamedTextColor.LIGHT_PURPLE)
                }
                true
            }
            else -> throw IllegalStateException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans disband -- this should have been caught earlier!")
        }
    }

    /**
     * Message the sender with a report about the given clan.
     * Format:
     *  Clan $clanName
     *  Leader: $leadername
     *  Members: $members
     *
     * @param s Command sender. Can be any message receiving entity.
     * @param args Arguments to command. Should be one -- the name of the clan.
     */
    private fun info(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation. One arg: name
        if (args.size != 1) {
            return false // Malformed command -- clan info needs a name!
        }
        // Argument semantics validation.
        if (!assertClanNameInList(s, clanList, args[0])) {
            return true
        }

        // If clan present, prepare and send message with information.
        val correspondingClan = clanList.get(args[0])
        val header = Component.text("Clan \"${correspondingClan.name}\":\n", NamedTextColor.GOLD)
        val leaderTitle = Component.text("Leader: ", NamedTextColor.RED)
        val leaderName = Component.text("${getOfflinePlayer(correspondingClan.leader).name}\n", NamedTextColor.GREEN)
        val memberHeader = Component.text("Members: ", NamedTextColor.RED)
        val members = correspondingClan.members().fold(Component.text()) { members, uuid ->
            members.append(Component.text("${getOfflinePlayer(uuid).name}, ", NamedTextColor.GREEN))
        }

        sendPrefixedMessage(s, header.append(leaderTitle).append(leaderName).append(memberHeader).append(members))
        return true
    }
}