package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.model.*
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanPermission
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
                "kick" -> kick(sender, args)
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

        // Special case: cc is outside of scope of normal clans command
        val chat = Component.text("\n- /cc: ", NamedTextColor.RED).append(Component.text("Send a message to only your clanmates", NamedTextColor.GRAY))
        // All other commands are prefixed by /clans
        val help = generateCommandHelp("help", "pull up this help menu.")
        val create = generateCommandHelp("create (name)", "create a new clan under your visionary leadership.")
        val disband = generateCommandHelp("disband", "begin to disband the clan you own.")
        val disbandConfirm = generateCommandHelp("disband confirm", "finish disbanding the clan you own.")
        val info = generateCommandHelp("info (name)", "get information about a clan.")
        val invite = generateCommandHelp("invite (user)", "invite a member to your clan.")
        val join = generateCommandHelp("join (clan name)", "join a clan.")
        val kick = generateCommandHelp("kick (playername)", "begin to kick a player from your clan.")
        val kickConfirm = generateCommandHelp("kick confirm (playername)", "finish kicking a player from your clan.")
        val leave = generateCommandHelp("leave", "begin to leave your clan.")
        val leaveConfirm = generateCommandHelp("leave confirm", "finish leaving your clan.")
        val list = generateCommandHelp("list", "output a list of clans.")

        s.sendMessage(header
            .append(chat)
            .append(help)
            .append(create)
            .append(disband)
            .append(disbandConfirm)
            .append(info)
            .append(invite)
            .append(join)
            .append(kick)
            .append(kickConfirm)
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
        val sortedClans = clanSet.clans()
            .sortedBy { it.allClanmates().size }
            .fold(Component.text())
                { text, thisClan ->
                    text.append(Component.text("\n- ${thisClan.name}: ", NamedTextColor.GOLD))
                        .append(Component.text("${thisClan.allClanmates().size} clanmates", NamedTextColor.RED))
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
        if (!assertValidPlayer(s)
            || !assertPlayerNotInAClan(s, clanSet, (s as Player).uniqueId, "You are already in a clan!")
            || !assertValidClanName(s, args[0])
            || !assertUniqueClanName(s, clanSet, args[0])) {
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val leader = s.uniqueId
        val newClan = Clan(args[0], leader)
        bus.fireEvent(ClanEvent.CREATE, newClan, agent=leader, target=leader)
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
        if (!assertValidPlayer(s)
            || !assertSenderInAClan(s, clanSet)
            || !assertSenderHasPermission(s, clanSet.playerClan((s as Player).uniqueId), ClanPermission.DISBAND)) {
            return true
        }

        // Either initiate a new disband attempt, or confirm one if it's done in time.
        val clan = clanSet.playerClan(s.uniqueId)
        return when(args.size) {
            0 -> {
                session.registerTimer(ClanEvent.DISBAND, clan, config.confirmTime.toLong())
                sendInfoMessage(s, "You have begun to disband your clan!")
                sendInfoMessage(s, "Enter \"/clans disband confirm\" within ${config.confirmTime} seconds to confirm this choice.")
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
                    bus.fireEvent(ClanEvent.DISBAND, clan, agent=s.uniqueId, target=s.uniqueId)
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
        if (!assertValidPlayer(s)
            || !assertSenderInAClan(s, clanSet)
            || !assertSenderHasPermission(s, clanSet.playerClan((s as Player).uniqueId), ClanPermission.INVITE)
            || !assertPlayerNameOnline(s, args[0])
            || !assertPlayerNotInAClan(s, clanSet, getPlayer(args[0])!!.uniqueId,
                "${getPlayer(args[0])!!.name} is already in a clan!")) {
            return true
        }

        val targetClan = clanSet.playerClan(s.uniqueId)
        val targetPlayer = getPlayer(args[0])!!
        // Validate that player is not currently waiting on invitation.
        if (session.isTimerInBounds(ClanEvent.JOIN, Pair(s.uniqueId, targetClan))) {
            sendErrorMessage(s, "${targetPlayer.name} is already waiting on an invitation from you.")
            return true
        }

        sendCongratsMessage(s, "You have invited ${targetPlayer.name} to your clan!")
        sendCongratsMessage(targetPlayer, "${s.name} has invited you to clan ${targetClan.name}!")
        sendCongratsMessage(targetPlayer, "You have ${config.confirmTime} seconds to accept your invitation.")

        // Invitation timer starts immediately after timer registered.
        session.registerTimer(ClanEvent.JOIN, Pair(targetPlayer.uniqueId, targetClan), config.confirmTime.toLong())
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
        if (!assertValidPlayer(s)
            || !assertClanNameInList(s, clanSet, args[0])
            || !assertPlayerNotInAClan(s, clanSet, (s as Player).uniqueId, "You are already in a clan!")) {
            return true
        }
        // Ensure player has an active invite to the clan.
        val clan = clanSet.get(args[0])
        if (!session.isTimerInBounds(ClanEvent.JOIN, Pair(s.uniqueId, clan))) {
            sendErrorMessage(s, "You do not have an active invite to ${clan.name}.")
            return true
        }
        // Add player to the clan.
        bus.fireEvent(ClanEvent.JOIN, clan, agent=s.uniqueId, target=s.uniqueId)
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
        if (!assertValidPlayer(s)
            || !assertSenderInAClan(s, clanSet)
            || !assertSenderNotLeader(s, clanSet.playerClan((s as Player).uniqueId))) {
            return true
        }

        return when(args.size) {
            // Prompt the user to confirm within the confirmation threshold.
            0 -> {
                sendInfoMessage(s, "Really leave your clan?")
                sendInfoMessage(s, "Type \"/clans leave confirm\" within ${config.confirmTime} seconds to leave.")
                session.registerTimer(ClanEvent.LEAVE, s.uniqueId, config.confirmTime.toLong())
                session.startTimer(ClanEvent.LEAVE, s.uniqueId)
                true
            }
            // Leave the user's clan.
            1 -> {
                // Argument structure validation: word must be "confirm"
                if (args[0].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                }
                // Leave clan if the timer was started.
                if (assertTimerInBounds(s, session, ClanEvent.LEAVE, s.uniqueId, "leave")) {
                    bus.fireEvent(ClanEvent.LEAVE, clanSet.playerClan(s.uniqueId), agent=s.uniqueId, target=s.uniqueId)
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
     * @param s Player sending command.
     * @param args Arguments to command, should be one: name of player to kick.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun kick(s: CommandSender, args: List<String>): Boolean {
        // Argument structure validation. One required argument: player to kick. One optional argument: confirmation.
        if (args.isEmpty() || args.size > 2) {
            return false
        }
        // Argument semantics validation.
        if (!assertValidPlayer(s)
             || !assertSenderInAClan(s, clanSet)
             || ! assertPlayerNameValid(s, args[0])) {
             return true
        }
        val clanToKickFrom = clanSet.playerClan((s as Player).uniqueId)
        val playerToKick = getOfflinePlayer(args[0])
        if (!assertSenderHasPermission(s, clanToKickFrom, ClanPermission.KICK)
             || !assertPlayerInThisClan(s, clanToKickFrom, playerToKick.uniqueId, "")
             || !assertSenderOutranks(s, clanToKickFrom, playerToKick.uniqueId)) {
            return true
        }

        return when (args.size) {
            // Prompt the user to confirm the kick within a certain timespan.
            1 -> {
                sendInfoMessage(s, "Really kick ${playerToKick.name} from your clan?")
                sendInfoMessage(s, "Type \"/clans kick ${playerToKick.name} confirm\" within ${config.confirmTime} seconds.")
                // Register confirm timer for this user to kick another.
                session.registerTimer(ClanEvent.KICK, Pair(s.uniqueId, playerToKick.uniqueId), config.confirmTime.toLong())
                session.startTimer(ClanEvent.KICK, Pair(s.uniqueId, playerToKick.uniqueId))
                true
            }
            2 -> {
                // Argument structure validation: 2nd argument must be confirm.
                if (args[1].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                }
                // Kick player from clan if timer started.
                if (assertTimerInBounds(s, session, ClanEvent.KICK, Pair(s.uniqueId, playerToKick.uniqueId), "kick")) {
                    bus.fireEvent(ClanEvent.KICK, clanSet.playerClan(s.uniqueId), agent=s.uniqueId, target=playerToKick.uniqueId)
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
        if (!assertClanNameInList(s, clanSet, args[0])) {
            return true
        }

        // Convert a collection into a single component
        fun componentify(title: String, collection: Iterable<UUID>) = collection.fold(
            Component.text("$title: ", NamedTextColor.RED))
            { others, uuid ->
                others.append(Component.text("${getOfflinePlayer(uuid).name}, ", NamedTextColor.GREEN))
            }

        // If clan present, prepare and send message with information.
        val correspondingClan = clanSet.get(args[0])
        val header = Component.text("Clan \"${correspondingClan.name}\":", NamedTextColor.GOLD)
        val leaderTitle = Component.text("\nLeader: ", NamedTextColor.RED)
        val leaderName = Component.text("${getOfflinePlayer(correspondingClan.leader).name}", NamedTextColor.GREEN)
        val coleaders = componentify("\nCo-leaders", correspondingClan.coLeaders())
        val elders = componentify("\nElders", correspondingClan.elders())
        val members = componentify("\nMembers", correspondingClan.members())

        sendPrefixedMessage(s, header.append(leaderTitle).append(leaderName).append(coleaders).append(elders).append(members))
        return true
    }
}