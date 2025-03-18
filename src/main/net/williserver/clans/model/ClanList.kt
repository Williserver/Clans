package net.williserver.clans.model

import kotlinx.serialization.Serializable
import net.williserver.clans.LogHandler

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
}