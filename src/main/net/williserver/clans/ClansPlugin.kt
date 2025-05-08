package net.williserver.clans

import net.williserver.clans.commands.ClansCommand
import net.williserver.clans.commands.ClansTabCompleter
import net.williserver.clans.commands.chat.ChatCommand
import net.williserver.clans.commands.chat.ChatTabCompleter
import net.williserver.clans.integration.*
import net.williserver.clans.model.ClanSet
import net.williserver.clans.model.ClansConfigLoader
import net.williserver.clans.model.clan.*
import net.williserver.clans.model.readFromFile
import net.williserver.clans.model.writeToFile
import net.williserver.clans.session.ClanEvent.*
import net.williserver.clans.session.ClanEventBus
import net.williserver.clans.session.ClanListenerType.*
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
        // TODO: luckperms integration manager.
        // probably want class -- persistent state: luckperms obj.

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
        bus.registerListener(CREATE, MODEL, clanSet.constructCreateListener())
        bus.registerListener(DEMOTE, MODEL, clanSet.constructDemoteListener())
        bus.registerListener(DISBAND, MODEL, clanSet.constructDisbandListener())
        bus.registerListener(JOIN, MODEL, clanSet.constructJoinListener())
        bus.registerListener(LEAVE, MODEL, clanSet.constructLeaveListener())
        bus.registerListener(PROMOTE, MODEL, clanSet.constructPromoteListener())
        bus.registerListener(KICK, MODEL, clanSet.constructKickListener())

        // Session listeners affect temporary data, like expiring invites.
        bus.registerListener(JOIN, SESSION, session.constructDeregisterInviteListener())

        // Messaging listeners send informational messages when events occur.
        bus.registerListener(CREATE, COSMETIC, constructCreateMessageListener())
        bus.registerListener(DEMOTE, COSMETIC, constructDemoteMessageListener())
        bus.registerListener(DISBAND, COSMETIC, constructDisbandMessageListener())
        bus.registerListener(JOIN, COSMETIC, constructJoinMessageListener())
        bus.registerListener(LEAVE, COSMETIC, constructLeaveMessageListener())
        bus.registerListener(PROMOTE, COSMETIC, constructPromoteMessageListener())
        bus.registerListener(KICK, COSMETIC, constructKickMessageListener())

        // Integration listeners connect clans with other features of the plugin.
        if (config.scoreboardTeamsIntegration) {
            bus.registerListener(CREATE, INTEGRATION, constructCreateAddTeamListener())
            bus.registerListener(DISBAND, INTEGRATION, constructDisbandRemoveTeamListener())
            bus.registerListener(JOIN, INTEGRATION, constructJoinTeamListener())
            bus.registerListener(LEAVE, INTEGRATION, constructLeaveTeamListener())
            // From the limited perspective of vanilla teams, a kick is just a player leaving.
            // They lack sufficient context to impose stricter checks and so use the same leave listener.
            bus.registerListener(KICK, INTEGRATION, constructLeaveTeamListener())
        }
        // initiate luckperms integration, if plugin present.
        if (server.pluginManager.isPluginEnabled("LuckPerms")) {
            LuckPermsIntegrator(logger).initiateGroups()
        }

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