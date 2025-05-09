package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.bukkit.configuration.file.FileConfiguration

/**
 * Responsible for parsing config options and restoring defaults, if need be.
 * Interfaces into a read-only data class.
 *
 * @param handler Logger
 * @param fileConfig Configuration file to parse options from.
 * @author Willmo3
 */
class ClansConfigLoader(private val handler: LogHandler,
                        private val fileConfig: FileConfiguration) {

    companion object {
        /**
         * Minimum time bound between initiating and confirming a destructive action.
         */
        const val MINIMUM_CONFIRM_TIME = 8u

        /**
         * Default name for LuckPerms clan rank track.
         */
        private const val DEFAULT_TRACK_NAME = "clans"

        // ***** NAMES FOR CONFIG FIELDS ***** //
        private const val CONFIRM_TIME_OPTION = "confirmTime"
        private const val TEAMS_INTEGRATION_OPTION = "scoreboardTeamsIntegration"
        private const val LUCKPERMS_INTEGRATION_OPTION = "luckPermsIntegration"
        private const val LUCKPERMS_TRACKNAME_OPTION = "luckPermsTrackName"
    }

    // Loaded fields contained in final tiersConfig.
    // This may be accessed externally.
    val config: ClansConfig

    init {
        // Load confirmation time.
        val loadedConfirmTime = fileConfig.getInt(CONFIRM_TIME_OPTION)
        val confirmTime = if (loadedConfirmTime > 0 && loadedConfirmTime.toUInt() > MINIMUM_CONFIRM_TIME) {
            loadedConfirmTime.toUInt()
        } else {
            handler.err("Confirmation time $loadedConfirmTime is less than minimum time $MINIMUM_CONFIRM_TIME, using $MINIMUM_CONFIRM_TIME")
            MINIMUM_CONFIRM_TIME
        }

        // Load integration options.
        val scoreboardTeamsIntegration = fileConfig.getBoolean(TEAMS_INTEGRATION_OPTION)
        val luckPermsIntegration = fileConfig.getBoolean(LUCKPERMS_INTEGRATION_OPTION)
        val luckPermsTrackName = fileConfig.getString(LUCKPERMS_TRACKNAME_OPTION)
            ?: run { handler.err("Invalid LuckPerms track name provided, using $DEFAULT_TRACK_NAME"); DEFAULT_TRACK_NAME }

        // Finalize config.
        config = ClansConfig(confirmTime, scoreboardTeamsIntegration, luckPermsIntegration, luckPermsTrackName)
    }
}

/**
 * ClansConfig data should be interfaced outside of File IO.
 * Therefore, ClansConfig class is passed as opposed to TiersConfigLoader.
 *
 * @param confirmTime Maximum time between initiating and confirming a destructive action via command.
 * Stored as UInt to guarantee unsigned, even though most fields will use this a long.
 */
data class ClansConfig
(
    val confirmTime: UInt,
    val scoreboardTeamsIntegration: Boolean,
    val luckpermsIntegration: Boolean,
    val luckPermsTrackName: String,
)