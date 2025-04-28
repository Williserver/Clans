package net.williserver.clans

import net.williserver.clans.commands.ClansCommand
import net.williserver.clans.commands.ClansTabCompleter
import net.williserver.clans.commands.chat.ChatCommand
import net.williserver.clans.commands.chat.ChatTabCompleter
import net.williserver.clans.model.ClanList
import net.williserver.clans.model.ClansConfigLoader
import net.williserver.clans.model.clan.constructCreateAddTeamListener
import net.williserver.clans.model.clan.constructDisbandRemoveTeamListener
import net.williserver.clans.model.clan.constructJoinTeamListener
import net.williserver.clans.model.clan.constructLeaveTeamListener
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
    private lateinit var clanList: ClanList

    override fun onEnable() {
        // Note: even with an empty config, this is necessary to generate the data directory.
        saveDefaultConfig()
        val config = ClansConfigLoader(logger, config).config
        logger.info("Loaded config")

        // Read base clan list.
        clanList = readFromFile(logger, path)
        logger.info("Initialized clanList")

        // Initiate this session.
        val session = SessionManager()
        logger.info("Initialized session")

        // Register major events in clan lifecycle.
        val bus = ClanEventBus()
        // Model listeners affect persistent data
        bus.registerListener(ClanEvent.CREATE, clanList.constructCreateListener())
        bus.registerListener(ClanEvent.DISBAND, clanList.constructDisbandListener())
        bus.registerListener(ClanEvent.JOIN, clanList.constructJoinListener())
        bus.registerListener(ClanEvent.LEAVE, clanList.constructLeaveListener())
        // Session listeners affect temporary data, like expiring invites.
        bus.registerListener(ClanEvent.JOIN, session.constructDeregisterInviteListener())
        // Integration listeners connect clans with other features of the plugin.
        bus.registerListener(ClanEvent.CREATE, constructCreateAddTeamListener())
        bus.registerListener(ClanEvent.DISBAND, constructDisbandRemoveTeamListener())
        bus.registerListener(ClanEvent.JOIN, constructJoinTeamListener())
        bus.registerListener(ClanEvent.LEAVE, constructLeaveTeamListener())
        logger.info("Registered clan lifecycle listeners")

        // Register commands.
        this.getCommand("cc")!!.setExecutor(ChatCommand(logger, clanList))
        this.getCommand("clans")!!.setExecutor(ClansCommand(logger, config, clanList, session, bus))
        this.getCommand("clans")!!.tabCompleter = ClansTabCompleter(clanList)
        this.getCommand("cc")!!.tabCompleter = ChatTabCompleter()
        logger.info("Registered commands")

        logger.info("Enabled")
    }

    override fun onDisable() {
        // Save model settings.
        writeToFile(logger, path, clanList)

        logger.info("Disabled")
    }
}