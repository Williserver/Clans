package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import net.williserver.clans.session.lifecycle.ClanEvent
import net.williserver.clans.session.lifecycle.ClanEventBus
import net.williserver.clans.session.lifecycle.createAddClanToModel
import net.williserver.clans.session.lifecycle.disbandRemoveClanFromModel
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse

class ClanEventBusTest {
    @Test
    fun testRegisterFireCreateClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        bus.registerListener(ClanEvent.CREATE, ::createAddClanToModel)

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        assertFalse(newClan.name in list)

        bus.fireEvent(ClanEvent.CREATE, list, newClan, newLeader)
        assert(newClan.name in list)
    }

    @Test
    fun testRegisterFireDisbandClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        bus.registerListener(ClanEvent.DISBAND, ::disbandRemoveClanFromModel)

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        bus.fireEvent(ClanEvent.DISBAND, list, newClan, newLeader)
        assert(newClan.name !in list)
    }
}