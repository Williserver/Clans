package net.williserver.clans.model

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.*
import kotlin.test.assertEquals

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
        assertEquals(Clan(clanData), list.playerClan(UUID.fromString(clanData.leader)))
        assertNull(list.playerClan(UUID.randomUUID()))
    }
}