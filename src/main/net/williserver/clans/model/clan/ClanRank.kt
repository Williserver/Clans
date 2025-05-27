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
 * @param permissions Set of permissions for this rank.
 * @author Willmo3
 */
enum class ClanRank(private val permissions: Set<ClanPermission>): Comparable<ClanRank> {
    MEMBER(setOf()),
    ELDER(setOf(ClanPermission.INVITE, ClanPermission.KICK) + MEMBER.permissions),
    COLEADER(setOf(ClanPermission.SET) + ELDER.permissions),
    LEADER(setOf(ClanPermission.DISBAND) + COLEADER.permissions);

    /**
     * @return Whether this ClanRank has the specified permission in its permission slist.
     */
    fun hasPermission(permission: ClanPermission) = permission in permissions
}