package net.williserver.clans.model

import net.williserver.clans.model.clan.Clan
import net.williserver.clans.model.clan.ClanData
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ClanTest {
    private fun generateValidClan(): Clan {
        val leader = UUID.randomUUID().toString()
        val data = ClanData("TestClan",
            members=listOf(leader, UUID.randomUUID().toString()),
            coLeaders=listOf(),
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
            members=listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
            coLeaders=listOf(),
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
        assert(net.williserver.clans.model.clan.validClanName("123Test-Clan_"))
        assert(!net.williserver.clans.model.clan.validClanName("\"\""))
        assert(!net.williserver.clans.model.clan.validClanName("{123}"))
        assert(!net.williserver.clans.model.clan.validClanName(""))
        assert(!net.williserver.clans.model.clan.validClanName(" "))
    }

}