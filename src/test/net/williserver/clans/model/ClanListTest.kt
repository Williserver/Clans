package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Test
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
}