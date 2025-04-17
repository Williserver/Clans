package net.williserver.clans.session

import net.williserver.clans.model.ClanList
import org.junit.jupiter.api.Test

class ClanInvitationListTest {
    @Test
    fun testConstruct() {
        val clanList = ClanList(ArrayList())
        ClanInvitationList(clanList);
    }
}