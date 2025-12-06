package xyz.meowing.zen.config.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Config(
    private val configName: String,
    private val description: String,
    private val category: String,
    private val default: Boolean = false
) : ReadOnlyProperty<Any?, ConfigBuilder> {
    private var builder: ConfigBuilder? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): ConfigBuilder {
        if (builder == null) {
            builder = ConfigBuilder(
                property.name,
                configName,
                description,
                category,
                default
            )
        }
        return builder!!
    }
}
