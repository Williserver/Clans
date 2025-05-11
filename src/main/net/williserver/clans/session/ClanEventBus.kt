package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import java.util.*

// TODO: clan member lifecycle

/**
 * Events in the clan lifecycle to which listeners can be registered.
 * @author Willmo3
 */
enum class ClanEvent {
    CREATE,
    CORONATE,
    JOIN,
    LEAVE,
    KICK,
    DISBAND,
    PROMOTE,
    DEMOTE,
}

/**
 * Type of side effect listener imposes, ordered from most to least destructive:
 * - Model: affects underlying data model. Will be fired first.
 * - Integration: affects integration with other plugins or features. Will be fired second.
 * - Session: affects temporary saved state, like timers. Will be fired third.
 * - Cosmetic: sends messsages to users. Will be fired last.
 *
 * @author Willmo3
 */
enum class ClanListenerType {
    MODEL,
    INTEGRATION,
    SESSION,
    COSMETIC
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
 * - Listeners registered here will fire in the order defined in ClanListenerType.
 * - Within a given type, no ordering guarantees are imposed.
 *
 * @author Willmo3
 */
class ClanEventBus {
    /**
     * Listeners that affect the underlying model. They are registered under:
     * - the event they target (ClanEventType)
     * - the type of side effect they impose (ClanListenerType)
     */
    private val listeners = hashMapOf<Pair<ClanEvent, ClanListenerType>, MutableSet<ClanLifecycleListener>>()
    init {
        // Initialize set of listeners for each type of clan event.
        ClanEvent.entries.forEach { event ->
            ClanListenerType.entries.forEach { listenerType ->
                listeners[Pair(event, listenerType)] = mutableSetOf()
            }
        }
    }

    /**
     * @param event Event in clan lifecycle to register listener for.
     * @param type type of impact listener will have on server state.
     * @param listener Listener function for a given clan.
     * @return Whether the listener was registered -- i.e. an identical listener was not already registered.
     */
    fun registerListener(event: ClanEvent, type: ClanListenerType, listener: ClanLifecycleListener) =
        if (listener !in listeners[Pair(event, type)]!!) {
            listeners[Pair(event, type)]!! += listener
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
        // Ensure that listeners are fired in order using explicit for loop/
        for (i in ClanListenerType.entries.indices) {
            listeners[Pair(event, ClanListenerType.entries[i])]!!.forEach { it(clan, agent, target) }
        }
    }
}