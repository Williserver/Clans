package net.williserver.clans.model

/**
 * Named permissions for important clan actions.
 * @author Willmo3
 */
enum class ClanPermission {
    DISBAND,
}

/**
 * Represents a rank in a clan -- effectively, a named wrapper around a number representing relative authority.
 *
 * @param rank Digit precedence for rank.
 * @author Willmo3
 */
enum class ClanRank(private val rank: UInt, private val permissions: Array<ClanPermission>): Comparable<ClanRank> {
    MEMBER(0u, arrayOf()),
    ELDER(1u, arrayOf()),
    COLEADER(2u, arrayOf()),
    LEADER(3u, arrayOf(ClanPermission.DISBAND));

    /**
     * @return Whether this ClanRank has the specified permission in its permission slist.
     */
    fun hasPermission(permission: ClanPermission) = permission in permissions

    /**
     * Return the next rank (i.e. that this would be promoted to)
     * Otherwise, if this is leader, simply return that.
     *
     * @return Promoted rank.
     */
    fun nextRank(): ClanRank =
        if (this < LEADER) {
            entries[ordinal + 1]
        } else {
            entries[ordinal]
        }

    /**
     * Return the previous rank (i.e. that this would be demoted to).
     * If this is member, do nothing -- would simply kick.
     *
     * @return Demoted rank.
     */
    fun previousRank(): ClanRank =
        if (this > MEMBER) {
            entries[ordinal - 1]
        } else {
            entries[ordinal]
        }
}