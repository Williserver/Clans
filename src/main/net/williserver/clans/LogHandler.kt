package net.williserver.clans

import java.util.logging.Logger

/**
 * Utility wrapper to allow logging outside Bukkit server environments.
 * @author Willmo3
 */
class LogHandler(private val logger: Logger?) {
    /**
     * Report an error to server console or to stderr if no console present.
     * @param message Message to report.
     */
    fun err(message: String) {
        logger?.warning(message) ?: System.err.println("$pluginMessagePrefix: $message")
    }

    /**
     * Report a message to server console or to stdout if no console present.
     * @param message Message to report.
     */
    fun info(message: String) {
        logger?.info(message) ?: println("$pluginMessagePrefix: $message")
    }
}