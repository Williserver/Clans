package net.williserver.clans.session

import net.williserver.clans.model.Clan
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
    fun testTimerInBounds() {
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
    fun testTimerNotInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader, mutableListOf(leader))
        val session = SessionManager()

        session.registerClanDisbandTimer(clan, 0)
        session.startClanDisbandTimer(clan)
        assertFalse(session.checkDisbandTimerInBounds(clan))
    }

    // TODO: tests for deleting clans.
    // TODO: tests for inviting new members to clans.
}