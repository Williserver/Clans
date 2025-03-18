package net.williserver.clans

import org.bukkit.plugin.java.JavaPlugin

/**
 * ClansPlugin. A lightweight predecessor to Polis, designed for user-oriented teams management.
 *
 * @author Willmo3
 */
class ClansPlugin : JavaPlugin() {
    private val handler = LogHandler(super.getLogger())

    override fun onEnable() {
        handler.info("Enabled")
    }

    override fun onDisable() {
        handler.info("Disabled")
    }
}