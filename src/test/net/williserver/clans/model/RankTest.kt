package net.williserver.clans.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    @Test
    fun hasDisband() {
        assertFalse(ClanRank.MEMBER.hasPermission(ClanPermission.DISBAND))
        assertFalse(ClanRank.ELDER.hasPermission(ClanPermission.DISBAND))
        assertFalse(ClanRank.COLEADER.hasPermission(ClanPermission.DISBAND))
        assertTrue(ClanRank.LEADER.hasPermission(ClanPermission.DISBAND))
    }
}