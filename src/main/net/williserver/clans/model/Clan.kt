package net.williserver.clans.model
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
data class ClanData(val name: String, val members: List<String>, val leader: String)

/**
 * Mutable model for clan.
 *
 * @param logger Logging utility.
 * @param data Starting data store.
 *
 * @author Willmo3
 */
class Clan(private val logger: LogHandler, data: ClanData) {
    private var name = data.name
    private var leader = UUID.fromString(data.leader)
    // Read serialized UUID strings into clan members.
    private var members = data.members.map { UUID.fromString(it) }

    init {
        // Membership assertions
        // These are internal errors and so may throw exceptions -- a failure here indicates a software defect!
        if (leader !in members) {
            logger.err("Invalid leader UUID (not in clan $name): $leader")
            throw IllegalArgumentException("Invalid leader UUID (not in clan $name): $leader")
        }
    }

    /**
     * Convert the object back to a tuple of ClanData. Useful for serialization.
     * @return ClanData tuple form of this data.
     */
    fun asDataTuple(): ClanData = ClanData(name, members.map { it.toString() }, leader.toString())
    /**
     * @param other Object to compare against.
     * @return Whether other is equal to this clan; i.e. it is a clan with the same name, members, and officers.
     */
    override fun equals(other: Any?): Boolean = other is Clan && other.name == name && other.members == members && other.leader == leader
}