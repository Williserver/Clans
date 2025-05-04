package net.williserver.clans.model.clan
import kotlinx.serialization.Serializable
import net.williserver.clans.pluginMessagePrefix
import java.util.*
import kotlin.collections.HashSet

// -- name
// -- members
// -- coleaders
// -- leader
// -- elders

/**
 * Persistent data for a given clan.
 *
 * @param name Unique name for the clan
 * @param coLeaders set of co-leader UUIDs
 * @param leader UUID for player who leads the clan.
 */
@Serializable
data class ClanData(val name: String, val members: Set<String>, val elders: Set<String>, val coLeaders: Set<String>, val leader: String)

/**
 * Mutable model for clan.
 *
 * @param name Name of clan. Must not change, and must be alphanumeric with minus or underscores.
 * @param leader Leader of clan.
 * @param members set of members of clan.
 *
 * @throws IllegalArgumentException if there are duplicate members between clans or clans with duplicate names.
 * @author Willmo3
 */
class Clan(val name: String, leader: UUID,
           private val members: MutableSet<UUID> = mutableSetOf(),
           private val elders: MutableSet<UUID> = mutableSetOf(),
           private val coLeaders: MutableSet<UUID> = mutableSetOf(),) {

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
        if (!contains(member)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Member $member does not exist!")
        } else if (member == leader) {
            throw IllegalArgumentException("$pluginMessagePrefix: Member $member leads this clan!")
        }

        when (rankOfMember(member)) {
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
        if (!contains(promotedMember)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Member $promotedMember does not exist!")
        }
        // Add the member to the corresponding set, or throw an error
        when (rankOfMember(promotedMember)) {
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

    // TODO: demote
    // assert the demoted member is in the list and has not hit the minimum rank.
    // remove them from the set corresponding to their current rank.
    // Add them to the set representing the next rank down.

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
    fun rankOfMember(member: UUID): ClanRank =
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
    fun asDataTuple(): ClanData = ClanData(
        name,
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
        when (rankOfMember(player)) {
            ClanRank.LEADER -> player !in members() && player !in elders() && player !in coLeaders()
            ClanRank.COLEADER -> player != leader && player !in members() && player !in elders()
            ClanRank.ELDER -> player != leader && player !in members() && player !in coLeaders()
            ClanRank.MEMBER -> player != leader && player !in elders() && player !in coLeaders()
        }
}

/*
 * Misc helpers
 */

/**
 * Check whether a clan name is valid -- i.e., contains only alphanumeric characters, underscore, and dash.
 *
 * @param name Name to validate.
 */
fun validClanName(name: String): Boolean = name.matches("^([a-zA-Z0-9]|-|_)+$".toRegex())