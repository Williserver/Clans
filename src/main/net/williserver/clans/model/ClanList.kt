package net.williserver.clans.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.williserver.clans.LogHandler
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Persistent serializable form for a list of clans.
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
class ClanList(private val logger: LogHandler, data: ClanListData) {
    // Use ArrayList as type to guarantee mutability.
    private val clans: MutableList<Clan> = ArrayList()
    init {
        for (clanData in data.clanDataTuples) {
            clans += Clan(logger, clanData)
        }
    }

    /**
     * Convert each clan in the list back into a data tuple.
     *
     * @return data class representing all clans.
     */
    fun asDataTuple(): ClanListData {
        val data: MutableList<ClanData> = ArrayList(clans.size)
        for (clan in clans) {
            data += clan.asDataTuple()
        }
        return ClanListData(data)
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