package net.williserver.clans.model.clan

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

/**
 * @author Willmo3
 */
class ClanRankTest {
    @Test
    fun testGetRank() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member))

        assertEquals(ClanRank.LEADER, clan.rankOf(leader))
        assertEquals(ClanRank.MEMBER, clan.rankOf(member))
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.rankOf(UUID.randomUUID()) }
    }

    @Test
    fun testDisbandPermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.DISBAND))
        Assertions.assertFalse(clan.rankOf(member).hasPermission(ClanPermission.DISBAND))
        Assertions.assertFalse(clan.rankOf(coleader).hasPermission(ClanPermission.DISBAND))
    }

    @Test
    fun testInvitePermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.INVITE))
        assert(clan.rankOf(coleader).hasPermission(ClanPermission.INVITE))
        Assertions.assertFalse(clan.rankOf(member).hasPermission(ClanPermission.INVITE))
    }

    @Test
    fun testKickPermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.KICK))
        assert(clan.rankOf(coleader).hasPermission(ClanPermission.KICK))
        Assertions.assertFalse(clan.rankOf(member).hasPermission(ClanPermission.KICK))
    }
}