package net.williserver.clans.model.clan

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * @author Willmo3
 */
class ClanTest {
    private fun generateValidClan(): Clan {
        val leader = UUID.randomUUID()
        return Clan("TestClan",
            leader,
            members=mutableSetOf(UUID.randomUUID()),
            elders=mutableSetOf(),
            coLeaders=mutableSetOf())
    }

    @Test
    fun joinClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        assert(newMember !in clan)
        clan.join(newMember)
        assert(newMember in clan)
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.join(newMember) }
    }

    @Test
    fun leaveClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        clan.join(newMember)
        assert(newMember in clan)
        clan.leave(newMember)
        assert(newMember !in clan)
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.leave(newMember) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.leave(clan.leader()) }

        // Test different ranks.
        clan.join(newMember)
        clan.promote(newMember)
        assert(newMember in clan)
        clan.leave(newMember)
        assert(newMember !in clan)

        clan.join(newMember)
        clan.promote(newMember)
        clan.promote(newMember)
        assert(newMember in clan)
        clan.leave(newMember)
        assert(newMember !in clan)
    }

    @Test
    fun validClanName() {
        assert(Clan.validClanName("123Test-Clan_"))
        assert(!Clan.validClanName("\"\""))
        assert(!Clan.validClanName("{123}"))
        assert(!Clan.validClanName(""))
        assert(!Clan.validClanName(" "))
    }

    @Test
    fun testConstructClanLeaderDuplicateRank() {
        val leader = UUID.randomUUID()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                coLeaders = mutableSetOf(leader)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                elders = mutableSetOf(leader)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                members = mutableSetOf(leader)
            )
        }
    }

    @Test
    fun testConstructClanColeaderDuplicateRank() {
        val leader = UUID.randomUUID()
        val coLeader = UUID.randomUUID()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                coLeaders = mutableSetOf(leader)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                coLeaders = mutableSetOf(coLeader),
                elders = mutableSetOf(coLeader)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                coLeaders = mutableSetOf(coLeader),
                members = mutableSetOf(coLeader)
            )
        }
    }

    @Test
    fun testConstructClanElderDuplicateRank() {
        val leader = UUID.randomUUID()
        val elder = UUID.randomUUID()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                elders = mutableSetOf(leader)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                coLeaders = mutableSetOf(elder),
                elders = mutableSetOf(elder)
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Clan(
                "TestClan",
                leader,
                elders = mutableSetOf(elder),
                members = mutableSetOf(elder)
            )
        }
    }

    @Test
    fun testRankOf() {
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
        assert(clan.rankOf(leader) == ClanRank.LEADER)
        assert(clan.rankOf(coleader) == ClanRank.COLEADER)
        assert(clan.rankOf(elder) == ClanRank.ELDER)
        assert(clan.rankOf(member) == ClanRank.MEMBER)
    }

    @Test
    fun testPromote() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()

        val clan = Clan("TestClan",
            leader,
            members = mutableSetOf(member),
        )
        assert(clan.rankOf(member) == ClanRank.MEMBER)
        clan.promote(member)
        assert(clan.rankOf(member) == ClanRank.ELDER)
        clan.promote(member)
        assert(clan.rankOf(member) == ClanRank.COLEADER)

        // Cannot promote beyond maximum rank
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.promote(member) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.promote(leader) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.demote(UUID.randomUUID()) }
    }

    @Test
    fun testDemote() {
        val leader = UUID.randomUUID()
        val coleader = UUID.randomUUID()

        val clan = Clan("TestClan",
            leader,
            coLeaders = mutableSetOf(coleader),
        )
        assert(clan.rankOf(coleader) == ClanRank.COLEADER)
        clan.demote(coleader)
        assert(clan.rankOf(coleader) == ClanRank.ELDER)
        clan.demote(coleader)
        assert(clan.rankOf(coleader) == ClanRank.MEMBER)

        // Cannot demote below maximum rank.
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.demote(coleader) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.demote(leader) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.demote(UUID.randomUUID()) }
    }

    @Test
    fun testCrown() {
        val leader = UUID.randomUUID()
        val coleader = UUID.randomUUID()
        val elder = UUID.randomUUID()
        val member = UUID.randomUUID()

        val coleaders = mutableSetOf(coleader)

        val clan = Clan("TestClan",
            leader,
            members = mutableSetOf(member),
            elders = mutableSetOf(elder),
            coLeaders = coleaders,)

        Assertions.assertEquals(leader, clan.leader())
        assert(leader !in coleaders)
        assert(coleader in coleaders)

        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.crown(leader) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.crown(elder) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { clan.crown(member) }

        clan.crown(coleader)
        Assertions.assertEquals(coleader, clan.leader())
        Assertions.assertEquals(ClanRank.COLEADER, clan.rankOf(leader))

        assert(leader in coleaders)
        assert(coleader !in coleaders)
    }

    @Test
    fun testDefaultOptions() {
        val clan = Clan("Test", UUID.randomUUID())

    }

    @Test
    fun testClanEquality() {
        val leader = UUID.randomUUID()
        val clan1 = Clan("TestClan", leader)
        val clan2 = Clan("TestClan", leader)

        Assertions.assertEquals(clan1, clan2)
        Assertions.assertEquals(clan1.hashCode(), clan2.hashCode())

        val differentClan = Clan("DifferentClan", leader)
        Assertions.assertNotEquals(clan1, differentClan)
    }
}