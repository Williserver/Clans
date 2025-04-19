package net.williserver.clans.session

import net.williserver.clans.model.Clan
import net.williserver.clans.session.invite.TimedClanInvitation
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class SessionManagerTest {
    @Test
    fun testRegisterClanDisbandTimer() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val session = SessionManager()

        // Duplicate registration is illegal
        // but because it may be easier to call registerTimer repeatedly than add conditional logic,
        // it does not throw exception, it simply ignores the registration and returns false
        assert(session.registerClanDisbandTimer(clan, 30))
        assertFalse(session.registerClanDisbandTimer(clan, 25))
    }

    @Test
    fun testDisbandTimerInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val session = SessionManager()

        // the timer must first be registered!
        assertThrows(NullPointerException::class.java) { session.startClanDisbandTimer(clan) }
        assertThrows(NullPointerException::class.java) { session.checkDisbandTimerInBounds(clan) }
        assertFalse(session.clanDisbandTimerRegistered(clan))
        session.registerClanDisbandTimer(clan, 30)
        // the timer has not started running!
        assertFalse(session.checkDisbandTimerInBounds(clan))
        session.startClanDisbandTimer(clan)
        assert(session.checkDisbandTimerInBounds(clan))
    }

    @Test
    fun testDisbandTimerNotInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val session = SessionManager()

        session.registerClanDisbandTimer(clan, 0)
        session.startClanDisbandTimer(clan)
        assertFalse(session.checkDisbandTimerInBounds(clan))
    }

    @Test
    fun testAddInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val session = SessionManager()

        val rando = UUID.randomUUID()
        assertFalse(session.activeClanInvite(rando, clan))
        // An invite with an expired timer fails!
        session.addClanInvite(TimedClanInvitation(rando, clan, 0))
        assertFalse(session.activeClanInvite(rando, clan))
        session.addClanInvite(TimedClanInvitation(rando, clan, 10))
        assert(session.activeClanInvite(rando, clan))
    }

    // TODO: tests for deleting clans.
}