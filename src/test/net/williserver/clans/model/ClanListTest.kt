package net.williserver.clans.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * @author Willmo3
 */
class ClanListTest {
    fun generateClanData(): ClanData {
        val leader = UUID.randomUUID().toString()
        val members = listOf(leader)
        return ClanData("TestClan", members, leader)
    }

    @Test
    fun testConstructClanList() {
        ClanList(listOf(generateClanData()))
    }

    @Test
    fun testReadWrite() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanList = ClanList(listOf(clanData))

        writeToFile("testClanList.json", clanList)
        assertEquals(clanList, readFromFile("testClanList.json"))
    }

    @Test
    fun testNoDuplicateClanNames() {
        assertThrows(IllegalArgumentException::class.java) { ClanList(listOf(generateClanData(), generateClanData())) }
    }

    @Test
    fun testPlayerClan() {
        val clanData = generateClanData()
        val list = ClanList(listOf(clanData))
        assert(list.playerInClan(UUID.fromString(clanData.leader)))
        assertEquals(Clan(clanData), list.playerClan(UUID.fromString(clanData.leader)))
    }

    @Test
    fun testPlayerNotInClan() {
        val clanData = generateClanData()
        val list = ClanList(listOf(clanData))
        assertFalse(list.playerInClan(UUID.randomUUID()))
        assertThrows(NullPointerException::class.java) { list.playerClan(UUID.randomUUID()) }
    }

    @Test
    fun testAddClan() {
        val clanData = generateClanData()
        val list = ClanList(listOf(clanData))

        // Cannot add duplicate clans.
        val sameLeaderClan = ClanData("NewTestClan", listOf(clanData.leader), clanData.leader)
        assertThrows(IllegalArgumentException::class.java) { list.addClan(Clan(sameLeaderClan)) }

        val newLeader = UUID.randomUUID().toString()
        val sameNameClan = ClanData("TestClan", listOf(newLeader), newLeader)
        assertThrows(IllegalArgumentException::class.java) { list.addClan(Clan(sameNameClan)) }

        val uniqueClan = ClanData("ThisShouldWork", listOf(newLeader), newLeader)
        list.addClan(Clan(uniqueClan))
        assert(uniqueClan.name in list)
    }

    @Test
    fun testRemoveClan() {
        val clanData = generateClanData()
        val clan = Clan(clanData)
        val list = ClanList(listOf(clanData))

        assert(clan.name in list)
        list.removeClan(clan)
        assert(clan.name !in list)
        assertThrows(NoSuchElementException::class.java) { list.removeClan(clan) }
    }

    @Test
    fun testForEachClan() {
        val names = HashSet<String>()

        val leaderOne = UUID.randomUUID()
        val clanOne = Clan("TestClan1", leaderOne, mutableListOf(leaderOne))

        val leaderTwo = UUID.randomUUID()
        val clanTwo = Clan("TestClan2", leaderTwo, mutableListOf(leaderTwo))

        val list = ClanList(listOf(clanOne.asDataTuple(), clanTwo.asDataTuple()))
        list.forEachClan { names.add(it.name) }

        assert("TestClan1" in names)
        assert("TestClan2" in names)
        assert("Sanity" !in names)
    }
}