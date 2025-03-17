package net.williserver.tiers

import org.bukkit.plugin.java.JavaPlugin

/**
 * TeamsPlugin. A lightweight predecessor to Polis, designed for user-oriented teams management.
 *
 * @author Willmo3
 */
class TeamsPlugin : JavaPlugin() {
    private val handler = LogHandler(super.getLogger())

    override fun onEnable() {
        handler.info("Enabled")
    }

    override fun onDisable() {
        handler.info("Disabled")
    }
}