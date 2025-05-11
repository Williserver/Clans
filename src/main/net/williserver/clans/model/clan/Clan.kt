package net.williserver.clans.model.clan
import kotlinx.serialization.Serializable
import net.williserver.clans.pluginMessagePrefix
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.min

/**
 * Persistent data for a given clan.
 *
 * @param name Unique name for the clan
 * @param prefix Displayed prefix for the clan. Default: first three letters of name, in caps.
 * @param members set of member UUIDs
 * @param elders set of elder UUIDS
 * @param coLeaders set of co-leader UUIDs
 * @param leader UUID for player who leads the clan.
 */
@Serializable
data class ClanData(
    val name: String,
    val prefix: String,
    val members: Set<String>,
    val elders: Set<String>,
    val coLeaders: Set<String>,
    val leader: String
)

// TODO: clan member lifecycle listener

/**
 * Mutable model for clan.
 *
 * @param name Name of clan. Must not change, and must be alphanumeric with minus or underscores.
 * @param leader Leader of clan.
 * @param prefix
 * @param members set of members of clan.
 *
 * @throws IllegalArgumentException if there are duplicate members between clans or clans with duplicate names.
 * @author Willmo3
 */
class Clan(
        val name: String,
        leader: UUID,
        prefix: String = name.substring(0, min(3, name.length)).uppercase(),
        private val members: MutableSet<UUID> = mutableSetOf(),
        private val elders: MutableSet<UUID> = mutableSetOf(),
        private val coLeaders: MutableSet<UUID> = mutableSetOf()
    ) {

    // Leader should be publicly visible, but for now, we restrict set to internal.
    var leader = leader
        private set

    // Visible prefix for clan.
    var prefix = prefix
        private set

    /**
     * Data tuple constructor for clan. Applies transformations to serializable data to get a canonical clan.
     * @param data Tuple containing strings for clan data.
     */
    constructor(data: ClanData) : this(
        data.name,
        UUID.fromString(data.leader),
        prefix = data.prefix,
        members = HashSet(data.members.map     { UUID.fromString(it) }),
        elders = HashSet(data.elders.map       { UUID.fromString(it) }),
        coLeaders = HashSet(data.coLeaders.map { UUID.fromString(it) }),
    )

    /*
     * Construction-time assertions, such as ensuring all rank holders are in the clan.
     */
    init {
        if (!validClanName(name)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Invalid clan name!")
        } else if (allClanmates().any { !uniqueRank(it)} ) {
            throw IllegalArgumentException("$pluginMessagePrefix: All clan members must have a unique rank.")
        } else if (prefix.length > 3) {
            throw IllegalArgumentException("$pluginMessagePrefix: Invalid clan prefix!")
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
            throw IllegalArgumentException("$pluginMessagePrefix: UUID $recruit already in clan $name.")
        }
        members += recruit
    }

    /**
     * Remove a member from the clan.
     * @param member Member leaving the clan.
     * @throws IllegalArgumentException If member not in this clan.
     */
    fun leave(member: UUID) {
        execeptionIfNotInClan(member)
        if (member == leader) {
            throw IllegalArgumentException("$pluginMessagePrefix: Member $member leads this clan!")
        }

        when (rankOf(member)) {
            ClanRank.MEMBER -> members -= member
            ClanRank.ELDER -> elders -= member
            ClanRank.COLEADER -> coLeaders -= member
            ClanRank.LEADER -> throw IllegalStateException("Should be unreachable -- are there multiple leaders?")
        }
    }

    /**
     * Promote a member to the next rank.
     * @param promotedMember Member to promote to the next rank.
     * @throws IllegalArgumentException if the rank becomes too high -- co-leaders must be promoted via separate function, since there can only be one leader.
     */
    fun promote(promotedMember: UUID) {
        execeptionIfNotInClan(promotedMember)
        // Add the member to the corresponding set, or throw an error
        when (rankOf(promotedMember)) {
            ClanRank.LEADER -> {
                throw IllegalArgumentException("$pluginMessagePrefix: Cannot promote leader!")
            }
            ClanRank.COLEADER -> {
                throw IllegalArgumentException("$pluginMessagePrefix: Cannot promote co-leader, use anoint!")
            }
            ClanRank.ELDER -> {
                // Members cannot be both elders and co-leaders.
                elders -= promotedMember
                coLeaders += promotedMember
            }
            ClanRank.MEMBER -> {
                members -= promotedMember
                elders += promotedMember
            }
        }
    }

    /**
     * Demote a member to the previous rank.
     * @param demotedMember Member to demote to previous rank.
     * @throws IllegalArgumentException if the rank becomes too low, or attempts to demote the leader.
     */
    fun demote(demotedMember: UUID) {
        execeptionIfNotInClan(demotedMember)
        // Remove the member from the corresponding set, or throw an error.
        when (rankOf(demotedMember)) {
            ClanRank.LEADER -> {
                throw IllegalArgumentException("$pluginMessagePrefix: Cannot demote leader!")
            }
            ClanRank.COLEADER -> {
                coLeaders -= demotedMember
                elders += demotedMember
            }
            ClanRank.ELDER -> {
                elders -= demotedMember
                members += demotedMember
            }
            ClanRank.MEMBER -> {
                throw IllegalArgumentException("$pluginMessagePrefix: Cannot demote member, use kick!")
            }
        }
    }

    /**
     * Promote a co-leader of the clan to leader. The previous leader will become a co-leader.
     * @param newLeader Co-leader who will now become leader.
     * @throws IllegalArgumentException if the provided UUID is not a member who is a co-leader
     */
    fun coronate(newLeader: UUID) {
        execeptionIfNotInClan(newLeader)
        if (rankOf(newLeader) != ClanRank.COLEADER) {
            throw IllegalArgumentException("$pluginMessagePrefix: Only co-leaders can be coronated!")
        }
        coLeaders += leader
        leader = newLeader
    }

    /*
     * Member information getters.
     */

    /**
     * @return the members as an immutable set.
     */
    fun members() = members.toSet()

    /**
     * @return the elders as an immutable set.
     */
    fun elders() = elders.toSet()

    /**
     * @return all co-leaders as an immutable set.
     */
    fun coLeaders() = coLeaders.toSet()

    /**
     * @return all clanmates as an immutable set.
     */
    fun allClanmates() = members() + elders() + coLeaders() + leader

    /**
     * Get the rank of a clan member. Throw an exception if they're not in the clan.
     *
     * @param member UUID of member to get rank for.
     * @return the rank of member in this clan.
     * @throws IllegalArgumentException If the specified member is not in this clan.
     */
    fun rankOf(member: UUID) =
        when (member) {
            leader -> ClanRank.LEADER
            in coLeaders -> ClanRank.COLEADER
            in elders -> ClanRank.ELDER
            in members -> ClanRank.MEMBER
            else -> throw IllegalArgumentException("$pluginMessagePrefix: Member $member is not in clan $name.")
        }

    /**
     * Check whether a UUID is already in the clan.
     * @param member UUID of member to check.
     */
    operator fun contains(member: UUID): Boolean = member in allClanmates()

    /*
     * Assorted helpers.
     */

    /**
     * Convert the object back to a tuple of ClanData. Useful for serialization.
     * @return ClanData tuple form of this data.
     */
    fun asDataTuple() = ClanData(
        name,
        prefix,
        HashSet(members.map   { it.toString() }),
        HashSet(elders.map    { it.toString() }),
        HashSet(coLeaders.map { it.toString() }),
        leader.toString()
    )

    /**
     * @param other Object to compare against.
     * @return Whether other is equal to this clan; i.e. it is a clan with the same name.
     * Notice this is a loose notion -- clans are assumed to have unique names!
     */
    override fun equals(other: Any?): Boolean = other is Clan && other.name == name

    /*
     * Automatically generated hash function.
     */
    override fun hashCode(): Int = name.hashCode()

    /**
     * @param player Player to check rank uniqueness.
     * @return whether the player is only in one rank set.
     * @throws IllegalArgumentException if the player is not in the clan.
     */
    private fun uniqueRank(player: UUID) =
        when (rankOf(player)) {
            ClanRank.LEADER -> player !in members() && player !in elders() && player !in coLeaders()
            ClanRank.COLEADER -> player != leader && player !in members() && player !in elders()
            ClanRank.ELDER -> player != leader && player !in members() && player !in coLeaders()
            ClanRank.MEMBER -> player != leader && player !in elders() && player !in coLeaders()
        }

    /**
     * @param player Player to assert in clan.
     * @throws IllegalArgumentException if the player is not in the clan yet.
     */
    private fun execeptionIfNotInClan(player: UUID) {
        if (!contains(player)) {
            throw IllegalArgumentException("$pluginMessagePrefix: No player with UUID $player found in this clan!")
        }
    }

    /*
     * Static helpers
     */
    companion object {
        /**
         * Check whether a clan name is valid -- i.e., contains only alphanumeric characters, underscore, and dash.
         *
         * @param name Name to validate.
         */
        fun validClanName(name: String): Boolean = name.matches("^([a-zA-Z0-9]|-|_)+$".toRegex())
    }
}