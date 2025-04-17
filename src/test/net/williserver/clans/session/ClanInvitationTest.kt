package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.TimedClanInvitation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class ClanInvitationTest {
    @Test
    fun testConstructTimed() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader));
        val timer = ConfirmTimer(1)
        TimedClanInvitation(UUID.randomUUID(), clan, timer)
    }

    @Test
    fun testRejectRunningTimer() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val timer = ConfirmTimer(1)
        timer.startTimer()
        assertThrows(IllegalStateException::class.java) { TimedClanInvitation(UUID.randomUUID(), clan, timer) }
    }

    @Test
    fun testValidTimedInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        // One invitation will be created with a 10 second long timer
        // this is more than enough time for the invite to be valid.
        val longTimer = ConfirmTimer(10)
        assert(TimedClanInvitation(UUID.randomUUID(), clan, longTimer).validInvite())
        val shortTimer = ConfirmTimer(0)
        val expiredInvite = TimedClanInvitation(UUID.randomUUID(), clan, shortTimer)
        assertFalse(expiredInvite.validInvite())
    }
}