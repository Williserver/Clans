package net.williserver.clans

import net.williserver.clans.commands.base.ClansCommand
import net.williserver.clans.commands.base.ClansTabCompleter
import net.williserver.clans.commands.chat.ChatCommand
import net.williserver.clans.commands.chat.ChatTabCompleter
import net.williserver.clans.integration.*
import net.williserver.clans.model.*
import net.williserver.clans.model.clan.*
import net.williserver.clans.session.ClanEvent.*
import net.williserver.clans.session.ClanEventBus
import net.williserver.clans.session.ClanListenerType.*
import net.williserver.clans.session.SessionManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


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
    // event bus.
    private lateinit var bus: ClanEventBus

    override fun onEnable() {
        /* Read base config */
        saveDefaultConfig() // If file deleted, restore defaults
        val clansConfig = ClansConfigLoader(logger, config).config
        logger.info("Loaded config.")

        /* Initialize ClanList. */
        clanSet = readFromFile(logger, path)
        logger.info("Initialized clanList.")

        /* Initiate clans session. */
        val session = SessionManager()
        logger.info("Initialized session.")

        /* Begin registering listeners for major events in clan lifecycle. */
        bus = ClanEventBus()
        /*
         * MODEL LISTENERS affect persistent data
         */
        registerCoreModelListeners()
        logger.info("Registered clans data model listeners.")
        /*
         * SESSION LISTENERS affect temporary data, like expiring invites.
         */
        registerSessionListeners(session)
        logger.info("Registered clans session listeners.")
        /*
         * COSMETIC LISTENERS send informational messages when events occur.
         */
        registerCosmeticListeners()
        logger.info("Registered clans cosmetic listeners.")
        /*
         * INTEGRATION LISTENERS connect clans with other plugins.
         */
        if (clansConfig.scoreboardTeamsIntegration) {
            logger.info("Scoreboard teams integration enabled, registering teams listeners.")
            registerScoreboardTeamsIntegrationListeners()
        } else {
            logger.info("Scoreboard teams integration disabled, not registering teams listeners.")
        }
        // Luckperms integration listeners.
        if (clansConfig.luckpermsIntegration && server.pluginManager.isPluginEnabled("LuckPerms")) {
            logger.info("LuckPerms integration enabled, registering LP listeners.")
            registerLuckPermsIntegrationListeners(clansConfig)
        } else {
            logger.info("LuckPerms integration disabled, not registering LP listeners.")
        }
        logger.info("Registered integration listeners.")

        logger.info("Finished registering clan lifecycle listeners.")

        /* Register commands */
        this.getCommand("cc")!!.setExecutor(ChatCommand(clanSet))
        this.getCommand("clans")!!.setExecutor(ClansCommand(clanSet, clansConfig, session, bus))
        this.getCommand("cc")!!.tabCompleter = ChatTabCompleter()
        this.getCommand("clans")!!.tabCompleter = ClansTabCompleter(clanSet)
        logger.info("Registered commands.")

        /* Finish enabling */
        logger.info("Enabled.")
    }

    override fun onDisable() {
        // Save model settings.
        writeToFile(logger, path, clanSet)

        logger.info("Disabled.")
    }

    // ***** LISTENER REGISTRATION HELPERS ***** //

    /**
     * Register listeners that affect the underlying persistent clans data model.
     *
     * These listeners will be run first, since the persistent data model has stringent data integrity guarantees
     * that should be met before other listeners fire.
     */
    private fun registerCoreModelListeners() {
        bus.registerListener(CORONATE, MODEL, clanSet.constructCoronateListener())
        bus.registerListener(CREATE, MODEL, clanSet.constructCreateListener())
        bus.registerListener(DEMOTE, MODEL, clanSet.constructDemoteListener())
        bus.registerListener(DISBAND, MODEL, clanSet.constructDisbandListener())
        bus.registerListener(JOIN, MODEL, clanSet.constructJoinListener())
        bus.registerListener(LEAVE, MODEL, clanSet.constructLeaveListener())
        bus.registerListener(PROMOTE, MODEL, clanSet.constructPromoteListener())
        bus.registerListener(KICK, MODEL, clanSet.constructKickListener())
    }

    /**
     * Register listeners that affect session-level data, like timers.
     * Since this data will persist only for a single session, these listeners are run third.
     */
    private fun registerSessionListeners(session: SessionManager) {
        bus.registerListener(JOIN, SESSION, session.constructDeregisterInviteListener())
    }

    /**
     * Register listeners that message users when changes occur in the clan lifecycle.
     *
     * These listeners will be run last, since they should have no persistent data impact.
     */
    private fun registerCosmeticListeners() {
        bus.registerListener(CORONATE, COSMETIC, constructCrownMessageListener())
        bus.registerListener(CREATE, COSMETIC, constructCreateMessageListener())
        bus.registerListener(DEMOTE, COSMETIC, constructDemoteMessageListener())
        bus.registerListener(DISBAND, COSMETIC, constructDisbandMessageListener())
        bus.registerListener(JOIN, COSMETIC, constructJoinMessageListener())
        bus.registerListener(LEAVE, COSMETIC, constructLeaveMessageListener())
        bus.registerListener(PROMOTE, COSMETIC, constructPromoteMessageListener())
        bus.registerListener(KICK, COSMETIC, constructKickMessageListener())
    }

    /**
     * Register listeners that map clans to vanilla scoreboard teams.
     *
     * This class of listener is run second, since scoreboard teams are persistent.
     */
    private fun registerScoreboardTeamsIntegrationListeners() {
        val teamIntegrator = ScoreboardTeamIntegrator(logger)
        bus.registerListener(CREATE, INTEGRATION, teamIntegrator.constructCreateAddTeamListener())
        bus.registerListener(DISBAND, INTEGRATION, teamIntegrator.constructDisbandRemoveTeamListener())
        bus.registerListener(JOIN, INTEGRATION, teamIntegrator.constructJoinTeamListener())
        bus.registerListener(LEAVE, INTEGRATION, teamIntegrator.constructLeaveTeamListener())
        // From the limited perspective of vanilla teams, a kick is just a player leaving.
        bus.registerListener(KICK, INTEGRATION, teamIntegrator.constructLeaveTeamListener())
    }

    /**
     * Register listeners that map clans to LuckPerms groups.
     *
     * This class of listener is run second, since LuckPerms groups are persistent.
     */
    private fun registerLuckPermsIntegrationListeners(clansConfig: ClansConfig) {
        val integrator = LuckPermsIntegrator(logger, clansConfig.luckPermsTrackName)
        integrator.initiateTrack()
        bus.registerListener(CREATE, INTEGRATION, integrator.constructCreateListener())
        bus.registerListener(DISBAND, INTEGRATION, integrator.constructDisbandListener())
        bus.registerListener(JOIN, INTEGRATION, integrator.constructJoinListener())
        bus.registerListener(LEAVE, INTEGRATION, integrator.constructLeaveListener())
        // From the perspective of LuckPerms, a kick is just a player leaving.
        bus.registerListener(KICK, INTEGRATION, integrator.constructLeaveListener())
    }

    /**
     * Static state
     */
    companion object {
        const val pluginMessagePrefix = "[CLANS]"
    }
}