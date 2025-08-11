package net.williserver.clans.model.clan
import kotlinx.serialization.Serializable
import net.williserver.clans.ClansPlugin.Companion.pluginMessagePrefix
import java.util.*
import kotlin.math.min

/**
 * Mutable model for clan.
 *
 * @property name Name of clan. Must not change, and must be alphanumeric with minus or underscores.
 * @property leader Leader of clan, may be changed through the clan's lifecycle.
 * @property members set of clan members.
 * @property elders set of clan elders.
 * @property coLeaders set of clan co-leaders.
 * @property options set of cosmetic clan options.
 *
 * @throws IllegalArgumentException if there are duplicate members between clans or clans with duplicate names.
 * @author Willmo3
 */
@Serializable
class Clan(
        val name: String,
        private var leader: SUUID,
        private val members: MutableSet<SUUID> = mutableSetOf(),
        private val elders: MutableSet<SUUID> = mutableSetOf(),
        private val coLeaders: MutableSet<SUUID> = mutableSetOf(),
        private val options: MutableMap<ClanOption, String> = mutableMapOf(),
    ) {

    /**
     * Perform construction time assertions, such as ensuring that all clans have a valid name.
     * Additionally, initialize all options to defaults, if not provided.
     */
    init {
        if (!validClanName(name)) {
            throw IllegalArgumentException("$pluginMessagePrefix: Invalid clan name!")
        } else if (allClanmates().any { !uniqueRank(it)} ) {
            throw IllegalArgumentException("$pluginMessagePrefix: All clan members must have a unique rank.")
        }

        ClanOption.entries.forEach { option ->
            if (option !in options) {
                options[option] = option.default(this)
            }
        }
    }

    /*
     * Whole-clan data accessors.
     */

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
    fun crown(newLeader: UUID) {
        execeptionIfNotInClan(newLeader)
        if (rankOf(newLeader) != ClanRank.COLEADER) {
            throw IllegalArgumentException("$pluginMessagePrefix: Only co-leaders can be coronated!")
        }
        coLeaders += leader
        coLeaders -= newLeader
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
     * @return the leader of the clan.
     */
    fun leader() = leader

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
     * @return A generated version of the clan's prefix.
     */
    fun generatePrefix() = name.substring(0, min(3, name.length)).uppercase()

    /**
     * @param other Object to compare against.
     * @return Whether other is equal to this clan; i.e. it is a clan with the same name.
     */
    override fun equals(other: Any?) = other is Clan
        && other.name == name
        && other.leader == leader
        && other.members == members
        && other.elders == elders
        && other.coLeaders == coLeaders

    /*
     * Automatically generated hashcode function
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + leader.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + elders.hashCode()
        result = 31 * result + coLeaders.hashCode()
        return result
    }

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

        /**
         * Convert a given string to a prefix code.
         */
        fun convertToPrefix(value: String) = value.substring(0, min(3, value.length)).uppercase()
    }
}