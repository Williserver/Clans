package net.williserver.clans.model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.williserver.clans.LogHandler
import java.util.*

// -- name
// -- members
// -- coleaders
// -- leader
// -- elders

/**
 * Persistent data for a given clan.
 *
 * @param name Unique name for the clan
 * @param members List of member UUIDs
 * @param leader UUID for player who leads the clan.
 */
@Serializable
data class ClanData(val name: String, val members: List<@Contextual UUID>, val leader: @Contextual UUID)

/**
 * Mutable model for clan.
 *
 * @param logger Logging utility.
 * @param data Starting data store.
 *
 * @author Willmo3
 */
class Clan(private val logger: LogHandler, data: ClanData) {
    private var leader = data.leader
    private var name = data.name
    private var members = data.members

    init {
        // Membership assertions
        if (leader !in members) {
            logger.err("Invalid leader UUID (not in clan $name): $leader")
            throw IllegalArgumentException("Invalid leader UUID (not in clan $name): $leader")
        }
    }
}