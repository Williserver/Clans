package net.williserver.clans.model
import kotlinx.serialization.Serializable
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
 * @param name Name of clan. Must not change, and must be alphanumeric with minus or underscores.
 * @param leader Leader of clan.
 * @param members List of members of clan.
 *
 * @throws IllegalArgumentException if names
 * @author Willmo3
 */
class Clan(val name: String, leader: UUID, private val members: MutableList<UUID>) {
    // Leader should be publicly visible, but for now, we restrict set to internal.
    var leader = leader
        private set

    /**
     * Data tuple constructor for clan. Applies transformations to serializable data to get a canonical clan.
     * @param data Tuple containing strings for clan data.
     */
    constructor(data: ClanData) : this(
        data.name,
        UUID.fromString(data.leader),
        ArrayList(data.members.map { UUID.fromString(it) })
    )

    /*
     * Construction-time assertions, such as ensuring all rank holders are in the clan.
     */
    init {
        if (!validClanName(name)) {
            throw IllegalArgumentException("Invalid clan name!")
        }
        if (leader !in members) {
            throw IllegalArgumentException("Invalid leader UUID (not in clan $name): $leader")
        }
    }

    /*
     * Clan manipulators.
     */

    /**
     * Add a new player to the clan.
     * @param recruit UUID of player to join.
     * @throws IllegalArgumentException If recruit already in a clan
     */
    fun join(recruit: UUID) {
        if (contains(recruit)) {
            throw IllegalArgumentException("UUID $recruit already in clan $name.")
        }
        members += recruit
    }

    // TODO: add way to iterate through members.

    /*
     * Member information getters.
     */

    /**
     * Get the rank of a clan member. Throw an exception if they're not in the clan.
     *
     * @param member UUID of member to get rank for.
     * @return the rank of member in this clan.
     * @throws IllegalArgumentException If the specified member is not in this clan.
     */
    fun rankOfMember(member: UUID): ClanRank =
        if (member == leader) {
            ClanRank.LEADER
        } else if (member in members) {
            ClanRank.MEMBER
        } else {
            throw IllegalArgumentException("Member $member is not in clan $name.")
        }

    /**
     * Check whether a UUID is already in the clan.
     * @param member UUID of member to check.
     */
    operator fun contains(member: UUID): Boolean = member in members

    /*
     * Assorted helpers.
     */

    /**
     * Convert the object back to a tuple of ClanData. Useful for serialization.
     * @return ClanData tuple form of this data.
     */
    fun asDataTuple(): ClanData = ClanData(name, members.map { it.toString() }, leader.toString())

    /**
     * @param other Object to compare against.
     * @return Whether other is equal to this clan; i.e. it is a clan with the same name.
     * Notice this is a loose notion -- clans are assumed to have unique names!
     */
    override fun equals(other: Any?): Boolean = other is Clan && other.name == name
}

/**
 * Check whether a clan name is valid -- i.e., contains only alphanumeric characters, underscore, and dash.
 *
 * @param name Name to validate.
 */
fun validClanName(name: String): Boolean = name.matches("^([a-zA-Z0-9]|-|_)+$".toRegex())