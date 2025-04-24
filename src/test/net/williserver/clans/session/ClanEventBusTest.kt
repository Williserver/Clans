package net.williserver.clans.session

import net.williserver.clans.lifecycle.*
import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse

class ClanEventBusTest {
    @Test
    fun testRegisterFireCreateClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        assertFalse(newClan.name in list)

        bus.registerListener(ClanEvent.CREATE, ::createAddClanToModel)
        bus.fireEvent(ClanEvent.CREATE, list, newClan, newLeader)
        assert(newClan.name in list)
    }

    @Test
    fun testRegisterFireDisbandClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        bus.registerListener(ClanEvent.DISBAND, ::disbandRemoveClanFromModel)
        bus.fireEvent(ClanEvent.DISBAND, list, newClan, newLeader)
        assert(newClan.name !in list)
    }

    @Test
    fun testRegisterFireJoinClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        bus.registerListener(ClanEvent.JOIN, ::joinAddPlayerToClan)
        val newMember = UUID.randomUUID()
        bus.fireEvent(ClanEvent.JOIN, list, newClan, newMember)
        assert(newMember in newClan)
    }
}