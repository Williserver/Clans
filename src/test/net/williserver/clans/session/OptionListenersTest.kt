package net.williserver.clans.session

import net.williserver.clans.model.ClanSet
import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanOption
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * @author Willmo3
 */
class OptionListenersTest {
    @Test
    fun testSetColorListener() {
        val clan = Clan("TestClan", java.util.UUID.randomUUID())
        assert(clan.getOption("COLOR") == ClanOption.COLOR.default(clan))

        val clanList = ClanSet(mutableSetOf(clan))
        val bus = ClanEventBus()

        bus.registerListener(ClanOption.COLOR, ClanListenerType.MODEL, clanList.constructOptionListener())

        bus.fireEvent(ClanOption.COLOR, clan, clan.leader(), "red")
        assert(clan.getOption("COLOR") == "red")
    }

    @Test
    fun testSetPrefixListener() {
        val clan = Clan("TestClan", java.util.UUID.randomUUID())
        assert(clan.getOption("PREFIX") == ClanOption.PREFIX.default(clan))

        val clanList = ClanSet(mutableSetOf(clan))
        val bus = ClanEventBus()

        bus.registerListener(ClanOption.PREFIX, ClanListenerType.MODEL, clanList.constructOptionListener())

        bus.fireEvent(ClanOption.PREFIX, clan, clan.leader(), "TC")
        assert(clan.getOption("PREFIX") == "TC")

        assertThrows(IllegalArgumentException::class.java) {
            bus.fireEvent(ClanOption.PREFIX, clan, clan.leader(), "ThisPrefixIsWayTooLong")
        }
        assertThrows(IllegalArgumentException::class.java) {
            bus.fireEvent(ClanOption.PREFIX, clan, clan.leader(), "")
        }
    }
}