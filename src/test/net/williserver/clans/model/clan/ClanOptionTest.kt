package net.williserver.clans.model.clan

import net.kyori.adventure.text.format.NamedTextColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * @author Willmo3
 */
class ClanOptionTest {
    @Test
    fun testPrefixWidth() {
        val option = ClanOption.PREFIX
        assert(option.validOption("Tag"))
        assertFalse(option.validOption(""))
        assertFalse(option.validOption("LongTag"))
    }

    @Test
    fun testPrefixFailsNonString() {
        val option = ClanOption.PREFIX
        assertFalse(option.validOption(123))
    }

    @Test
    fun testColorStringMatchesNamedTextColor() {
        val option = ClanOption.COLOR
        assert(option.validOption("red"))
        assertFalse(option.validOption("notacolor"))
        assertFalse(option.validOption(123))
        assertFalse(option.validOption(""))
    }

    @Test
    fun prefixFromName() {
        var result = ClanOption.optionFromName("PREFIX")
        assertEquals(ClanOption.PREFIX, result)
        result = ClanOption.optionFromName("prEfix")
        assertEquals(ClanOption.PREFIX, result)
    }

    @Test
    fun colorFromName() {
        var result = ClanOption.optionFromName("COLOR")
        assertEquals(ClanOption.COLOR, result)
        result = ClanOption.optionFromName("coLoR")
        assertEquals(ClanOption.COLOR, result)
    }

    @Test
    fun invalidFromName() {
        var result = ClanOption.optionFromName("INVALID")
        assertNull(result)
        result = ClanOption.optionFromName("")
        assertNull(result)
    }

    @Test
    fun testDefaultPrefix() {
        val clan = Clan(name = "TestClan", leader = SUUID.randomUUID())
        val defaultPrefix = ClanOption.PREFIX.default(clan)
        assertEquals("TES", defaultPrefix)
    }

    @Test
    fun testDefaultColor() {
        val clan = Clan(name = "TestClan", leader = SUUID.randomUUID())
        val defaultColor = ClanOption.COLOR.default(clan)
        assertEquals("gray", defaultColor)
    }
}