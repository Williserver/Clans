package net.williserver.clans.integration

import net.luckperms.api.LuckPerms
import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.session.ClanLifecycleListener
import org.bukkit.Bukkit
import java.util.*

/**
 * Integrate with LuckPerms group tracks, if the plugin is present.
 *
 * @author Willmo3
 */
class LuckPermsIntegrator(private val logger: LogHandler, private val trackName: String) {
    private val luckperms = Bukkit.getServer().servicesManager.load(LuckPerms::class.java)!!
    companion object {
        const val LOG_PREFIX = "[LuckPerms Integration]"
    }

    /**
     * Initiate the luckperms group.
     */
    fun initiateTrack() {
        luckperms.trackManager.createAndLoadTrack(trackName)
        logger.info("$LOG_PREFIX: Loading clans track.")
    }

    /**
     * Generate a listener to register a new rank when a clan is created.
     */
    fun generateCreateListener(): ClanLifecycleListener = { clan: Clan, uuid: UUID, uuid1: UUID ->

    }
}