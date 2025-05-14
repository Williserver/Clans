package net.williserver.clans.model

import kotlinx.serialization.json.Json
import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanData
import net.williserver.clans.model.clan.ClanRank
import net.williserver.clans.ClansPlugin.Companion.pluginMessagePrefix
import net.williserver.clans.session.ClanLifecycleListener
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.NoSuchElementException


/**
 * Wrapper for collection of clans. Can represent main collection or subset.
 * Each clan must have a unique name, or an assertion will be thrown!
 *
 * @param data Serializable list of clans.
 *
 * @author Willmo3
 */
class ClanSet(data: Set<ClanData>) {
    private val clans: MutableSet<Clan> = HashSet()

    // Validate that all clan names are unique
    init {
        for (datum in data) {
            val clan = Clan(datum)
            if (clan in clans) {
                throw IllegalArgumentException("$pluginMessagePrefix: Internal error -- Clan with name ${datum.name} already exists.")
            } else if (duplicateMembers(clan)) {
                throw IllegalArgumentException("$pluginMessagePrefix: A member in clan ${datum.name} is already in another clan.")
            }
            clans += clan
        }
    }

    /*
     * ClanList mutators.
     */

    /**
     * Add a new clan to the ClanList.
     *
     * First ensure that no clans with the same name exist,
     * and that the new clan's leader is not already in another clan in this list.
     *
     * Then, add newClan to the list.
     *
     * @param newClan clan to add.
     * @throws IllegalArgumentException if invariants are violated
     */
    fun addClan(newClan: Clan) {
        if (newClan in clans) {
            throw IllegalArgumentException("$pluginMessagePrefix: Internal error -- Clan with name ${newClan.name} already exists.")
        } else if (duplicateMembers(newClan)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Internal error -- Leader already in a clan.")
        }
        clans += newClan
    }

    operator fun plusAssign(newClan: Clan) = addClan(newClan)

    /**
     * Remove a clan from the ClanList. This effectively deletes it.
     *
     * @param clanToRem ove Clan to remove from the list.
     * @throws NoSuchElementException If the clan is not in this list.
     */
    fun removeClan(clanToRemove: Clan) {
        if (clanToRemove !in clans) {
            throw NoSuchElementException("$pluginMessagePrefix: Clan ${clanToRemove.name} is not in this list!")
        }
        clans -= clanToRemove
    }

    operator fun minusAssign(clanToRemove: Clan) = removeClan(clanToRemove)

    /*
     * ClanList player helpers.
     */

    /**
     * Checks whether a player is in a clan on this list.
     * Note: this should be called before using playerClan.
     *
     * @param playerToFind UUID for player.
     * @return Whether the player is in a clan in thist list.
     */
    fun isPlayerInClan(playerToFind: UUID) = clans().any { playerToFind in it }

    /**
     * Find the clan in this list that has player as a member.
     *
     * @throws NullPointerException If no player in clan.
     * @param playerToFind player to search for.
     * @return The clan UUID is a member of, or null if no such clan exists.
     */
    fun clanOf(playerToFind: UUID): Clan = clans().find { playerToFind in it }!!

    /*
     * ClanList clan helpers.
     */

    /**
     * @return immutable list of the clans.
     */
    fun clans() = clans.toSet()

    /**
     * Determine whether there is a clan in this list with the given name.
     *
     * @param name Name of clan to search for
     * @return Whether a clan with the given name is in this list.
     */
    operator fun contains(name: String) = clans().any { it.name == name }

    /**
     * Get the clan with a given name if it's present in this list.
     *
     * @param name Name of clan to search for.
     * @return The found clan, or null if not present.
     * @throws NoSuchElementException if a clan with this name is not present.
     * @throws IllegalArgumentException if multiple clans with this name are present.
     */
    fun get(name: String): Clan {
        if (!contains(name)) {
            throw NoSuchElementException("$pluginMessagePrefix: $name is not a clan, check with `in` helper before calling get.")
        }

        val nameMatches = clans().filter { it.name == name }
        if (nameMatches.size > 1) {
            throw IllegalStateException("$pluginMessagePrefix: Two clans with identical name: $name -- very illegal!")
        }
        return nameMatches.first()
    }

    /**
     * @param other Object to compare against.
     * @return whether other is a clanList with the same clans.
     */
    override fun equals(other: Any?): Boolean = other is ClanSet && other.clans() == clans()

    override fun hashCode() = clans.hashCode()

    /*
     * ClanList serialization helpers.
     */

    /**
     * @return This list as clan data tuples, suitable to be written as JSON.
     */
    fun asDataTuples() = clans.map { it.asDataTuple() }

    /*
     * Clans event listener factories.
     */

    /**
     * Construct coronate listener to change leaders of a clan.
     * @return the listener
     */
    fun constructCoronateListener(): ClanLifecycleListener = { clan, oldLeader, newLeader ->
        exceptionIfClanNotInList(clan)
        if (clan.leader != oldLeader) {
            throw IllegalArgumentException("$pluginMessagePrefix: Player $oldLeader is not leader of this clan.")
        }
        clan.crown(newLeader)
    }

    /**
     * Construct create listener to add a clan to this model.
     * Fire when a new clan is created.
     * @return the listener
     */
    fun constructCreateListener(): ClanLifecycleListener = { clan, _, creator ->
        if (creator != clan.leader) {
            throw IllegalArgumentException("$pluginMessagePrefix: This player is not the leader of the new clan.")
        }
        addClan(clan)
    }

    /**
     * Construct disband listener to remove from this model.
     * Fire when a clan is disbanded.
     * @return the listener
     */
    fun constructDisbandListener(): ClanLifecycleListener = { clan, _, disbander ->
        exceptionIfClanNotInList(clan)
        if (disbander != clan.leader) {
            throw IllegalArgumentException("$pluginMessagePrefix: Disband attempted by non-leader member!")
        }
        removeClan(clan)
    }

    /**
     * Construct demotion listener to demote a player in a clan in this model.
     * Fire when a player is demoted.
     * @return the listener.
     */
    fun constructDemoteListener(): ClanLifecycleListener = { clan, demoter, demotee ->
        exceptionIfClanNotInList(clan)
        if (demoter !in clan || demotee !in clan) {
            throw IllegalArgumentException("$pluginMessagePrefix: Player not in clan.")
        } else if (clan.rankOf(demoter) <= clan.rankOf(demotee)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Demoter does not outrank demoted player.")
        } else if (clan.rankOf(demotee) == ClanRank.MEMBER) {
            throw IllegalArgumentException("$pluginMessagePrefix: Cannot demote below member, use kick instead.")
        }
        clan.demote(demotee)
    }

    /**
     * Construct join listener to add a player to a clan in this model.
     * Fire when a player joins a clan.
     * @return the listener
     */
    fun constructJoinListener(): ClanLifecycleListener = { clan, _, joiner ->
        exceptionIfClanNotInList(clan)
        if (isPlayerInClan(joiner)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Player $joiner is already in a clan.")
        }
        clan.join(joiner)
    }

    /**
     * Construct kick listener to kick a player from clan in this model.
     * Fire when one player kicks another from the clan.
     * @return the listener.
     */
    fun constructKickListener(): ClanLifecycleListener = { clan, agent, kickedPlayer ->
        exceptionIfClanNotInList(clan)
        if (clan.rankOf(kickedPlayer) >= clan.rankOf(agent)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Kick attempted by player with insufficient rank!")
        }
        clan.leave(kickedPlayer)
    }

    /**
     * Construct leave listener to remove a player from clan in this model.
     * Fire when a player leaves a clan.
     * @return The listener.
     */
    fun constructLeaveListener(): ClanLifecycleListener = { clan, _, leaver ->
        exceptionIfClanNotInList(clan)
        clan.leave(leaver)
    }

    /**
     * Construct promotion listener to promote a player in a clan in this model.
     * Fire when a player is promoted.
     * @return the listener.
     */
    fun constructPromoteListener(): ClanLifecycleListener = { clan, promoter, promotee ->
        exceptionIfClanNotInList(clan)
        if (promoter !in clan || promotee !in clan) {
            throw IllegalArgumentException("$pluginMessagePrefix: Player not in clan.")
        } else if (clan.rankOf(promoter) <= clan.rankOf(promotee)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Promoter does not outrank promoted player.")
        }
        clan.promote(promotee)
    }

    /*
     * ClanList internal helpers.
     */

    /**
     * Check whether given clan has members that are also in other clans in this list.
     * In good circumstances, this should never be the case.
     * @param clan Clan to check.
     */
    private fun duplicateMembers(clan: Clan) =
        clans().any {
            otherClan -> otherClan != clan && otherClan.allClanmates().any { it in clan }
        }

    /**
     * @param clan Clan to assert in list
     * @throws NoSuchElementException if the clan is not tracked in this ClanList.
     */
    private fun exceptionIfClanNotInList(clan: Clan) {
        if (clan !in clans()) {
            throw NoSuchElementException("$pluginMessagePrefix: Clan ${clan.name} is not in this list!")
        }
    }
}

/**
 * Write a clanlist in json format to a file at path.
 *
 * @param path Destination for file.
 * @param clanSet clanlist object to write.
 */
fun writeToFile(logger: LogHandler, path: String, clanSet: ClanSet) {
    val writer = FileWriter(path)
    writer.write(Json.encodeToString(clanSet.asDataTuples()))
    logger.info("Saved clan list to $path")
    writer.close()
}

/**
 * Read a clanList from a json file at path.
 *
 * @param path Location of file.
 * @return ClanList read from file.
 */
fun readFromFile(logger: LogHandler, path: String): ClanSet {
    if (!File(path).exists()) {
        logger.info("Found no clan list at $path\nReturning new empty list.")
        return ClanSet(setOf())
    }
    val reader = FileReader(path)
    val jsonString = reader.readText()
    reader.close()
    logger.info("Loaded clan list from $path")
    return ClanSet(Json.decodeFromString<Set<ClanData>>(jsonString))
}