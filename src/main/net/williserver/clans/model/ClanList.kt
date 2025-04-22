package net.williserver.clans.model

import kotlinx.serialization.json.Json
import net.williserver.clans.LogHandler
import net.williserver.clans.pluginMessagePrefix
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList


/**
 * Wrapper for collection of clans. Can represent main collection or subset.
 * Each clan must have a unique name, or an assertion will be thrown!
 *
 * @param data Serializable list of clans.
 *
 * @author Willmo3
 */
class ClanList(data: List<ClanData>) {
    private val clans: MutableList<Clan> = ArrayList()

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

    /**
     * Remove a clan from the ClanList. This effectively deletes it.
     *
     * @param clanToRemove Clan to remove from the list.
     * @throws NoSuchElementException If the clan is not in this list.
     */
    fun removeClan(clanToRemove: Clan) {
        if (clanToRemove !in clans) {
            throw NoSuchElementException("$pluginMessagePrefix: Clan ${clanToRemove.name} is not in this list!")
        }
        clans -= clanToRemove
    }

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
    fun playerInClan(playerToFind: UUID) = clans.any { playerToFind in it }

    /**
     * Find the clan in this list that has player as a member.
     *
     * @throws NullPointerException If no player in clan.
     * @param UUID player to search for.
     * @return The clan UUID is a member of, or null if no such clan exists.
     */
    fun playerClan(playerToFind: UUID): Clan = clans.find { playerToFind in it }!!

    /*
     * ClanList clan helpers.
     */

    /**
     * @return immutable list of the clans.
     */
    fun clans() = clans.toList()

    /**
     * Determine whether there is a clan in this list with the given name.
     *
     * @param name Name of clan to search for
     * @return Whether a clan with the given name is in this list.
     */
    operator fun contains(name: String): Boolean = clans.any { it.name == name }

    /**
     * Get the clan with a given name if it's present in this list.
     *
     * @param name Name of clan to search for.
     * @return The found clan, or null if not present.
     * @throws NoSuchElementException if a clan with this name is not present.
     */
    fun get(name: String): Clan {
        if (!contains(name)) {
            throw NoSuchElementException("$pluginMessagePrefix: $name is not a clan, check with `in` helper before calling get.")
        }

        val nameMatches = clans.filter { it.name == name }
        if (nameMatches.size > 1) {
            throw IllegalStateException("$pluginMessagePrefix: Two clans with identical name: $name -- very illegal!")
        }
        return nameMatches.first()
    }

    /**
     * @param other Object to compare against.
     * @return whether other is a clanList with the same clans.
     */
    override fun equals(other: Any?): Boolean = other is ClanList && other.clans == clans

    /*
     * ClanList serialization helpers.
     */

    /**
     * @return This list as clan data tuples, suitable to be written as JSON.
     */
    fun asDataTuples(): List<ClanData> = clans.map { it.asDataTuple() }

    /*
     * ClanList internal helpers.
     */

    /**
     * Check whether given clan has members that are also in other clans in this list.
     * In good circumstances, this should never be the case.
     */
    private fun duplicateMembers(clan: Clan): Boolean =
        clans.any {
            otherClan -> otherClan != clan && otherClan.members().any { it in clan }
        }
}

/**
 * Write a clanlist in json format to a file at path.
 *
 * @param path Destination for file.
 * @param clanList clanlist object to write.
 */
fun writeToFile(logger: LogHandler, path: String, clanList: ClanList) {
    val writer = FileWriter(path)
    writer.write(Json.encodeToString(clanList.asDataTuples()))
    logger.info("Saved clan list to $path")
    writer.close()
}

/**
 * Read a clanList from a json file at path.
 *
 * @param path Location of file.
 * @return ClanList read from file.
 */
fun readFromFile(logger: LogHandler, path: String): ClanList {
    if (!File(path).exists()) {
        logger.info("Found no clan list at $path\nReturning new empty list.")
        return ClanList(listOf())
    }
    val reader = FileReader(path)
    val jsonString = reader.readText()
    reader.close()
    logger.info("Loaded clan list from $path")
    return ClanList(Json.decodeFromString<List<ClanData>>(jsonString))
}