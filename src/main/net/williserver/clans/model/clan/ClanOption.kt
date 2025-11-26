package net.williserver.clans.model.clan

import net.kyori.adventure.text.format.NamedTextColor

/**
 * Represents different options associated with a clan.
 * @author Willmo3
 */
enum class ClanOption {
    PREFIX,
    COLOR;

    /***
     * @param value Value for a Clan option
     * @return Whether that value is valid for a given option.
     */
    fun validOption(value: Any) =
        when (this) {
            PREFIX -> value is String && value.isNotEmpty() && value.length <= 3
            COLOR -> value is String && NamedTextColor.NAMES.value(value) != null
        }

    /**
     * @return Description of valid values for this option.
     */
    fun validValuesDescription() =
        when (this) {
            PREFIX -> "A non-empty string of up to $PREFIX_LENGTH characters"
            COLOR -> "A named color, e.g. 'red', 'blue', 'aqua', etc"
        }

    /**
     * @param clan Clan to generate defaults for.
     * @return The default value.
     */
    fun default(clan: Clan): String = when (this) {
        PREFIX -> clan.name.take(minOf(clan.name.length, PREFIX_LENGTH)).uppercase()
        COLOR -> NamedTextColor.GRAY.toString()
    }

    companion object {
        /**
         * Size of a clan team prefix.
         */
        const val PREFIX_LENGTH = 3

        /**
         * @param name Name to find corresponding object for.
         * @return The corresponding clan option, or null if none.
         */
        fun optionFromName(name: String) = entries.find { it.name.equals(name, ignoreCase = true) }
    }

}