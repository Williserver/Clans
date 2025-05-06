package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import java.util.*

/**
 * Events in the clan lifecycle to which listeners can be registered.
 * @author Willmo3
 */
enum class ClanEvent {
    CREATE,
    JOIN,
    LEAVE,
    KICK,
    DISBAND,
    PROMOTE,
    DEMOTE,
}

/**
 * Listener function, registered to a specific event.
 * Clan and agent parameters are provided -- additional required state should be closed around.
 * @param clan Clan to which the event is occuring.
 * @param agent Player performing the action in the clan lifecycle, or none -- may be server initiated.
 * @param target Player affected by action.
 */
typealias ClanLifecycleListener = (clan: Clan, agent: UUID, target: UUID) -> Unit

/**
 * Event bus for major events in a clan's lifecycle.
 * Listeners registered here will interact with the model.
 *
 * @author Willmo3
 */
class ClanEventBus {
    private val listeners = hashMapOf<ClanEvent, MutableSet<ClanLifecycleListener>>()
    init {
        // Initialize set of listeners for each type of clan event.
        ClanEvent.entries.forEach { event -> listeners[event] = mutableSetOf() }
    }

    /**
     * @param event Event in clan lifecycle to register listener for.
     * @param listener Listener function for a given clan.
     * @return Whether the listener was registered -- i.e. an identical listener was not already registered.
     */
    fun registerListener(event: ClanEvent, listener: ClanLifecycleListener) =
        if (listener !in listeners[event]!!) {
            listeners[event]!! += listener
            true
        } else false

    /**
     * Notify all listeners that an event has occured.
     *
     * @param event Event that has just occured.
     * @param clan Clan affected by event.
     * @param agent player who initiated event.
     * @param target player affected by the event -- may be same as agent.
     */
    fun fireEvent(event: ClanEvent, clan: Clan, agent: UUID, target: UUID) {
        listeners[event]!!.forEach { it(clan, agent, target) }
    }
}

