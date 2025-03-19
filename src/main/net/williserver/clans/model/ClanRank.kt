package net.williserver.clans.model

/**
 * Represents a rank in a clan -- effectively, a named wrapper around a number representing relative authority.
 *
 * @param rank Digit precedence for rank.
 * @author Willmo3
 */
enum class ClanRank(val rank: UInt): Comparable<ClanRank> {
    MEMBER(0u),
    ELDER(1u),
    COLEADER(2u),
    LEADER(3u);

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