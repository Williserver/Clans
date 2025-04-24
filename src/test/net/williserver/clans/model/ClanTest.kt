package net.williserver.clans.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * @author Willmo3
 */
class ClanTest {
    private fun generateValidClan(): Clan {
        val leader = UUID.randomUUID().toString()
        val data = ClanData("TestClan",
            listOf(leader, UUID.randomUUID().toString()),
            leader)
        return Clan(data)
    }

    @Test
    fun constructValidClan() {
        generateValidClan()
    }

    @Test
    fun constructClanLeaderNotMember() {
        val data = ClanData("TestClan",
            listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
            UUID.randomUUID().toString())
        assertThrows(IllegalArgumentException::class.java) { Clan(data) }
    }

    @Test
    fun joinClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        assert(newMember !in clan)
        clan.join(newMember)
        assert(newMember in clan)
        assertThrows(IllegalArgumentException::class.java) { clan.join(newMember) }
    }

    @Test
    fun leaveClan() {
        val clan = generateValidClan()
        val newMember = UUID.randomUUID()
        clan.join(newMember)
        assert(newMember in clan)
        clan.leave(newMember)
        assert(newMember !in clan)
        assertThrows(IllegalArgumentException::class.java) { clan.leave(newMember) }
        assertThrows(IllegalArgumentException::class.java) { clan.leave(clan.leader) }
    }

    @Test
    fun validClanName() {
        assert(validClanName("123Test-Clan_"))
        assert(!validClanName("\"\""))
        assert(!validClanName("{123}"))
        assert(!validClanName(""))
        assert(!validClanName(" "))
    }

}