package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.model.*
import net.williserver.clans.pluginMessagePrefix
import net.williserver.clans.session.SessionManager
import net.williserver.clans.lifecycle.ClanEvent
import net.williserver.clans.lifecycle.ClanEventBus
import net.williserver.clans.session.invite.ClanInvitation
import net.williserver.clans.session.invite.TimedClanInvitation
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
            when(args[0].lowercase(Locale.getDefault())) {
                "create" -> create(sender, args)
                "disband" -> disband(sender, args)
                "info" -> info(sender, args)
                "invite" -> invite(sender, args)
                "join" -> join(sender, args)
                "help" -> help(sender, args)
                "list" -> list(sender, args)
                else -> false
            }
        } else help(sender, args)

    /*
     * Subcommands
     */

    /**
     * Usage information for plugin commands.
     *
     * @param s Command sender; will recieve help messages.
     * @param args Args for command. Since this is the help command, should only be one arg.
     */
    private fun help(s: CommandSender, args: Array<out String>): Boolean {
        // Help may be invoked with zero or one arguments.
        if (args.size > 1) {
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
        val list = generateCommandHelp("list", "output a list of clans.")

        s.sendMessage(header
            .append(help)
            .append(create)
            .append(disband)
            .append(disbandConfirm)
            .append(info)
            .append(invite)
            .append(join)
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
    private fun list(s: CommandSender, args: Array<out String>): Boolean {
        // List should only be invoked with one argument.
        if (args.size > 1) {
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
    private fun create(s: CommandSender, args: Array<String>): Boolean {
        // Initial validation.
        if (args.size > 2) {
            // Returing false here -- this is the only place where the server should send a usage message!
            return false
        } else if (args.size < 2) {
            sendErrorMessage(s, "Your clan needs a name!")
            return true
        } else if (s !is Player) {
            sendErrorMessage(s, "You must be a player to create a clan!")
            return true
        }

        // Check if the clan name is valid and unique.
        val name = args[1]
        if (!validClanName(name)) {
            sendErrorMessage(s, "You have specified an invalid clan name.")
            sendErrorMessage(s, "Use only alphanumeric characters, underscore, and dash.")
            return true
        } else if (name in clanList) {
            sendErrorMessage(s, "The name $name is already taken, try a new one!")
            return true
        }

        // Check if the leader is already in a clan.
        val leader = s.uniqueId
        if (clanList.playerInClan(leader)) {
            sendErrorMessage(s, "You are already in a clan!")
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val newClan = Clan(name, leader, arrayListOf(leader))
        bus.fireEvent(ClanEvent.CREATE, clanList, newClan, leader)

        broadcastPrefixedMessage("Chief ${s.name} has formed the clan \"${newClan.name}\"!", NamedTextColor.GREEN)
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
    private fun disband(s: CommandSender, args: Array<out String>): Boolean {
        // API validation
        if (args.size != 1 && args.size != 2) {
            return false
        } else if (s !is Player) {
            sendErrorMessage(s, "You must be a player to disband your clan!")
            return true
        }

        // Validate that player has appropriate permissions in some clan.
        if (!clanList.playerInClan(s.uniqueId)) {
            sendErrorMessage(s, "You must be in a clan!")
            return true
        }
        val clan = clanList.playerClan(s.uniqueId)
        if (!clan.rankOfMember(s.uniqueId).hasPermission(ClanPermission.DISBAND)) {
            sendErrorMessage(s, "You don't have permission to disband this clan!")
            return true
        }

        // Either initiate a new disband attempt, or confirm one if it's done in time.
        return when(args.size) {
            1 -> {
                session.registerClanDisbandTimer(clan, config.confirmDisbandTime.toLong())
                sendPrefixedMessage(s, "You have begun to disband your clan!", NamedTextColor.LIGHT_PURPLE)
                sendPrefixedMessage(s, "Enter \"/clans disband confirm\" within ${config.confirmDisbandTime} seconds to confirm this choice.", NamedTextColor.LIGHT_PURPLE)
                session.startClanDisbandTimer(clan)
                true
            }
            2 -> {
                /*
                 * Confirm that the clan should be disbanded after the timer started.
                 * If so, remove the clan from this list.
                 */
                if (args[1].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                } else if (!session.clanDisbandTimerRegistered(clan)) {
                    sendErrorMessage(s, "You have attempted to delete your clan with \"/clans disband confirm\" before starting the deletion timer!")
                    sendErrorMessage(s, "Please start the timer with \"/clans disband\" first, or ignore this message to keep your clan.")
                } else if (!session.checkDisbandTimerInBounds(clan)) {
                    sendErrorMessage(s, "The timer to disband your clan has expired!")
                    sendErrorMessage(s, "To delete your clan, enter \"/clans disband\", or ignore this message to keep your clan.")
                } else {
                    // Confirm the deletion of the clan, firing all relevant listeners.
                    bus.fireEvent(ClanEvent.DISBAND, clanList, clan, s.uniqueId)
                    broadcastPrefixedMessage("Clan \"${clan.name}\" has disbanded!", NamedTextColor.DARK_PURPLE)
                }
                true
            }
            else -> throw IllegalArgumentException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clans disband -- this should have been caught earlier!")
        }
    }

    /**
     * Invite a player to our clan.
     *
     * @param s Player who invoked the command.
     * @param args Arguments to command. Should be one: the player invited.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun invite(s: CommandSender, args: Array<out String>): Boolean {
        // API validation: 2 args (subcommand, target)
        if (args.size != 2) {
            return false
        } else if (s !is Player) {
            sendErrorMessage(s, "You must be a player to invite members to your clan!")
            return true
        } else if (!clanList.playerInClan(s.uniqueId)) {
            sendErrorMessage(s, "You must be in a clan!")
            return true
        }

        // Ensure player has correct permissions in their clan.
        val targetClan = clanList.playerClan(s.uniqueId)
        if (!targetClan.rankOfMember(s.uniqueId).hasPermission(ClanPermission.INVITE)) {
            sendErrorMessage(s, "You do not have permission to invite new members to the clan!")
            return true
        }

        // Find the UUID of the target player and validate that they are an eligible bachelor.
        val targetPlayer = getPlayer(args[1])
        if (targetPlayer == null) {
            sendErrorMessage(s, "Player \"${args[1]}\" not found -- are they online?")
            return true
        } else if (clanList.playerInClan(targetPlayer.uniqueId)) {
            sendErrorMessage(s, "${targetPlayer.name} is already in a clan!")
            return true
        } else if (session.activeClanInvite(targetPlayer.uniqueId, targetClan)) {
            sendErrorMessage(s, "${targetPlayer.name} is already waiting on an invitation from you.")
            return true
        }

        sendPrefixedMessage(s, "You have invited ${s.name} to your clan!", NamedTextColor.GREEN)
        sendPrefixedMessage(targetPlayer, "${s.name} has invited you to clan ${targetClan.name}!", NamedTextColor.GREEN)
        sendPrefixedMessage(targetPlayer, "You have 30 seconds to accept your invitation.", NamedTextColor.GREEN)
        // TODO: configure time for invite.
        session.addClanInvite(TimedClanInvitation(targetPlayer.uniqueId, targetClan, 30))
        return true
    }

    /**
     * Join a new clan.
     *
     * @param s Player who invoked the command, will join the target clan if an invitation is active.
     * @param args Arguments to command. Should be one: the name of the clan to join.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun join(s: CommandSender, args: Array<out String>): Boolean {
        // API validation: 2 args (subcommand, target)
        if (args.size != 2) {
            return false
        }
        // Ensure player and clan are eligible.
        if (s !is Player) {
            sendErrorMessage(s, "You must be a player to join a clan!")
            return true
        } else if (clanList.playerInClan(s.uniqueId)) {
            sendErrorMessage(s, "You are already in a clan!")
            return true
        } else if (args[1] !in clanList) {
            sendErrorMessage(s, "Clan ${args[1]} not found!")
            return true
        }
        // Ensure player has an active invite to the clan.
        val clan = clanList.get(args[1])
        if (!session.activeClanInvite(s.uniqueId, clan)) {
            sendErrorMessage(s, "You do not have an active invite to ${clan.name}.")
            return true
        }
        // Add player to the clan.
        bus.fireEvent(ClanEvent.JOIN, clanList, clan, s.uniqueId)
        // TODO: setup listener to remove any active invites when you join.
        broadcastPrefixedMessage("${s.name} has joined clan ${clan.name}!", NamedTextColor.DARK_PURPLE)
        sendPrefixedMessage(s, "Welcome to clan ${clan.name}!", NamedTextColor.GREEN)
        return true
    }

    /*

    /**
     * Leave our clan.
     *
     * @param s Player who invoked the command, will leave their clan.
     * @param args Arguments to command. Should be none.
     * @return Whether the command was invoked with the correct number of arguments.
     */
    private fun leave(s: CommandSender, args: Array<out String>): Boolean {
        true
    }
    */

    /**
     * Message the sender with a report about the given clan.
     * Format:
     *  Clan $clanName:
     *  Leader: $leadername
     *  Members: $members
     *
     * @param s Command sender. Can be any message receiving entity.
     * @param args Arguments to command. Should be one -- the name of the clan.
     */
    private fun info(s: CommandSender, args: Array<out String>): Boolean {
        // Ensure arguments conform with expected API
        if (args.size != 2) {
            return false // Malformed command -- clan info needs a name!
        }

        // Validate that clan present in list.
        if (args[1] !in clanList) {
            sendErrorMessage(s, "Clan \"${args[1]}\" does not exist!")
            return true
        }

        // If clan present, prepare and send message with information.
        val correspondingClan = clanList.get(args[1])
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

    /*
     * Internal helpers
     */

    /**
     * Append a message prefix component onto a message component.
     *
     * @param message Message to append the plugin prefix to.
     * @return A new component with the plugin prefix appended.
     */
    private fun prefixedMessage(message: Component)
        = Component.text("$pluginMessagePrefix: ", NamedTextColor.GOLD).append(message)

    /**
     * Convert a string into a colored message component and prefix it.
     *
     * @param message Message to format.
     * @param color Color for message.
     * @return A component of the prefixed message.
     */
    private fun prefixedMessage(message: String, color: NamedTextColor)
        = prefixedMessage(Component.text(message, color))

    /**
     * Send a prefixed message component to a target.
     *
     * @param target Entity to receive message.
     * @param message Message to format and send to target.
     */
    private fun sendPrefixedMessage(target: CommandSender, message: Component)
        = target.sendMessage(prefixedMessage(message))

    /**
     * Send a colored string message to a target.
     *
     * @param target Entity to receive message.
     * @param message Message to format and send to target.
     * @param color kyori component color to send message in.
     */
    private fun sendPrefixedMessage(target: CommandSender, message: String, color: NamedTextColor)
        = target.sendMessage(prefixedMessage(message, color))

    /**
     * Send a red-colored error message to a target.
     *
     * @param target Entity to receive error.
     * @param message Error to format and send to target.
     */
    private fun sendErrorMessage(target: CommandSender, message: String)
        = target.sendMessage(prefixedMessage(Component.text(message, NamedTextColor.RED)))

    /**
     * Broadcast a colored string message.
     *
     * @param message Message to format and broadcast.
     * @param color kyori component color to broadcast message in.
     */
    private fun broadcastPrefixedMessage(message: String, color: NamedTextColor)
        = broadcast(prefixedMessage(Component.text(message, color)))
}