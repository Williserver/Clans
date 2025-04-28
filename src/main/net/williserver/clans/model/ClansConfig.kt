package net.williserver.clans.model

import net.williserver.clans.LogHandler
import org.bukkit.configuration.file.FileConfiguration

/*
 * Minimum time bound between initiating and confirming a destructive action.
 */
const val MINIMUM_CONFIRM_TIME = 8u

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

    // ***** CONFIG FIELDS ***** //
    private val confirmTimeOption = "confirmTime"
    private val teamsIntegrationOption = "scoreboardTeamsIntegration"

    // Loaded fields contained in final tiersConfig.
    // This may be accessed externally.
    val config: ClansConfig

    init {
        // Load config file.
        val loadedConfirmTime = fileConfig.getInt(confirmTimeOption)
        val confirmTime = if (loadedConfirmTime > 0 && loadedConfirmTime.toUInt() > MINIMUM_CONFIRM_TIME) {
            loadedConfirmTime.toUInt()
        } else {
            handler.err("Confirmation time $loadedConfirmTime is less than minimum time $MINIMUM_CONFIRM_TIME, using $MINIMUM_CONFIRM_TIME")
            MINIMUM_CONFIRM_TIME
        }

        val scoreboardTeamsIntegration = fileConfig.getBoolean(teamsIntegrationOption)
        config = ClansConfig(confirmTime, scoreboardTeamsIntegration)
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
    val scoreboardTeamsIntegration: Boolean
)