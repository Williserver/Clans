package net.williserver.clans

import net.williserver.clans.commands.ClansCommand
import net.williserver.clans.commands.ClansTabCompleter
import net.williserver.clans.model.ClanList
import net.williserver.clans.model.readFromFile
import net.williserver.clans.model.writeToFile
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * ClansPlugin. A lightweight predecessor to Polis, designed for user-oriented teams management.
 *
 * @author Willmo3
 */
class ClansPlugin : JavaPlugin() {
    private val handler = LogHandler(super.getLogger())
    // Default data path
    private val path = "$dataFolder${File.separator}clans.json"
    // clanList model.
    private lateinit var clanList: ClanList

    override fun onEnable() {
        // Note: even with an empty config, this is necessary to generate the data directory.
        saveDefaultConfig()

        // Read base clan list.
        clanList = readFromFile(path)

        // Register commands.
        this.getCommand("clans")!!.setExecutor(ClansCommand(handler, clanList))
        this.getCommand("clans")!!.tabCompleter = ClansTabCompleter()

        handler.info("Enabled")
    }

    override fun onDisable() {
        // Save model settings.
        writeToFile(path, clanList)

        handler.info("Disabled")
    }
}