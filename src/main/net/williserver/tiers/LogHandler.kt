package net.williserver.tiers

import java.util.logging.Logger

class LogHandler(private val logger: Logger?) {
    fun err(message: String) {
        logger?.warning(message) ?: println(message)
    }

    fun info(message: String) {
        logger?.info(message) ?: println(message)
    }
}