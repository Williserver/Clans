package net.williserver.clans.model

import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.ClanSet.Companion.readFromFile
import net.williserver.clans.model.ClanSet.Companion.writeToFile
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * @author Willmo3
 */
class ClanSetTest {
    @Test
    fun testReadWrite() {
        val leader = UUID.randomUUID()
        val clanData = Clan("TestClan", leader = leader, members = mutableSetOf(UUID.randomUUID(), UUID.randomUUID()), elders = mutableSetOf(UUID.randomUUID()), coLeaders = mutableSetOf(UUID.randomUUID()))
        val clanSet = ClanSet(mutableSetOf(clanData))

        writeToFile(LogHandler(null), "testClanList.json", clanSet)
        assertEquals(clanSet, readFromFile(LogHandler(null), "testClanList.json"))
    }

    @Test
    fun testNoDuplicateClanName() {
        val clan1 = Clan("DuplicateName", UUID.randomUUID())
        val clan2 = Clan("DuplicateName", UUID.randomUUID())
        assertThrows(IllegalArgumentException::class.java) { ClanSet(mutableSetOf(clan1, clan2)) }
    }

    @Test
    fun testNoInitWithDuplicateMembers() {
        val dup = UUID.randomUUID()
        val leader1 = UUID.randomUUID()
        val leader2 = UUID.randomUUID()
        val clan1 = Clan("Clan1", leader1, members = mutableSetOf(dup), elders = mutableSetOf(), coLeaders = mutableSetOf())
        val clan2 = Clan("Clan2", leader2, members = mutableSetOf(dup), elders = mutableSetOf(), coLeaders = mutableSetOf())

        assertThrows(IllegalArgumentException::class.java) { ClanSet(mutableSetOf(clan1, clan2)) }
    }

    @Test
    fun testClanOf() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader = leader, members = mutableSetOf(), elders = mutableSetOf(), coLeaders = mutableSetOf())
        val list = ClanSet(mutableSetOf(clan))
        assert(list.isPlayerInClan(clan.leader()))
        assertEquals(clan, list.clanOf(clan.leader()))
    }

    @Test
    fun testPlayerNotInClan() {
        val leader = UUID.randomUUID()
        val clanData = Clan("TestClan", leader, mutableSetOf(), mutableSetOf(), mutableSetOf())
        val list = ClanSet(mutableSetOf(clanData))
        assertFalse(list.isPlayerInClan(UUID.randomUUID()))
        assertThrows(NullPointerException::class.java) { list.clanOf(UUID.randomUUID()) }
    }

    @Test
    fun testAddClan() {
        val clan = Clan("TestClan", UUID.randomUUID())
        val list = ClanSet(mutableSetOf(clan))

        // Cannot add duplicate clans.
        val sameLeaderClan = Clan("NewTestClan", clan.leader())
        assertThrows(IllegalArgumentException::class.java) { list += sameLeaderClan }

        val newLeader = UUID.randomUUID()
        val sameNameClan = Clan("TestClan", newLeader)
        assertThrows(IllegalArgumentException::class.java) { list += sameNameClan }

        val uniqueClan = Clan("ThisShouldWork", newLeader)
        list.addClan(uniqueClan)
        assert(uniqueClan.name in list)
    }

    @Test
    fun testRemoveClan() {
        val clan = Clan("TestClan", UUID.randomUUID())
        val list = ClanSet(mutableSetOf(clan))

        assert(clan.name in list)
        list -= clan
        assert(clan.name !in list)
        assertThrows(NoSuchElementException::class.java) { list -= clan }
    }
}