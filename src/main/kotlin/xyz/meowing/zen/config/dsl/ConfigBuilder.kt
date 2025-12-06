@file:Suppress("UNUSED")

package xyz.meowing.zen.config.dsl

import xyz.meowing.zen.config.Handler
import xyz.meowing.zen.config.ui.elements.MCColorCode
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.config.FeatureElement
import java.awt.Color
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ConfigBuilder internal constructor(
    private val configKey: String,
    private val featureName: String,
    private val description: String,
    private val category: String,
    private val default: Boolean = false
) {
    private var featureElement: FeatureElement? = null
    private val mainHandler: Handler<Boolean> by lazy {
        Handler(configKey, Boolean::class.java)
    }

    fun feature(): FeatureElement {
        if (featureElement == null) {
            featureElement = ConfigManager
                .addFeature(
                    featureName,
                    description,
                    category,
                    ConfigElement(
                        configKey,
                        ElementType.Switch(default)
                    )
                )
        }
        return featureElement!!
    }

    operator fun invoke(): Boolean = mainHandler.getValue(null, ::mainHandler)

    fun switch(name: String, default: Boolean = false) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.Switch(default)))
            Handler(key, Boolean::class.java)
        }

    fun slider(name: String, default: Double, min: Double, max: Double, showDouble: Boolean = true) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.Slider(min, max, default, showDouble)))
            Handler(key, Double::class.java)
        }

    fun slider(name: String, default: Int, min: Int, max: Int) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.Slider(min.toDouble(), max.toDouble(), default.toDouble(), false)))
            Handler(key, Int::class.java)
        }

    fun textInput(name: String, default: String = "", placeholder: String = "") =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.TextInput(default, placeholder)))
            Handler(key, String::class.java)
        }

    fun dropdown(name: String, options: List<String>, default: Int = 0) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.Dropdown(options, default)))
            Handler(key, Int::class.java)
        }

    fun colorPicker(name: String, default: Color = Color(0, 255, 255, 127)) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.ColorPicker(default)))
            Handler(key, Color::class.java)
        }

    fun mcColorPicker(name: String, default: MCColorCode = MCColorCode.WHITE) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.MCColorPicker(default)))
            Handler(key, MCColorCode::class.java)
        }

    fun keybind(name: String, default: Int = 0) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.Keybind(default)))
            Handler(key, Int::class.java)
        }

    fun multiCheckbox(name: String, options: List<String>, default: Set<Int> = emptySet()) =
        OptionDelegate { key ->
            feature().addOption(name, ConfigElement(key, ElementType.MultiCheckbox(options, default)))
            @Suppress("UNCHECKED_CAST")
            Handler(key, Set::class.java) as Handler<Set<Int>>
        }

    fun button(text: String, onClick: () -> Unit) {
        feature().addOption(text, ConfigElement("$configKey.button_${text.hashCode()}", ElementType.Button(text, onClick)))
    }

    fun textParagraph(text: String) {
        feature().addOption(text, ConfigElement("$configKey.paragraph_${text.hashCode()}", ElementType.TextParagraph(text)))
    }

    inner class OptionDelegate<T>(private val init: (String) -> Handler<T>?) {
        private var handler: Handler<T>? = null

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T> {
            val fullKey = "$configKey.${property.name}"
            handler = init(fullKey)
            return ReadOnlyProperty { thisRef, property -> handler!!.getValue(thisRef, property) }
        }
    }
}