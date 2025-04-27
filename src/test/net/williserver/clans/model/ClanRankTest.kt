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
        val members = arrayListOf(leader, member)
        val clan = Clan("TestClan", leader, members)

        assertEquals(ClanRank.LEADER, clan.rankOfMember(leader))
        assertEquals(ClanRank.MEMBER, clan.rankOfMember(member))
        assertThrows(IllegalArgumentException::class.java) { clan.rankOfMember(UUID.randomUUID()) }
    }

    @Test
    fun testDisbandPermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val members = arrayListOf(leader, member)
        val clan = Clan("TestClan", leader, members)

        assert(clan.rankOfMember(leader).hasPermission(ClanPermission.DISBAND))
        assertFalse(clan.rankOfMember(member).hasPermission(ClanPermission.DISBAND))
    }

    @Test
    fun testInvitePermission() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val members = arrayListOf(leader, member)
        val clan = Clan("TestClan", leader, members)

        assert(clan.rankOfMember(leader).hasPermission(ClanPermission.INVITE))
        assertFalse(clan.rankOfMember(member).hasPermission(ClanPermission.INVITE))
    }
}