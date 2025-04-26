package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.model.ClanList
import org.junit.jupiter.api.Assertions.assertThrows
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

        bus.registerListener(ClanEvent.CREATE, list.constructCreateListener())
        bus.fireEvent(ClanEvent.CREATE, newClan, newLeader)
        assert(newClan.name in list)
    }

    @Test
    fun testRegisterFireDisbandClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        bus.registerListener(ClanEvent.DISBAND, list.constructDisbandListener())
        bus.fireEvent(ClanEvent.DISBAND, newClan, newLeader)
        assert(newClan.name !in list)
    }

    @Test
    fun testRegisterFireJoinClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        bus.registerListener(ClanEvent.JOIN, list.constructJoinListener())
        val newMember = UUID.randomUUID()
        bus.fireEvent(ClanEvent.JOIN, newClan, newMember)
        assert(newMember in newClan)
    }

    @Test
    fun testRegisterFireLeaveClan() {
        val bus = ClanEventBus()
        val list = ClanList(mutableListOf())

        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader, mutableListOf(newLeader))
        list.addClan(newClan)

        val newMember = UUID.randomUUID()
        newClan.join(newMember)
        assert(newMember in newClan)

        // When leave fires, the listener should register the change.
        bus.registerListener(ClanEvent.LEAVE, list.constructLeaveListener())
        bus.fireEvent(ClanEvent.LEAVE, newClan, newMember)
        assert(newMember !in newClan)

        // Only non-leader members who are in the clan should be able to leave.
        assertThrows(IllegalArgumentException::class.java) { newClan.leave(newMember) }
        assertThrows(IllegalArgumentException::class.java) { newClan.leave(newLeader) }
    }

    @Test
    fun testDeregisterInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))

        // Add an invitation for a new member.
        val newMember = UUID.randomUUID()
        val session = SessionManager()
        session.registerTimer(ClanEvent.JOIN, Pair(newMember, clan), 300)
        assert(session.isTimerRegistered(ClanEvent.JOIN, Pair(newMember, clan)))

        // When the player joins, the event should be gone.
        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.JOIN, session.constructDeregisterInviteListener())
        bus.fireEvent(ClanEvent.JOIN, clan, newMember)

        assertFalse(session.isTimerRegistered(ClanEvent.JOIN, Pair(newMember, clan)))
    }
}