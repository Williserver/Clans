package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.ClanInvitation
import net.williserver.clans.session.invite.ClanInvitationList
import net.williserver.clans.session.invite.TimedClanInvitation
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse

class ClanInvitationListTest {
    @Test
    fun testConstruct() {
        ClanInvitationList()
    }

    @Test
    fun testAddActiveInvitation() {
        val invitations = ClanInvitationList()
        val leader = UUID.randomUUID()
        val rando = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))

        assertFalse(invitations.hasActiveInvitation(rando, clan))
        invitations.add(TimedClanInvitation(rando, clan, ConfirmTimer(10)))
        assert(invitations.hasActiveInvitation(rando, clan))
    }

    @Test
    fun testAddInactiveInvitation() {
        val invitations = ClanInvitationList()
        val leader = UUID.randomUUID()
        val rando = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))

        assertFalse(invitations.hasActiveInvitation(rando, clan))
        invitations.add(TimedClanInvitation(rando, clan, ConfirmTimer(0)))
        assertFalse(invitations.hasActiveInvitation(rando, clan))
    }

    @Test
    fun testDuplicateInvitation() {
        val invitations = ClanInvitationList()
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))

        val invite = ClanInvitation(UUID.randomUUID(), clan)
        invitations.add(invite)
        invitations.hasActiveInvitation(invite.player, clan)

    }
}