package net.williserver.clans.integration

import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import net.luckperms.api.node.Node
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PrefixNode
import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.session.ClanLifecycleListener
import net.williserver.clans.session.ClanOptionListener
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

    /*
     * Public helpers
     */

    /**
     * Initiate the luckperms group.
     */
    fun initiateTrack() {
        luckperms.trackManager.createAndLoadTrack(trackName)
        logger.info("$LOG_PREFIX: Loading clans track under name \"$trackName\".")
    }

    /**
     * Update the group of a clan with a new prefix.
     * @param clan Clan whose group should be updated.
     * @param prefix New prefix for the group.
     */
    fun setPrefix(clan: Clan, prefix: String) {
        luckperms.groupManager.modifyGroup(clan.name) {
            // Remove existing group data and replace with new prefix.
            it.data().clear()
            it.data().add(PrefixNode.builder("&7[${Clan.convertToPrefix(prefix)}]", 100).build())
        }
        logger.info("$LOG_PREFIX: Set prefix for group \"${clan.name}\" to \"${prefix}\".")
    }

    /**
     * Update the color of a clan's prefix.
     * @param clan Clan to update prefix of.
     * @param colorCode Character color code
     */
    fun setColor(clan: Clan, colorCode: Char) {
        luckperms.groupManager.modifyGroup(clan.name) {
            // Invariant: node exists
            val oldPrefix: Node = it.data().toCollection().find { node -> node is PrefixNode }!!
            val oldPrefixString = (oldPrefix as PrefixNode).metaValue.substring(2)
            val newPrefixString = "&$colorCode$oldPrefixString"
            it.data().remove(oldPrefix)
            it.data().add(PrefixNode.builder(newPrefixString, 100).build())
            logger.info("$LOG_PREFIX: Set color for group \"${clan.name}\" to \"${colorCode}\".")
        }
    }

    /*
     * Listeners
     */

    /**
     * @return a threaded asynchronous listener that creates a group and adds its creator to it.
     */
    fun constructCreateListener(): ClanLifecycleListener = { clan: Clan, _: UUID, creator: UUID ->
        luckperms.groupManager.createAndLoadGroup(clan.name).thenApplyAsync { group ->
            setPrefix(clan, clan.generatePrefix())

            // Add the group to the clans track. Note that this must exist to avoid undefined behavior!
            val track = luckperms.trackManager.getTrack(trackName)!!
            track.insertGroup(group, 0)
            luckperms.trackManager.saveTrack(track)
            logger.info("$LOG_PREFIX: Added group \"${group.name}\" to track \"${track.name}\".")

            // Add the user to the group.
            addToGroup(group, creator)
        }
    }

    /**
     * @return a threaded asynchronous listener that removes a group.
     */
    fun constructDisbandListener(): ClanLifecycleListener = { clan: Clan, _: UUID, _: UUID ->
        asyncMatchGroup(clan.name,
            ifFound = { group ->
                luckperms.groupManager.deleteGroup(group)
                logger.info("$LOG_PREFIX: deleted group ${clan.name}")
            },
            ifNotFound = { errGroupNotPresent(clan.name) }
        )
    }

    /**
     * @return A threaded asynchronous listener that adds a player to a group.
     */
    fun constructJoinListener(): ClanLifecycleListener = { clan, _: UUID, joiner: UUID ->
        asyncMatchGroup(clan.name,
            ifFound =    { group -> addToGroup(group, joiner) },
            ifNotFound = { errGroupNotPresent(clan.name) }
        )
    }

    /**
     * @return A threaded asynchronous listener that removes a player from a group.
     */
    fun constructLeaveListener(): ClanLifecycleListener = { clan, _: UUID, leaver: UUID ->
        asyncMatchGroup(clan.name,
            ifFound =    { group -> removeFromGroup(group, leaver) },
            ifNotFound = { errGroupNotPresent(clan.name) }
        )
    }

    /**
     * @return a listener that changes the LuckPerms group prefix when the clan prefix option is changed.W
     */
    fun constructPrefixListener(): ClanOptionListener = { clan, agent: UUID, option, value -> setPrefix(clan, value) }

    /*
     * Internal helpers
     */

    /**
     * Control flow function for asynchronously searching for and unwrapping a LuckPerms group on name.
     *
     * @param groupName name of group to search for.
     * @param ifFound Function to invoke if the group is present.
     * @param ifNotFound Function to invoke if the group is not found.
     */
    private fun asyncMatchGroup(groupName: String, ifFound: (group: Group) -> Unit, ifNotFound: () -> Unit) =
        luckperms.groupManager.loadGroup(groupName).thenApplyAsync {
            when (it.isPresent) {
                true -> ifFound(it.get())
                false -> ifNotFound()
            }
        }

    /**
     * @param group LuckPerms group to add player to
     * @param player UUID of player to add to group.
     */
    private fun addToGroup(group: Group, player: UUID) =
        luckperms.userManager.modifyUser(player) {
            val groupNode = InheritanceNode.builder(group).build()
            it.data().add(groupNode)
            logger.info("$LOG_PREFIX: Added user ${it.username} to group \"${group.name}")
        }

    /**
     * @param group LuckPerms group to remove player from.
     * @param player UUID of player to remove from group.
     */
    private fun removeFromGroup(group: Group, player: UUID) =
        luckperms.userManager.modifyUser(player) {
            val groupNode = InheritanceNode.builder(group).build()
            it.data().remove(groupNode)
            logger.info("$LOG_PREFIX: Removed user ${it.username} from group \"${group.name}")
        }

    /**
     * Report logger error for group that is not present.
     * @param name of group that should be present, but isn't.
     */
    private fun errGroupNotPresent(name: String) =
        logger.err("$LOG_PREFIX: Group not found under $name -- was LuckPerms integration turned off at some point?")
}