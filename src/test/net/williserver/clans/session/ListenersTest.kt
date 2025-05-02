package net.williserver.clans.session

import net.williserver.clans.model.ClanSet
import net.williserver.clans.model.clan.Clan
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
        bus.registerListener(ClanEvent.DISBAND, clanSet.constructDisbandListener())
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
        bus.registerListener(ClanEvent.CREATE, clanSet.constructCreateListener())
        assertThrows(IllegalArgumentException::class.java) { bus.fireEvent(ClanEvent.CREATE, clan, UUID.randomUUID(), UUID.randomUUID()) }

        bus.fireEvent(ClanEvent.CREATE, clan, clan.leader, clan.leader)
        assert(clan.name in clanSet)
    }

    @Test
    fun testLeaveUntrackedClan() {
        val leader = UUID.randomUUID()
        val member = UUID.randomUUID()
        val trackedClan = Clan("TestClan", leader, members= mutableSetOf(leader, member))

        val untrackedClan = Clan("OtherClan", leader, members = mutableSetOf(leader, member))
        val clanSet = ClanSet(setOf(trackedClan.asDataTuple()))

        val bus = ClanEventBus()
        bus.registerListener(ClanEvent.LEAVE, clanSet.constructLeaveListener())
        assertThrows(IllegalArgumentException::class.java) {bus.fireEvent(ClanEvent.LEAVE, untrackedClan, member, member)}

        bus.fireEvent(ClanEvent.LEAVE, trackedClan, member, member)
        assert(member !in trackedClan)
    }
}