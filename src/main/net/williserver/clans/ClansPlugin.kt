package net.williserver.clans

import net.williserver.clans.commands.ClansCommand
import net.williserver.clans.commands.ClansTabCompleter
import net.williserver.clans.commands.chat.ChatCommand
import net.williserver.clans.commands.chat.ChatTabCompleter
import net.williserver.clans.model.ClanSet
import net.williserver.clans.model.ClansConfigLoader
import net.williserver.clans.model.clan.*
import net.williserver.clans.model.readFromFile
import net.williserver.clans.model.writeToFile
import net.williserver.clans.session.ClanEvent
import net.williserver.clans.session.ClanEventBus
import net.williserver.clans.session.SessionManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

const val pluginMessagePrefix = "[CLANS]"

/**
 * ClansPlugin. A lightweight predecessor to Polis, designed for user-oriented teams management.
 *
 * @author Willmo3
 */
class ClansPlugin : JavaPlugin() {
    private val logger = LogHandler(super.getLogger())
    // Default data path
    private val path = "$dataFolder${File.separator}clans.json"
    // clanList model.
    private lateinit var clanSet: ClanSet

    override fun onEnable() {
        // Note: even with an empty config, this is necessary to generate the data directory.
        saveDefaultConfig()
        val config = ClansConfigLoader(logger, config).config
        logger.info("Loaded config")

        // Read base clan list.
        clanSet = readFromFile(logger, path)
        logger.info("Initialized clanList")

        // Initiate this session.
        val session = SessionManager()
        logger.info("Initialized session")

        // Register major events in clan lifecycle.
        val bus = ClanEventBus()

        // Model listeners affect persistent data
        bus.registerListener(ClanEvent.CREATE, clanSet.constructCreateListener())
        bus.registerListener(ClanEvent.DISBAND, clanSet.constructDisbandListener())
        bus.registerListener(ClanEvent.JOIN, clanSet.constructJoinListener())
        bus.registerListener(ClanEvent.LEAVE, clanSet.constructLeaveListener())
        bus.registerListener(ClanEvent.PROMOTE, clanSet.constructPromoteListener())
        bus.registerListener(ClanEvent.KICK, clanSet.constructKickListener())

        // Session listeners affect temporary data, like expiring invites.
        bus.registerListener(ClanEvent.JOIN, session.constructDeregisterInviteListener())

        // Messaging listeners send informational messages when events occur.
        bus.registerListener(ClanEvent.CREATE, constructCreateMessageListener())
        bus.registerListener(ClanEvent.DISBAND, constructDisbandMessageListener())
        bus.registerListener(ClanEvent.JOIN, constructJoinMessageListener())
        bus.registerListener(ClanEvent.LEAVE, constructLeaveMessageListener())
        bus.registerListener(ClanEvent.PROMOTE, constructPromoteMessageListener())
        bus.registerListener(ClanEvent.KICK, constructKickMessageListener())

        // Integration listeners connect clans with other features of the plugin.
        if (config.scoreboardTeamsIntegration) {
            bus.registerListener(ClanEvent.CREATE, constructCreateAddTeamListener())
            bus.registerListener(ClanEvent.DISBAND, constructDisbandRemoveTeamListener())
            bus.registerListener(ClanEvent.JOIN, constructJoinTeamListener())
            bus.registerListener(ClanEvent.LEAVE, constructLeaveTeamListener())
            bus.registerListener(ClanEvent.KICK, constructLeaveTeamListener())
        }

        // TODO: add demote lifecycle listeners.
        logger.info("Registered clan lifecycle listeners")

        // Register commands.
        this.getCommand("cc")!!.setExecutor(ChatCommand(clanSet))
        this.getCommand("clans")!!.setExecutor(ClansCommand(clanSet, config, session, bus))
        this.getCommand("cc")!!.tabCompleter = ChatTabCompleter()
        this.getCommand("clans")!!.tabCompleter = ClansTabCompleter(clanSet)
        logger.info("Registered commands")

        logger.info("Enabled")
    }

    override fun onDisable() {
        // Save model settings.
        writeToFile(logger, path, clanSet)

        logger.info("Disabled")
    }
}