package net.williserver.clans.model.clan

/**
 * Named permissions for important clan actions.
 * @author Willmo3
 */
enum class ClanPermission {
    DISBAND,
    INVITE,
    KICK,
    SET,
}

/**
 * Represents a rank in a clan -- effectively, a named wrapper around a number representing relative authority.
 *
 * @param rank Digit precedence for rank.
 * @author Willmo3
 */
enum class ClanRank(private val rank: UInt, private val permissions: Array<ClanPermission>): Comparable<ClanRank> {
    MEMBER(0u, arrayOf()),
    ELDER(1u, arrayOf(ClanPermission.INVITE, ClanPermission.KICK) + MEMBER.permissions),
    COLEADER(2u, arrayOf(ClanPermission.SET) + ELDER.permissions),
    LEADER(3u, arrayOf(ClanPermission.DISBAND) + COLEADER.permissions);

    /**
     * @return Whether this ClanRank has the specified permission in its permission slist.
     */
    fun hasPermission(permission: ClanPermission) = permission in permissions
}