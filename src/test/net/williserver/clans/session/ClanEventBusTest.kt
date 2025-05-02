package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.ClanSet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse

class ClanEventBusTest {
    @Test
    fun testRegisterFireCreateClan() {
        val list = ClanSet(setOf())
        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader)
        assertFalse(newClan.name in list)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.CREATE, list.constructCreateListener())
        bus.fireEvent(ClanEvent.CREATE, newClan, newLeader, newLeader)
        assert(newClan.name in list)
    }

    @Test
    fun testRegisterFireDisbandClan() {
        val list = ClanSet(setOf())
        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader)
        list.addClan(newClan)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.DISBAND, list.constructDisbandListener())
        bus.fireEvent(ClanEvent.DISBAND, newClan, newLeader, newLeader)
        assert(newClan.name !in list)
    }

    @Test
    fun testRegisterFireJoinClan() {
        val newClan = Clan("TestClan", UUID.randomUUID())
        val list = ClanSet(setOf())
        list.addClan(newClan)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.JOIN, list.constructJoinListener())

        val newMember = UUID.randomUUID()
        bus.fireEvent(ClanEvent.JOIN, newClan, newMember, newMember)
        assert(newMember in newClan)
    }

    @Test
    fun testRegisterFireLeaveClan() {
        val newLeader = UUID.randomUUID()
        val newClan = Clan("TestClan", newLeader)
        val list = ClanSet(setOf())
        list.addClan(newClan)

        val newMember = UUID.randomUUID()
        newClan.join(newMember)
        assert(newMember in newClan)

        val bus = ClanEventBus()
        // When leave fires, the listener should register the change.
        bus.registerListener(ClanEvent.LEAVE, list.constructLeaveListener())
        bus.fireEvent(ClanEvent.LEAVE, newClan, newMember, newMember)
        assert(newMember !in newClan)

        // Only non-leader members who are in the clan should be able to leave.
        assertThrows(IllegalArgumentException::class.java) { newClan.leave(newMember) }
        assertThrows(IllegalArgumentException::class.java) { newClan.leave(newLeader) }
    }

    @Test
    fun testRegisterFireKickClan() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableSetOf(leader, member), mutableSetOf(), mutableSetOf())
        val list = ClanSet(setOf())
        list.addClan(clan)
        assert(list.playerInClan(member))
        assert(member in clan)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.KICK, list.constructLeaveListener())
        bus.fireEvent(ClanEvent.KICK, clan, leader, member)

        assertFalse(list.playerInClan(member))
        assert(member !in clan)
    }

    @Test
    fun testDeregisterInvitation() {
        val clan = Clan("TestClan", UUID.randomUUID())

        // Add an invitation for a new member.
        val newMember = UUID.randomUUID()
        val session = SessionManager()
        session.registerTimer(ClanEvent.JOIN, Pair(newMember, clan), 300)
        assert(session.isTimerRegistered(ClanEvent.JOIN, Pair(newMember, clan)))

        // When the player joins, the event should be gone.
        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.JOIN, session.constructDeregisterInviteListener())
        bus.fireEvent(ClanEvent.JOIN, clan, newMember, newMember)

        assertFalse(session.isTimerRegistered(ClanEvent.JOIN, Pair(newMember, clan)))
    }
}