@file:Suppress("UNUSED")

package xyz.meowing.zen.api.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import tech.thatgravyboat.skyblockapi.utils.json.Json
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import net.fabricmc.loader.api.FabricLoader
import xyz.meowing.zen.Zen
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.GameEvent
import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StoredFile(val path: String) {
    private val file: File = File(FabricLoader.getInstance().configDir.toFile(), "zen/$path.json").apply {
        parentFile.mkdirs()
    }

    private var root: JsonObject? = null

    init {
        EventBus.register<GameEvent.Stop> { save() }
    }

    private fun load(): JsonObject {
        if (root != null) return root!!
        return try {
            if (!file.exists()) {
                JsonObject()
            } else {
                val content = file.readText()
                if (content.isBlank()) {
                    JsonObject()
                } else {
                    val element = JsonParser.parseString(content)
                    if (element.isJsonNull || !element.isJsonObject) {
                        JsonObject()
                    } else {
                        element.asJsonObject
                    }
                }
            }
        } catch (e: Exception) {
            Zen.LOGGER.error("Caught exception while trying to load StoredFile for $path: $e")
            JsonObject()
        }.also { root = it }
    }

    private fun save() {
        try {
            val data = root ?: load()
            file.writeText(Json.gson.toJson(data))
        } catch (e: Exception) {
            Zen.LOGGER.error("Caught exception while trying to save StoredFile for $path: $e")
        }
    }

    fun reload() {
        root = null
    }

    fun forceSave() = save()

    fun jsonObject(key: String, default: JsonObject = JsonObject()) = object : ReadWriteProperty<Any?, JsonObject> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): JsonObject {
            val obj = load()
            return if (obj.has(key)) obj.getAsJsonObject(key) else default
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: JsonObject) {
            val obj = load()
            obj.add(key, value)
        }
    }

    inner class Value<T : Any>(
        private val key: String,
        private val default: T,
        private val codec: Codec<T>
    ) : ReadWriteProperty<Any?, T> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val obj = load()
            return obj.get(key)?.toData(codec) ?: default
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            val obj = load()
            value.toJson(codec)?.let {
                obj.add(key, it)
            }
        }
    }

    fun int(key: String, default: Int = 0) = Value(key, default, Codec.INT)
    fun long(key: String, default: Long = 0L) = Value(key, default, Codec.LONG)
    fun string(key: String, default: String = "") = Value(key, default, Codec.STRING)
    fun boolean(key: String, default: Boolean = false) = Value(key, default, Codec.BOOL)
    fun double(key: String, default: Double = 0.0) = Value(key, default, Codec.DOUBLE)
    fun float(key: String, default: Float = 0f) = Value(key, default, Codec.FLOAT)

    fun <T : Any> list(key: String, codec: Codec<T>, default: List<T> = emptyList()) =
        Value(key, default, codec.listOf())

    fun <T : Any> set(key: String, codec: Codec<T>, default: Set<T> = emptySet()) =
        Value(key, default, codec.listOf().xmap({ it.toSet() }, { it.toList() }))

    fun <K : Any, V : Any> map(key: String, keyCodec: Codec<K>, valueCodec: Codec<V>, default: Map<K, V> = emptyMap()) =
        Value(key, default, Codec.unboundedMap(keyCodec, valueCodec))
}

class StoredJsonObject(default: JsonObject = JsonObject(), path: String) : ReadWriteProperty<Any?, JsonObject> {
    private val file = StoredFile(path)
    private val value = file.jsonObject("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): JsonObject = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: JsonObject) = this.value.setValue(thisRef, property, value)
}

class StoredInt(default: Int = 0, path: String) : ReadWriteProperty<Any?, Int> {
    private val file = StoredFile(path)
    private val value = file.int("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) = this.value.setValue(thisRef, property, value)
}

class StoredLong(default: Long = 0L, path: String) : ReadWriteProperty<Any?, Long> {
    private val file = StoredFile(path)
    private val value = file.long("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Long = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) = this.value.setValue(thisRef, property, value)
}

class StoredString(default: String = "", path: String) : ReadWriteProperty<Any?, String> {
    private val file = StoredFile(path)
    private val value = file.string("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): String = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) = this.value.setValue(thisRef, property, value)
}

class StoredBoolean(default: Boolean = false, path: String) : ReadWriteProperty<Any?, Boolean> {
    private val file = StoredFile(path)
    private val value = file.boolean("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) = this.value.setValue(thisRef, property, value)
}

class StoredDouble(default: Double = 0.0, path: String) : ReadWriteProperty<Any?, Double> {
    private val file = StoredFile(path)
    private val value = file.double("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Double = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) = this.value.setValue(thisRef, property, value)
}

class StoredFloat(default: Float = 0f, path: String) : ReadWriteProperty<Any?, Float> {
    private val file = StoredFile(path)
    private val value = file.float("value", default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Float = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) = this.value.setValue(thisRef, property, value)
}

class StoredList<T : Any>(default: List<T> = emptyList(), path: String, codec: Codec<T>) : ReadWriteProperty<Any?, List<T>> {
    private val file = StoredFile(path)
    private val value = file.list("value", codec, default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) = this.value.setValue(thisRef, property, value)
}

class StoredSet<T : Any>(default: Set<T> = emptySet(), path: String, codec: Codec<T>) : ReadWriteProperty<Any?, Set<T>> {
    private val file = StoredFile(path)
    private val value = file.set("value", codec, default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Set<T> = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<T>) = this.value.setValue(thisRef, property, value)
}

class StoredMap<K : Any, V : Any>(default: Map<K, V> = emptyMap(), path: String, keyCodec: Codec<K>, valueCodec: Codec<V>) : ReadWriteProperty<Any?, Map<K, V>> {
    private val file = StoredFile(path)
    private val value = file.map("value", keyCodec, valueCodec, default)
    override fun getValue(thisRef: Any?, property: KProperty<*>): Map<K, V> = value.getValue(thisRef, property)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Map<K, V>) = this.value.setValue(thisRef, property, value)
}