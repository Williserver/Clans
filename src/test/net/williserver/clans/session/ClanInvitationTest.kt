package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.TimedClanInvitation
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
}