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
            members=setOf(leader, UUID.randomUUID().toString()),
            elders=setOf(),
            coLeaders=setOf(),
            leader)
        return Clan(data)
    }

    @Test
    fun constructValidClan() {
        generateValidClan()
    }

    @Test
    fun constructClanLeaderNotMember() {
        val data = ClanData("TestClan",
            members=setOf(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
            coLeaders=setOf(),
            elders=setOf(),
            leader=UUID.randomUUID().toString())
        assertThrows(IllegalArgumentException::class.java) { Clan(data) }
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
    fun testConstructClanColeaderNotMember() {
        val leader = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, coLeaders = mutableSetOf(UUID.randomUUID())) }
    }

    @Test
    fun testConstructClanColeaderLeader() {
        val leader = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java ) { Clan("TestClan", leader, coLeaders = mutableSetOf(leader)) }
    }

    @Test
    fun testConstructClanElderNotMember() {
        val leader = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, elders = mutableSetOf(UUID.randomUUID())) }
    }

    @Test
    fun testConstructClanElderLeader() {
        val leader = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) { Clan("TestClan", leader, elders = mutableSetOf(leader)) }
    }

    @Test
    fun testRankOfMember() {
        val leader = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val elder = UUID.randomUUID()
        val member = UUID.randomUUID()
        val clan = Clan("TestClan",
                        leader,
                        members = mutableSetOf(leader, elder, coleader, member),
                        coLeaders = mutableSetOf(coleader),
                        elders = mutableSetOf(elder),
                        )
        assert(clan.rankOfMember(leader) == ClanRank.LEADER)
        assert(clan.rankOfMember(coleader) == ClanRank.COLEADER)
        assert(clan.rankOfMember(elder) == ClanRank.ELDER)
        assert(clan.rankOfMember(member) == ClanRank.MEMBER)
    }
}