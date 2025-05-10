package net.williserver.clans.integration

import net.luckperms.api.LuckPerms
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PrefixNode
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
        logger.info("$LOG_PREFIX: Loading clans track under name \"$trackName\".")
    }

    /**
     * Construct a listener that creates a group and adds its creator to it.
     */
    fun constructCreateListener(): ClanLifecycleListener = { clan: Clan, _: UUID, creator: UUID ->
        // NOTE: joining load means this must be run as a separate thread!
        luckperms.groupManager.createAndLoadGroup(clan.name).thenApplyAsync { group ->
            luckperms.groupManager.modifyGroup(group.name) {
                it.data().add(PrefixNode.builder("[${clan.prefix}]", 100).build())
            }

            // Add the group to the clans track. Note that this must exist to avoid undefined behavior!
            val track = luckperms.trackManager.getTrack(trackName)!!
            track.insertGroup(group, 0)
            luckperms.trackManager.saveTrack(track)
            logger.info("$LOG_PREFIX: Added group \"${group.name}\" to track \"${track.name}\".")

            // Add the user to the group.
            luckperms.userManager.modifyUser(creator) {
                val groupNode = InheritanceNode.builder(group).build()
                it.data().add(groupNode)
                logger.info("Added user ${it.username} to group \"${group.name}\".")
            }
        }
    }
}