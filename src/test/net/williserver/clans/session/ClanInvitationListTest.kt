package net.williserver.clans.session

import net.williserver.clans.model.ClanList
import net.williserver.clans.session.invite.ClanInvitationList
import org.junit.jupiter.api.Test

class ClanInvitationListTest {
    @Test
    fun testConstruct() {
        val clanList = ClanList(ArrayList())
        ClanInvitationList(clanList);
    }
}