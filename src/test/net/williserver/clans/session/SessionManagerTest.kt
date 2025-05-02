package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
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
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        // Duplicate registration is illegal
        // but because it may be easier to call registerTimer repeatedly than add conditional logic,
        // it does not throw exception, it simply ignores the registration and returns false
        assert(session.registerTimer(ClanEvent.DISBAND, clan, 30))
        assertFalse(session.registerTimer(ClanEvent.DISBAND, clan, 25))
    }

    @Test
    fun testDisbandTimerInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        // the timer must first be registered!
        assertThrows(NullPointerException::class.java) { session.startTimer(ClanEvent.DISBAND, clan) }
        assertFalse(session.isTimerInBounds(ClanEvent.DISBAND, clan))
        assertFalse(session.isTimerRegistered(ClanEvent.DISBAND, clan))
        session.registerTimer(ClanEvent.DISBAND, clan, 30)
        // the timer has not started running!
        assertFalse(session.isTimerInBounds(ClanEvent.DISBAND, clan))
        session.startTimer(ClanEvent.DISBAND, clan)
        assert(session.isTimerInBounds(ClanEvent.DISBAND, clan))
    }

    @Test
    fun testDisbandTimerNotInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        session.registerTimer(ClanEvent.DISBAND, clan, 0)
        session.startTimer(ClanEvent.DISBAND, clan)
        assertFalse(session.isTimerInBounds(ClanEvent.DISBAND, clan))
    }

    @Test
    fun testAddInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        val rando = UUID.randomUUID()
        assertFalse(session.isTimerRegistered(ClanEvent.JOIN, Pair(rando, clan)))
        // An invite with an expired timer fails!
        session.registerTimer(ClanEvent.JOIN, Pair(rando, clan), 0)
        assert(session.isTimerRegistered(ClanEvent.JOIN, Pair(rando, clan)))
        assertFalse(session.isTimerInBounds(ClanEvent.JOIN, Pair(rando, clan)))

        session.deregisterTimer(ClanEvent.JOIN, Pair(rando, clan))
        session.registerTimer(ClanEvent.JOIN, Pair(rando, clan), 10)
        session.startTimer(ClanEvent.JOIN, Pair(rando, clan))
        assert(session.isTimerInBounds(ClanEvent.JOIN, Pair(rando, clan)))
    }

    // TODO: tests for deleting clans.
}