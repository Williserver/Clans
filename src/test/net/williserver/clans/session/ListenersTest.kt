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
    }
}