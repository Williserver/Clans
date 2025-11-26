package net.williserver.clans.session

import net.williserver.clans.model.clan.Clan
import java.util.*

// TODO: clan member lifecycle

/**
 * Events in the clan lifecycle to which listeners can be registered.
 * @author Willmo3
 */
enum class ClanLifecycleEvent {
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
 * Options to listen for changes to.
 * @author Willmo3
 */
enum class ClanOptionEvent {
    SETCOLOR,
    SETPREFIX,
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
 * Major lifecycle function, registered to a specific event.
 * Clan and agent parameters are provided -- additional required state should be closed around.
 *
 * @param clan Clan to which the event is occuring.
 * @param agent Player performing the action in the clan lifecycle, or none -- may be server initiated.
 * @param target Player affected by action.
 */
typealias ClanLifecycleListener = (clan: Clan, agent: UUID, target: UUID) -> Unit

/**
 * Clan option modification listener.
 *
 * @param clan Clan to set option for
 * @param agent Player performing the action in the clan lifecycle -- should have permission set.
 * @param option Option written to
 */
typealias ClanOptionListener = (clan: Clan, agent: UUID, value: String) -> Unit

/**
 * Event bus for major events in a clan's lifecycle.
 * - Listeners registered here will fire in the order defined in ClanListenerType.
 * - Within a given type, no ordering guarantees are imposed.
 *
 * @author Willmo3
 */
class ClanEventBus {
    /**
     * Listeners for key events in the clan's lifecycle. They are registered under:
     * - the event they target (ClanLifecycleEvent)
     * - the type of side effect they impose (ClanListenerType)
     */
    private val lifecycleListeners = hashMapOf<Pair<ClanLifecycleEvent, ClanListenerType>, MutableSet<ClanLifecycleListener>>()

    /**
     * Listeners for changes in clan options. They are registered under:
     * - the option they target (ClanOptionEvent)
     * - the type of side effect they impose. (ClanListenerType)
     */
    private val optionListeners = hashMapOf<Pair<ClanOptionEvent, ClanListenerType>, MutableSet<ClanOptionListener>>()

    init {
        // Initialize set of listeners for each type of clan event.
        ClanLifecycleEvent.entries.forEach { event ->
            ClanListenerType.entries.forEach { listenerType ->
                lifecycleListeners[Pair(event, listenerType)] = mutableSetOf()
            }
        }
        ClanOptionEvent.entries.forEach { event ->
            ClanListenerType.entries.forEach { listenerType ->
                optionListeners[Pair(event, listenerType)] = mutableSetOf()
            }
        }
    }

    /**
     * Register listener for a lifecycle event.
     *
     * @param event Event in clan lifecycle to register listener for.
     * @param type type of impact listener will have on server state.
     * @param listener Listener to register.
     * @return Whether the listener was registered -- i.e. an identical listener was not already registered.
     */
    fun registerListener(event: ClanLifecycleEvent, type: ClanListenerType, listener: ClanLifecycleListener) =
        if (listener !in lifecycleListeners[Pair(event, type)]!!) {
            lifecycleListeners[Pair(event, type)]!! += listener
            true
        } else false

    /**
     * Register listener for an option event.
     *
     * @param option Option change to listen for.
     * @param type Type of impact listener will have on server state.
     * @param listener Listener to register.
     * @return Whether the listener was registered -- i.e. an identical listener was not already registered.
     */
    fun registerListener(option: ClanOptionEvent, type: ClanListenerType, listener: ClanOptionListener) =
        if (listener !in optionListeners[Pair(option, type)]!!) {
            optionListeners[Pair(option, type)]!! += listener
            true
        } else false

    /**
     * Notify all lifecycle listeners that an event has occured.
     *
     * @param event Event that has just occured.
     * @param clan Clan affected by event.
     * @param agent player who initiated event.
     * @param target player affected by the event -- may be same as agent.
     */
    fun fireEvent(event: ClanLifecycleEvent, clan: Clan, agent: UUID, target: UUID) {
        // Ensure that listeners are fired in order using explicit for loop
        for (i in ClanListenerType.entries.indices) {
            lifecycleListeners[Pair(event, ClanListenerType.entries[i])]!!.forEach { it(clan, agent, target) }
        }
    }

    /**
     * Notify all option listeners that a set has occured.
     *
     * @param option Option that has just been set
     * @param clan Clan receiving option change
     * @param agent Player who initiated change
     * @param option String value for changed option.
     */
    fun fireEvent(option: ClanOptionEvent, clan: Clan, agent: UUID, value: String) {
        // Ensure that listeners are fired in order using explicit for loop
        for (i in ClanListenerType.entries.indices) {
            optionListeners[Pair(option, ClanListenerType.entries[i])]!!.forEach { it(clan, agent, value) }
        }
    }
}