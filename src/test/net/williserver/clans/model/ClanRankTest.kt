package net.williserver.clans.model

import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanPermission
import net.williserver.clans.model.clan.ClanRank
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
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
        assertThrows(IllegalArgumentException::class.java) { clan.rankOf(UUID.randomUUID()) }
    }

    @Test
    fun testDisbandPermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.DISBAND))
        assertFalse(clan.rankOf(member).hasPermission(ClanPermission.DISBAND))
        assertFalse(clan.rankOf(coleader).hasPermission(ClanPermission.DISBAND))
    }

    @Test
    fun testInvitePermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.INVITE))
        assert(clan.rankOf(coleader).hasPermission(ClanPermission.INVITE))
        assertFalse(clan.rankOf(member).hasPermission(ClanPermission.INVITE))
    }

    @Test
    fun testKickPermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(member), coLeaders = mutableSetOf(coleader))

        assert(clan.rankOf(leader).hasPermission(ClanPermission.KICK))
        assert(clan.rankOf(coleader).hasPermission(ClanPermission.KICK))
        assertFalse(clan.rankOf(member).hasPermission(ClanPermission.KICK))
    }
}