package net.williserver.clans.model

import kotlinx.serialization.Serializable
import net.williserver.clans.LogHandler

/**
 * Persistent serializable form for a list of clans.
 *
 * @param clans List of clans we represent.
 */
@Serializable
data class ClanListData(val clans: List<ClanData>)

/**
 * Wrapper for collection of clans. Can represent main collection or subset.
 *
 * @param logger Logging manager.
 * @param data Serializable list of clans.
 *
 * @author Willmo3
 */
class ClanList(private val logger: LogHandler, data: ClanListData) {
    private var clans: List<Clan> = emptyList()
    init {
        for (clanData in data.clans) {
            clans += Clan(logger, clanData)
        }
    }
}