package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.ClanInvitation
import net.williserver.clans.session.invite.TimedClanInvitation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class ClanInvitationTest {
    @Test
    fun testConstructTimed() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader));
        TimedClanInvitation(UUID.randomUUID(), clan, 1)
    }

    @Test
    fun testValidTimedInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        // One invitation will be created with a 10 second long timer
        // this is more than enough time for the invite to be valid.
        assert(TimedClanInvitation(UUID.randomUUID(), clan, 10).validInvite())
        val shortTimer = ConfirmTimer(0)
        val expiredInvite = TimedClanInvitation(UUID.randomUUID(), clan, 0)
        assertFalse(expiredInvite.validInvite())
    }
}