package net.williserver.clans.session

import net.williserver.clans.ClansPlugin.Companion.pluginMessagePrefix
import java.util.Date

/**
 * Represents an upper bound on delay between invocation. Used for confirmation commands, which may require the user
 * to confirm their intent multiple times within a set time bound.
 *
 * @param secondsToConfirm Maximum number of seconds that can elapse.
 */
class ConfirmTimer(private val secondsToConfirm: Long) {
    private var timerMaximum: Date? = null

    /**
     * Set the timerStartTime, beginning a maximum time countdown.
     */
    fun startTimer() {
        timerMaximum = Date(System.currentTimeMillis() + secondsToConfirm * 1000)
    }

    /**
     * @return Whether the current time is before hte threshold.
     */
    fun inBounds(): Boolean {
        if (!isRunning()) {
            throw IllegalStateException("$pluginMessagePrefix: Timer must be started before checking if in bounds!")
        }
        return Date(System.currentTimeMillis()).before(timerMaximum!!)
    }

    /**
     * Reset this timer. Must start it again before.
     */
    fun reset() {
        timerMaximum = null
    }

    /**
     * Check whether this timer is running.
     */
    fun isRunning() = timerMaximum != null
}