package net.williserver.clans.model

import net.williserver.clans.LogHandler
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanData
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * @author Willmo3
 */
class ClanSetTest {
    private fun generateClanData(): ClanData {
        val leader = UUID.randomUUID().toString()
        val members = setOf(leader)
        return ClanData("TestClan", members, elders = setOf(), coLeaders = setOf(), leader)
    }

    @Test
    fun testConstructClanList() {
        ClanSet(setOf(generateClanData()))
    }

    @Test
    fun testReadWrite() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", setOf(leader), elders = setOf(), coLeaders = setOf(), leader)
        val clanSet = ClanSet(setOf(clanData))

        writeToFile(LogHandler(null), "testClanList.json", clanSet)
        assertEquals(clanSet, readFromFile(LogHandler(null), "testClanList.json"))
    }

    @Test
    fun testNoDuplicateClanNames() {
        assertThrows(IllegalArgumentException::class.java) { ClanSet(setOf(generateClanData(), generateClanData())) }
    }

    @Test
    fun testNoInitWithDuplicateMembers() {
        val dup = UUID.randomUUID()
        val leader1 = UUID.randomUUID()
        val leader2 = UUID.randomUUID()
        val clan1 = Clan("Clan1", leader1, mutableSetOf(leader1, dup), elders = mutableSetOf(), coLeaders = mutableSetOf())
        val clan2 = Clan("Clan2", leader2, mutableSetOf(leader2, dup), elders = mutableSetOf(), coLeaders = mutableSetOf())

        assertThrows(IllegalArgumentException::class.java) { ClanSet(setOf(clan1.asDataTuple(), clan2.asDataTuple())) }
    }

    @Test
    fun testPlayerClan() {
        val clanData = generateClanData()
        val list = ClanSet(setOf(clanData))
        assert(list.playerInClan(UUID.fromString(clanData.leader)))
        assertEquals(Clan(clanData), list.playerClan(UUID.fromString(clanData.leader)))
    }

    @Test
    fun testPlayerNotInClan() {
        val clanData = generateClanData()
        val list = ClanSet(setOf(clanData))
        assertFalse(list.playerInClan(UUID.randomUUID()))
        assertThrows(NullPointerException::class.java) { list.playerClan(UUID.randomUUID()) }
    }

    @Test
    fun testAddClan() {
        val clanData = generateClanData()
        val list = ClanSet(setOf(clanData))

        // Cannot add duplicate clans.
        val sameLeaderClan = ClanData("NewTestClan", setOf(clanData.leader), elders = setOf(), coLeaders = setOf(), clanData.leader)
        assertThrows(IllegalArgumentException::class.java) { list.addClan(Clan(sameLeaderClan)) }

        val newLeader = UUID.randomUUID().toString()
        val sameNameClan = ClanData("TestClan", setOf(newLeader), elders = setOf(),  coLeaders = setOf(), newLeader)
        assertThrows(IllegalArgumentException::class.java) { list.addClan(Clan(sameNameClan)) }

        val uniqueClan = ClanData("ThisShouldWork", setOf(newLeader), elders = setOf(), coLeaders = setOf(), newLeader)
        list.addClan(Clan(uniqueClan))
        assert(uniqueClan.name in list)
    }

    @Test
    fun testRemoveClan() {
        val clanData = generateClanData()
        val clan = Clan(clanData)
        val list = ClanSet(setOf(clanData))

        assert(clan.name in list)
        list.removeClan(clan)
        assert(clan.name !in list)
        assertThrows(NoSuchElementException::class.java) { list.removeClan(clan) }
    }
}