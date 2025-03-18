package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Willmo3
 */
class ClanTest {
    private val logHandler = LogHandler(null);

    @Test
    fun constructValidClan() {
        val leader = UUID.randomUUID()
        val data = ClanData("TestClan",
            listOf(leader, UUID.randomUUID()),
            leader)
        Clan(logHandler, data)
    }
}