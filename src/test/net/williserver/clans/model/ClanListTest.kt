package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * @author Willmo3
 */
class ClanListTest {
    @Test
    fun testConstructClanList() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        ClanList(LogHandler(null), clanListData)
    }

    @Test
    fun testAsDataTuple() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        assertEquals(clanListData, ClanList(LogHandler(null), clanListData).asDataTuple())
    }

    @Test
    fun testReadWrite() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        val clanList = ClanList(LogHandler(null), clanListData)

        writeToFile("testClanList.json", clanList)
        val readDataTuples = readFromFile("testClanList.json")

        val readList = ClanList(LogHandler(null), readDataTuples)
        assertEquals(clanList, readList);
    }
}