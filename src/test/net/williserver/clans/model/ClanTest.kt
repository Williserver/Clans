package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ClanTest {
    private val logHandler = LogHandler(null);

    @Test
    fun constructValidClan() {
        val leader = UUID.randomUUID().toString()
        val data = ClanData("TestClan",
            listOf(leader, UUID.randomUUID().toString()),
            leader)
        Clan(data)
    }

    @Test
    fun constructClanLeaderNotMember() {
        val data = ClanData("TestClan",
            listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
            UUID.randomUUID().toString())
        assertThrows(IllegalArgumentException::class.java) { Clan(data) }
    }
}