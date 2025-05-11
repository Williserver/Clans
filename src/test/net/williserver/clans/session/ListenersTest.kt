package net.williserver.clans.session

import net.williserver.clans.model.ClanSet
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanRank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ListenersTest {
    @Test
    fun testDisbandAgentLeader() {
        val clan = Clan("TestClan", UUID.randomUUID())
        val clanSet = ClanSet(setOf(clan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.DISBAND, ClanListenerType.MODEL, clanSet.constructDisbandListener())
        // Fire a disband event with a random player initiating
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.DISBAND, clan, UUID.randomUUID(), UUID.randomUUID()) }

        bus.fireEvent(ClanEvent.DISBAND, clan, clan.leader, clan.leader)
        assert(clan.name !in clanSet)
    }

    @Test
    fun testCreateAgentLeader() {
        val clan = Clan("TestClan", UUID.randomUUID())
        val clanSet = ClanSet(setOf())

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.CREATE, ClanListenerType.MODEL, clanSet.constructCreateListener())
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CREATE, clan, UUID.randomUUID(), UUID.randomUUID()) }

        bus.fireEvent(ClanEvent.CREATE, clan, clan.leader, clan.leader)
        assert(clan.name in clanSet)
    }

    @Test
    fun testJoinAlreadyInClan() {
        // ClanSet listeners should not allow players to be in multiple clans!
        val duplicate = UUID.randomUUID()
        val clan = Clan("TestClan", UUID.randomUUID(), elders= mutableSetOf(duplicate))
        val otherClan = Clan("TestClan2", UUID.randomUUID())
        val clanSet = ClanSet(setOf(clan.asDataTuple(), otherClan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.JOIN, ClanListenerType.MODEL, clanSet.constructJoinListener())
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.JOIN, otherClan, duplicate, duplicate) }
    }

    @Test
    fun testPromote() {
        val leader = UUID.randomUUID()
        val promotee = UUID.randomUUID()
        val otherColeader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, members = mutableSetOf(promotee), coLeaders = mutableSetOf(otherColeader))
        val clanSet = ClanSet(setOf(clan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.PROMOTE, ClanListenerType.MODEL, clanSet.constructPromoteListener())

        assertEquals(ClanRank.MEMBER, clan.rankOf(promotee))
        // Cannot promote yourself
        assertThrows(IllegalArgumentException::class.java) {bus.fireEvent(ClanEvent.PROMOTE, clan, promotee, promotee) }
        bus.fireEvent(ClanEvent.PROMOTE, clan, otherColeader, promotee)
        assertEquals(ClanRank.ELDER, clan.rankOf(promotee))
        bus.fireEvent(ClanEvent.PROMOTE, clan, otherColeader, promotee)
        assertEquals(ClanRank.COLEADER, clan.rankOf(promotee))
        // Cannot promote beyond coleader -- even if we outrank
        assertThrows(IllegalArgumentException::class.java) {bus.fireEvent(ClanEvent.PROMOTE, clan, leader, promotee) }
    }

    @Test
    fun testDemote() {
        val leader = UUID.randomUUID()
        val demotee = UUID.randomUUID()
        val otherColeader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, coLeaders = mutableSetOf(demotee, otherColeader))
        val clanSet = ClanSet(setOf(clan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.DEMOTE, ClanListenerType.MODEL, clanSet.constructDemoteListener())

        assertEquals(ClanRank.COLEADER, clan.rankOf(demotee))
        // You cannot demote yourself!
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.DEMOTE, clan, demotee, demotee) }
        bus.fireEvent(ClanEvent.DEMOTE, clan, leader, demotee)
        assertEquals(ClanRank.ELDER, clan.rankOf(demotee))
        bus.fireEvent(ClanEvent.DEMOTE, clan, otherColeader, demotee)
        assertEquals(ClanRank.MEMBER, clan.rankOf(demotee))
        // Cannot demote below member -- use kick instead
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.DEMOTE, clan, otherColeader, demotee) }
    }

    @Test
    fun testAnoint() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val elder = UUID.randomUUID()
        val coleader = UUID.randomUUID()

        val clan = Clan("TestClan", leader, members = mutableSetOf(member), elders = mutableSetOf(elder), coLeaders = mutableSetOf(coleader))
        val clanSet = ClanSet(setOf(clan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.CORONATE, ClanListenerType.MODEL, clanSet.constructCoronateListener())
        // Only a co-leader, promoted by a leader, should work!
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CORONATE, clan, leader, leader)}
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CORONATE, clan, leader, elder)}
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CORONATE, clan, leader, member)}
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CORONATE, clan, coleader, coleader)}

        bus.fireEvent(ClanEvent.CORONATE, clan, leader, coleader)
        assertEquals(coleader, clan.leader)
        assertEquals(ClanRank.COLEADER, clan.rankOf(leader))
    }

    // All event listeners should complain with a NoSuchElementException if the clan is not present.
    @Test
    fun testEventUntrackedClan() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val untrackedClan = Clan("TestClan", leader, members= mutableSetOf(member))
        val clanSet = ClanSet(setOf())

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.DEMOTE, ClanListenerType.MODEL, clanSet.constructDemoteListener())
        bus.registerListener(ClanEvent.DISBAND, ClanListenerType.MODEL, clanSet.constructDisbandListener())
        bus.registerListener(ClanEvent.JOIN, ClanListenerType.MODEL, clanSet.constructJoinListener())
        bus.registerListener(ClanEvent.KICK, ClanListenerType.MODEL, clanSet.constructKickListener())
        bus.registerListener(ClanEvent.LEAVE, ClanListenerType.MODEL, clanSet.constructLeaveListener())
        bus.registerListener(ClanEvent.PROMOTE, ClanListenerType.MODEL, clanSet.constructPromoteListener())
        bus.registerListener(ClanEvent.CORONATE, ClanListenerType.MODEL, clanSet.constructCoronateListener())

        // For each event, fire it with a clan not in this list. It should throw NoSuchElementException.
        ClanEvent.entries.forEach {
            // Ignore create event -- by definition, it is not tracked yet.
            if (it != ClanEvent.CREATE) {
                assertThrows(NoSuchElementException::class.java) { bus.fireEvent(it, untrackedClan, leader, member)}
            }
        }
    }
}