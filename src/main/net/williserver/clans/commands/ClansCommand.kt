package net.williserver.clans.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.williserver.clans.LogHandler
import net.williserver.clans.model.*
import net.williserver.clans.pluginMessagePrefix
import org.bukkit.Bukkit.broadcast
import org.bukkit.Bukkit.getOfflinePlayer
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
 *
 * @author Willmo3
 */
class ClansCommand(private val logger: LogHandler,
                   private val config: ClansConfig,
                   private val clanList: ClanList): CommandExecutor {

    /*
     * Map of clans to timers confirming their deletion.
     */
    private val clanConfirmDeleteMap = hashMapOf<Clan, ConfirmTimer>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean =
        if (args.isNotEmpty()) {
            when(args[0].lowercase(Locale.getDefault())) {
                "create" -> create(sender, args)
                "disband" -> disband(sender, args)
                "help" -> help(sender, args)
                "info" -> info(sender, args)
                "list" -> list(sender, args)
                else -> false
            }
        } else help(sender, args)

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

        val help = StringBuilder()
        help.append("$pluginMessagePrefix: Commands:\n")
        help.append("-- /clans help: pull up this help menu.\n")
        help.append("-- /clans create (name): Create a new clan under your visionary leadership.\n")
        help.append("-- /clans disband: Begin to disband the clan you own.\n")
        help.append("-- /clans disband confirm: Finish disbanding the clan you own.")

        s.sendMessage(Component.text(help.toString(), NamedTextColor.GREEN))
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

        val listTitle = Component.text("$pluginMessagePrefix: List:", NamedTextColor.AQUA)
        val sortedClans = clanList.clans()
            .sortedBy { it.members().size }
            .fold(Component.text())
                { text, thisClan ->
                    text.append(Component.text("\n- ${thisClan.name}: ", NamedTextColor.GOLD))
                        .append(Component.text("${thisClan.members().size} members", NamedTextColor.RED))
                }

        s.sendMessage(listTitle.append(sortedClans))
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
            s.sendMessage(Component.text("$pluginMessagePrefix: Your clan needs a name!", NamedTextColor.RED))
            return true
        } else if (s !is Player) {
            s.sendMessage(Component.text("$pluginMessagePrefix: You must be a player to create a clan!", NamedTextColor.RED))
            return true
        }

        // Check if the clan name is valid and unique.
        val name = args[1]
        if (!validClanName(name)) {
            s.sendMessage(Component.text("$pluginMessagePrefix: You have specified an invalid clan name.", NamedTextColor.RED))
            s.sendMessage(Component.text("Use only alphanumeric characters, underscore, and dash.", NamedTextColor.RED))
            return true
        } else if (name in clanList) {
            s.sendMessage(Component.text("$pluginMessagePrefix: The name $name is already taken, try a new one!", NamedTextColor.RED))
            return true
        }

        // Check if the leader is already in a clan.
        val leader = s.uniqueId
        if (clanList.playerInClan(leader)) {
            s.sendMessage(Component.text("$pluginMessagePrefix: You are already in a clan!", NamedTextColor.RED))
            return true
        }

        // Create a new clan with this player as its leader and starting member.
        val newClan = Clan(name, leader, arrayListOf(leader))
        // Insert the clan into the main ClanList.
        clanList.addClan(newClan)

        broadcast(Component.text(
            "$pluginMessagePrefix: Chief ${s.name} has formed the clan \"${newClan.name}\"!",
            NamedTextColor.GREEN))
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
            s.sendMessage(Component.text("$pluginMessagePrefix: You must be a player to disband your clan!", NamedTextColor.RED))
            return true
        }

        // Validate that player has appropriate permissions in some clan.
        if (!clanList.playerInClan(s.uniqueId)) {
            s.sendMessage(Component.text("$pluginMessagePrefix: You must be in a clan!", NamedTextColor.RED))
            return true
        }
        val clan = clanList.playerClan(s.uniqueId)
        if (!clan.rankOfMember(s.uniqueId).hasPermission(ClanPermission.DISBAND)) {
            s.sendMessage(Component.text("$pluginMessagePrefix: You don't have permission to disband this clan!", NamedTextColor.RED))
            return true
        }

        // Either initiate a new disband attempt, or confirm one if it's done in time.
        return when(args.size) {
            1 -> {
                /*
                 * Initiate a disband timer, giving the user 60 seconds to confirm clan deletion.
                 */
                if (clanConfirmDeleteMap[clan] == null) {
                    clanConfirmDeleteMap[clan] = ConfirmTimer(config.confirmDisbandTime.toLong())
                }

                s.sendMessage(Component.text("$pluginMessagePrefix: You have begun to disband your clan!", NamedTextColor.LIGHT_PURPLE))
                s.sendMessage(Component.text("$pluginMessagePrefix: Enter /clans disband confirm within ${config.confirmDisbandTime} seconds to confirm this choice.", NamedTextColor.LIGHT_PURPLE))
                clanConfirmDeleteMap[clan]!!.reset()
                clanConfirmDeleteMap[clan]!!.startTimer()
                true
            }
            2 -> {
                /*
                 * Confirm that the clan should be disbanded after the timer started.
                 * If so, remove the clan from this list.
                 */
                if (args[1].lowercase(Locale.getDefault()) != "confirm") {
                    return false
                } else if (clanConfirmDeleteMap[clan] == null || !clanConfirmDeleteMap[clan]!!.isRunning()) {
                    s.sendMessage(Component.text("$pluginMessagePrefix: You have attempted to delete your clan with \"/clan delete confirm\" before starting the deletion timer!", NamedTextColor.RED))
                    s.sendMessage(Component.text("$pluginMessagePrefix: Please start the timer with \"/clans disband\" first, or ignore this message to keep your clan.", NamedTextColor.RED))
                } else if (!clanConfirmDeleteMap[clan]!!.inBounds()) {
                    s.sendMessage(Component.text("$pluginMessagePrefix: The timer to disband your clan has expired!", NamedTextColor.RED))
                    s.sendMessage(Component.text("$pluginMessagePrefix: To delete your clan, enter \"/clans disband\", or ignore this message to keep your clan.", NamedTextColor.RED))
                } else {
                    // Delete the clan by removing it from the associated ClanList.
                    clanList.removeClan(clan)
                    broadcast(Component.text("$pluginMessagePrefix: Clan \"${clan.name}\" has disbanded!", NamedTextColor.DARK_PURPLE))
                }
                true
            }
            else -> throw IllegalArgumentException("$pluginMessagePrefix: Internal error: Wrong number of arguments to /clan disband -- this should have been caught earlier!")
        }
    }

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
            s.sendMessage(Component.text("$pluginMessagePrefix: Clan \"${args[1]}\" does not exist!", NamedTextColor.RED))
            return true
        }

        // If clan present, prepare and send message with information.
        val correspondingClan = clanList.get(args[1])
        val header = Component.text("$pluginMessagePrefix: Clan \"${correspondingClan.name}\":\n", NamedTextColor.GOLD)
        val leaderTitle = Component.text("Leader: ", NamedTextColor.RED)
        val leaderName = Component.text("${getOfflinePlayer(correspondingClan.leader).name}\n", NamedTextColor.GREEN)
        val memberHeader = Component.text("Members: ", NamedTextColor.RED)
        val members = correspondingClan.members().fold(Component.text()) { members, uuid ->
            members.append(Component.text("${getOfflinePlayer(uuid).name}, ", NamedTextColor.GREEN))
        }

        s.sendMessage(header.append(leaderTitle).append(leaderName).append(memberHeader).append(members))
        return true
    }
}