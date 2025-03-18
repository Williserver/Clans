package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ClanListTest {
    @Test
    fun testConstructClanList() {
        val leader = UUID.randomUUID()
        val clanData = ClanData("TestClan", listOf(leader), leader)
        val clanListData = ClanListData(listOf(clanData))
        ClanList(LogHandler(null), clanListData)
    }
}