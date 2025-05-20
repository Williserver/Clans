package net.williserver.clans.model

import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanData
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
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", "TST", members = setOf(), elders = setOf(), coLeaders = setOf(), leader)
        val clanSet = ClanSet(setOf(clanData))

        writeToFile(LogHandler(null), "testClanList.json", clanSet)
        assertEquals(clanSet, readFromFile(LogHandler(null), "testClanList.json"))
    }

    @Test
    fun testNoDuplicateClanNames() {
        val clan1 = Clan("DuplicateName", UUID.randomUUID())
        val clan2 = Clan("DuplicateName", UUID.randomUUID())
        assertThrows(IllegalArgumentException::class.java) { ClanSet(setOf(clan1.asDataTuple(), clan2.asDataTuple())) }
    }

    @Test
    fun testNoInitWithDuplicateMembers() {
        val dup = UUID.randomUUID()
        val leader1 = UUID.randomUUID()
        val leader2 = UUID.randomUUID()
        val clan1 = Clan("Clan1", leader1, members = mutableSetOf(dup), elders = mutableSetOf(), coLeaders = mutableSetOf())
        val clan2 = Clan("Clan2", leader2, members = mutableSetOf(dup), elders = mutableSetOf(), coLeaders = mutableSetOf())

        assertThrows(IllegalArgumentException::class.java) { ClanSet(setOf(clan1.asDataTuple(), clan2.asDataTuple())) }
    }

    @Test
    fun testClanOf() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", "TST", setOf(), setOf(), setOf(), leader)
        val list = ClanSet(setOf(clanData))
        assert(list.isPlayerInClan(UUID.fromString(clanData.leader)))
        assertEquals(Clan(clanData), list.clanOf(UUID.fromString(clanData.leader)))
    }

    @Test
    fun testPlayerNotInClan() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", "TST", setOf(), setOf(), setOf(), leader)
        val list = ClanSet(setOf(clanData))
        assertFalse(list.isPlayerInClan(UUID.randomUUID()))
        assertThrows(NullPointerException::class.java) { list.clanOf(UUID.randomUUID()) }
    }

    @Test
    fun testAddClan() {
        val clan = Clan("TestClan", UUID.randomUUID())
        val list = ClanSet(setOf(clan.asDataTuple()))

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
        val list = ClanSet(setOf(clan.asDataTuple()))

        assert(clan.name in list)
        list -= clan
        assert(clan.name !in list)
        assertThrows(NoSuchElementException::class.java) { list -= clan }
    }
}