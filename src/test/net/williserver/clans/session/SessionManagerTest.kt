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
        assert(session.registerTimer(ClanLifecycleEvent.DISBAND, clan, 30))
        assertFalse(session.registerTimer(ClanLifecycleEvent.DISBAND, clan, 25))
    }

    @Test
    fun testDisbandTimerInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        // the timer must first be registered!
        assertThrows(NullPointerException::class.java) { session.startTimer(ClanLifecycleEvent.DISBAND, clan) }
        assertFalse(session.isTimerInBounds(ClanLifecycleEvent.DISBAND, clan))
        assertFalse(session.isTimerRegistered(ClanLifecycleEvent.DISBAND, clan))
        session.registerTimer(ClanLifecycleEvent.DISBAND, clan, 30)
        // the timer has not started running!
        assertFalse(session.isTimerInBounds(ClanLifecycleEvent.DISBAND, clan))
        session.startTimer(ClanLifecycleEvent.DISBAND, clan)
        assert(session.isTimerInBounds(ClanLifecycleEvent.DISBAND, clan))
    }

    @Test
    fun testDisbandTimerNotInBounds() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        session.registerTimer(ClanLifecycleEvent.DISBAND, clan, 0)
        session.startTimer(ClanLifecycleEvent.DISBAND, clan)
        assertFalse(session.isTimerInBounds(ClanLifecycleEvent.DISBAND, clan))
    }

    @Test
    fun testAddInvitation() {
        val leader = UUID.randomUUID()
        val clan = Clan("TestClan", leader)
        val session = SessionManager()

        val rando = UUID.randomUUID()
        assertFalse(session.isTimerRegistered(ClanLifecycleEvent.JOIN, Pair(rando, clan)))
        // An invite with an expired timer fails!
        session.registerTimer(ClanLifecycleEvent.JOIN, Pair(rando, clan), 0)
        assert(session.isTimerRegistered(ClanLifecycleEvent.JOIN, Pair(rando, clan)))
        assertFalse(session.isTimerInBounds(ClanLifecycleEvent.JOIN, Pair(rando, clan)))

        session.deregisterTimer(ClanLifecycleEvent.JOIN, Pair(rando, clan))
        session.registerTimer(ClanLifecycleEvent.JOIN, Pair(rando, clan), 10)
        session.startTimer(ClanLifecycleEvent.JOIN, Pair(rando, clan))
        assert(session.isTimerInBounds(ClanLifecycleEvent.JOIN, Pair(rando, clan)))
    }
}