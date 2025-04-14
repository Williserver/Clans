package net.williserver.clans.model

import net.williserver.clans.LogHandler
import net.williserver.clans.pluginMessagePrefix
import org.bukkit.configuration.file.FileConfiguration

/*
 * Minimum time bound between initiating and confirming a clan disband.
 */
const val MINIMUM_DISBAND_TIME = 8u

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
    private val confirmDisbandOption = "confirmDisbandTime"

    // Loaded fields contained in final tiersConfig.
    // This may be accessed externally.
    val config: ClansConfig

    init {
        // Load config file.
        val loadedDisbandTime = fileConfig.getInt(confirmDisbandOption)
        val confirmDisbandTime = if (loadedDisbandTime > 0 && loadedDisbandTime.toUInt() > MINIMUM_DISBAND_TIME) {
            loadedDisbandTime.toUInt()
        } else {
            handler.err("$pluginMessagePrefix: Disband confirmation time $loadedDisbandTime is less than minimum time $MINIMUM_DISBAND_TIME, using $MINIMUM_DISBAND_TIME")
            MINIMUM_DISBAND_TIME
        }
        config = ClansConfig(confirmDisbandTime)
    }
}

/**
 * ClansConfig data should be interfaced outside of File IO.
 * Therefore, ClansConfig class is passed as opposed to TiersConfigLoader.
 *
 * @param confirmDisbandTime Maximum time between initiating and confirming a clan disband.
 * Stored as UInt to guarantee unsigned, even though most fields will use this a long.
 */
data class ClansConfig
(
    val confirmDisbandTime: UInt
)