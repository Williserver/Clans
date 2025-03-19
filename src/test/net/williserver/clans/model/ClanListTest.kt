package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * @author Willmo3
 */
class ClanListTest {
//    fun generateClan(): Clan {
//        val leader = UUID.randomUUID().toString()
//        val members = listOf(leader)
//        return Clan(ClanData("TestClan", members, leader))
//    }

    @Test
    fun testConstructClanList() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        ClanList(clanListData)
    }

    @Test
    fun testAsDataTuple() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        assertEquals(clanListData, ClanList(clanListData).asDataTuple())
    }

    @Test
    fun testReadWrite() {
        val leader = UUID.randomUUID().toString()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        val clanList = ClanList(clanListData)

        writeToFile("testClanList.json", clanList)
        val readDataTuples = readFromFile("testClanList.json")

        val readList = ClanList(readDataTuples)
        assertEquals(clanList, readList);
    }
}