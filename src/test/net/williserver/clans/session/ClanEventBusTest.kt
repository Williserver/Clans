package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.ClanSet
import org.junit.jupiter.api.Assertions.assertEquals
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
        bus.registerListener(ClanEvent.CREATE, ClanListenerType.MODEL, list.constructCreateListener())
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
        bus.registerListener(ClanEvent.DISBAND, ClanListenerType.MODEL, list.constructDisbandListener())
        bus.fireEvent(ClanEvent.DISBAND, newClan, newLeader, newLeader)
        assert(newClan.name !in list)
    }

    @Test
    fun testRegisterFireJoinClan() {
        val newClan = Clan("TestClan", UUID.randomUUID())
        val list = ClanSet(setOf())
        list.addClan(newClan)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.JOIN, ClanListenerType.MODEL, list.constructJoinListener())

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
        bus.registerListener(ClanEvent.LEAVE, ClanListenerType.MODEL, list.constructLeaveListener())
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
        val clan = Clan("TestClan", leader, members = mutableSetOf(member))
        val list = ClanSet(setOf())
        list.addClan(clan)
        assert(list.isPlayerInClan(member))
        assert(member in clan)

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.KICK, ClanListenerType.MODEL, list.constructKickListener())
        bus.fireEvent(ClanEvent.KICK, clan, leader, member)

        assertFalse(list.isPlayerInClan(member))
        assert(member !in clan)

        clan.join(member)
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.KICK, clan, member, member) }
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
        bus.registerListener(ClanEvent.JOIN, ClanListenerType.SESSION, session.constructDeregisterInviteListener())
        bus.fireEvent(ClanEvent.JOIN, clan, newMember, newMember)

        assertFalse(session.isTimerRegistered(ClanEvent.JOIN, Pair(newMember, clan)))
    }

    @Test
    fun testOrderingCorrect() {
        // Using non-associative operations, ensure the ordering is correct.
        var sum = 1
        val bus = ClanEventBus()

        bus.registerListener(ClanEvent.CREATE, ClanListenerType.MODEL) { _, _, _ ->
            sum *= 2
        }
        bus.registerListener(ClanEvent.CREATE, ClanListenerType.INTEGRATION) { _, _, _ ->
            sum /= 2
        }
        bus.registerListener(ClanEvent.CREATE, ClanListenerType.SESSION) { _, _, _ ->
            sum += 1
        }
        bus.registerListener(ClanEvent.CREATE, ClanListenerType.COSMETIC) { _, _, _ ->
            sum *= 3
        }

        // Fire repeatedly to avoid potential set ordering traversal weirdness.
        for (i in 1..100) {
            bus.fireEvent(ClanEvent.CREATE, Clan("TestClan", UUID.randomUUID()), UUID.randomUUID(), UUID.randomUUID())
        }
        assertEquals(101462409, sum)
    }
}