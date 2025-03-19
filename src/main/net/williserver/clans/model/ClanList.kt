package net.williserver.clans.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.williserver.clans.LogHandler
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Persistent serializable form for a list of clans.
 * Invariant: All clans in this list have a unique name.
 *
 * @param clanDataTuples List of clans we represent, in serializable tuple form.
 */
@Serializable
data class ClanListData(val clanDataTuples: List<ClanData>)

/**
 * Wrapper for collection of clans. Can represent main collection or subset.
 *
 * @param logger Logging manager.
 * @param data Serializable list of clans.
 *
 * @author Willmo3
 */
class ClanList(data: ClanListData) {
    // Use ArrayList as type to guarantee mutability.
    private val clans: MutableList<Clan> = data.clanDataTuples.map { Clan(it) } as MutableList<Clan>

    // TODO: check for duplicate clans on initialization.
    /**
     * Convert each clan in the list back into a data tuple.
     *
     * @return data class representing all clans.
     */
    fun asDataTuple(): ClanListData = ClanListData(clans.map { it.asDataTuple() })

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
    writer.write(Json.encodeToString(clanList.asDataTuple()))
    writer.close()
}

/**
 * Read a clanList from a json file at path.
 *
 * @param path Location of file.
 * @return ClanList read from file.
 */
fun readFromFile(path: String): ClanListData {
    if (!File(path).exists()) {
        return ClanListData(ArrayList<ClanData>())
    }
    val reader = FileReader(path)
    val jsonString = reader.readText()
    reader.close()
    return Json.decodeFromString<ClanListData>(jsonString)
}