package net.williserver.clans.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * @author Willmo3
 */
class RankTest {
    @Test
    fun testPromote() {
        assertEquals(ClanRank.ELDER, ClanRank.MEMBER.nextRank())
        assertEquals(ClanRank.COLEADER, ClanRank.ELDER.nextRank())
        assertEquals(ClanRank.LEADER, ClanRank.COLEADER.nextRank())
        assertEquals(ClanRank.LEADER, ClanRank.LEADER.nextRank())
    }
    @Test
    fun testDemote() {
        assertEquals(ClanRank.MEMBER, ClanRank.MEMBER.previousRank())
        assertEquals(ClanRank.MEMBER, ClanRank.ELDER.previousRank())
        assertEquals(ClanRank.ELDER, ClanRank.COLEADER.previousRank())
        assertEquals(ClanRank.COLEADER, ClanRank.LEADER.previousRank())
    }
}