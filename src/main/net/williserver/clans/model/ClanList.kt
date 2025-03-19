package net.williserver.clans.model

import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter


/**
 * Wrapper for collection of clans. Can represent main collection or subset.
 * Each clan must have a unique name, or an assertion will be thrown!
 *
 * @param logger Logging manager.
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
                throw IllegalArgumentException("[CLANS] Internal error -- Clan with name ${datum.name} already exists.")
            }
            clans += clan
        }
    }

    /**
     * @return This list as clan data tuples, suitable to be written as JSON.
     */
    fun asDataTuples(): List<ClanData> = clans.map { it.asDataTuple() }

    /**
     * Determine whether there is a clan in this list with the given name.
     *
     * @param name Name of clan to search for
     * @return Whether a clan with the given name is in this list.
     */
    operator fun contains(name: String): Boolean = clans.any { it.name == name }

    /**
     * Get the clan with a given name if it's present in this list.
     * Otherwise, return null.
     *
     * @param name Name of clan to search for.
     * @return The found clan, or null if not present.
     */
    fun get(name: String): Clan {
        if (!contains(name)) throw NoSuchElementException("$name is not a clan, check with `in` helper before calling get.")

        val nameMatches = clans.filter { it.name == name }
        if (nameMatches.size > 1) {
            throw IllegalStateException("Two clans with identical name: $name -- very illegal!")
        }

        return nameMatches.first()
    }

    /**
     * @param other Object to compare against.
     * @return whether other is a clanList with the same clans.
     */
    override fun equals(other: Any?): Boolean = other is ClanList && other.clans == clans
}

/**
 * Write a clanlist in json format to a file at path.
 *
 * @param path Destination for file.
 * @param clanList clanlist object to write.
 */
fun writeToFile(path: String, clanList: ClanList) {
    val writer = FileWriter(path)
    writer.write(Json.encodeToString(clanList.asDataTuples()))
    writer.close()
}

/**
 * Read a clanList from a json file at path.
 *
 * @param path Location of file.
 * @return ClanList read from file.
 */
fun readFromFile(path: String): ClanList {
    if (!File(path).exists()) {
        return ClanList(listOf())
    }
    val reader = FileReader(path)
    val jsonString = reader.readText()
    reader.close()
    return ClanList(Json.decodeFromString<List<ClanData>>(jsonString))
}