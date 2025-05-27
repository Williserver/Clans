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

    companion object {
        /**
         * @param name Name to find corresponding object for.
         * @return The corresponding clan option, or null if none.
         */
        fun optionFromName(name: String) = entries.find { it.name.equals(name, ignoreCase = true) }
    }

}