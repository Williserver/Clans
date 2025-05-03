package net.williserver.clans.model

import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanData
import net.williserver.clans.model.clan.ClanRank
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ClanTest {
    private fun generateValidClan(): Clan {
        val leader = UUID.randomUUID().toString()
        val data = ClanData("TestClan",
            members=setOf(UUID.randomUUID().toString()),
            elders=setOf(),
            coLeaders=setOf(),
            leader)
        return Clan(data)
    }

    @Test
    fun joinClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        assert(newMember !in clan)
        clan.join(newMember)
        assert(newMember in clan)
        assertThrows(IllegalArgumentException::class.java) { clan.join(newMember) }
    }

    @Test
    fun leaveClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        clan.join(newMember)
        assert(newMember in clan)
        clan.leave(newMember)
        assert(newMember !in clan)
        assertThrows(IllegalArgumentException::class.java) { clan.leave(newMember) }
        assertThrows(IllegalArgumentException::class.java) { clan.leave(clan.leader) }
    }

    @Test
    fun validClanName() {
        assert(net.williserver.clans.model.clan.validClanName("123Test-Clan_"))
        assert(!net.williserver.clans.model.clan.validClanName("\"\""))
        assert(!net.williserver.clans.model.clan.validClanName("{123}"))
        assert(!net.williserver.clans.model.clan.validClanName(""))
        assert(!net.williserver.clans.model.clan.validClanName(" "))
    }

    @Test
    fun testConstructClanLeaderDuplicateRank() {
        val leader = UUID.randomUUID()

        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(leader)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, elders = mutableSetOf(leader)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, members = mutableSetOf(leader)) }
    }

    @Test
    fun testConstructClanColeaderDuplicateRank() {
        val leader = UUID.randomUUID()
        val coLeader = UUID.randomUUID()

        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(leader)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(coLeader), elders = mutableSetOf(coLeader)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(coLeader), members = mutableSetOf(coLeader)) }
    }

    @Test
    fun testConstructClanElderDuplicateRank() {
        val leader = UUID.randomUUID()
        val elder = UUID.randomUUID()

        assertThrows(IllegalArgumentException::class.java ) { Clan("TestClan", leader, elders = mutableSetOf(leader)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(elder), elders = mutableSetOf(elder)) }
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, elders = mutableSetOf(elder), members = mutableSetOf(elder)) }
    }

    @Test
    fun testRankOfMember() {
        val leader = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val elder = UUID.randomUUID()
        val member = UUID.randomUUID()
        val clan = Clan("TestClan",
                        leader,
                        members = mutableSetOf(member),
                        coLeaders = mutableSetOf(coleader),
                        elders = mutableSetOf(elder),
                        )
        assert(clan.rankOfMember(leader) == ClanRank.LEADER)
        assert(clan.rankOfMember(coleader) == ClanRank.COLEADER)
        assert(clan.rankOfMember(elder) == ClanRank.ELDER)
        assert(clan.rankOfMember(member) == ClanRank.MEMBER)
    }

    @Test
    fun testPromote() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()

        val clan = Clan("TestClan",
            leader,
            members = mutableSetOf(member),
        )
        assert(clan.rankOfMember(member) == ClanRank.MEMBER)
        clan.promote(member)
        assert(clan.rankOfMember(member) == ClanRank.ELDER)
        clan.promote(member)
        assert(clan.rankOfMember(member) == ClanRank.COLEADER)

        assertThrows(IllegalArgumentException::class.java) { clan.promote(member) }
        assertThrows(IllegalArgumentException::class.java) { clan.promote(leader) }
    }
}