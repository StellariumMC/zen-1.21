package meowing.zen.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object EventBus {
    private val listeners = ConcurrentHashMap<Class<*>, ConcurrentHashMap<Any, Consumer<*>>>()

    @JvmStatic
    fun <T> register(eventType: Class<T>, owner: Any, handler: Consumer<T>) {
        listeners.computeIfAbsent(eventType) { ConcurrentHashMap() }[owner] = handler
    }

    @JvmStatic
    fun unregister(owner: Any) {
        listeners.values.forEach { map -> map.remove(owner) }
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> fire(event: T) {
        val handlers = listeners[event!!::class.java]
        if (handlers != null && handlers.isNotEmpty()) {
            handlers.values.parallelStream().forEach { handler ->
                try {
                    (handler as Consumer<T>).accept(event)
                } catch (ignored: Exception) {
                }
            }
        }
    }
}