package meowing.zen.config

import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.*
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import meowing.zen.Zen
import java.awt.Color
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class ZenConfig {
    companion object {
        val Handler: ConfigClassHandler<ZenConfig> = ConfigClassHandler.createBuilder(ZenConfig::class.java)
            .id(Identifier.of("meowing", "zen"))
            .serializer { config ->
                GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().configDir.resolve("zen.json5"))
                    .setJson5(true)
                    .build()
            }
            .build()

        fun createConfigScreen(parent: Screen?): Screen {
            return YetAnotherConfigLib.create(Handler) { defaults, config, builder ->
                builder
                    .title(Text.literal("Zen Configuration"))
                    .category(
                        ConfigCategory.createBuilder()
                            .name(Text.literal("General"))
                            .group(createGroup("Meowing", listOf(
                                ConfigOption("Auto Meow", "Automatically responds with a meow message whenever someone sends meow in chat.", "automeow"),
                                ConfigOption("Meow Sounds", "Plays a cat sound whenever someone sends \"meow\" in chat", "meowsounds"),
                                ConfigOption("Meow Death Sounds", "Plays a cat sound whenever an entity dies", "meowdeathsounds")
                            ), defaults, config))
                            .group(createGroup("Clean Chat", listOf(
                                ConfigOption("Clean guild join/leave", "Replaces the guild and friend join messages with a cleaner version of them.", "guildjoinleave"),
                                ConfigOption("Clean friend join/leave", "Replaces the guild and friend join messages with a cleaner version of them.", "friendjoinleave"),
                                ConfigOption("Clean guild messages", "Replaces the guild chat messages with a cleaner version of them.", "guildmessage"),
                                ConfigOption("Clean party messages", "Replaces the party chat messages with a cleaner version of them.", "partymessage"),
                                ConfigOption("Better Auction house", "Better auction house messages.", "betterah"),
                                ConfigOption("Better Bazaar", "Better bazaar messages.", "betterbz")
                            ), defaults, config))
                            .group(createGroup("Misc", listOf(
                                ConfigOption("Send world age", "Sends world age to your chat.", "worldage")
                            ), defaults, config))
                            .build()
                    )
                    .category(
                        ConfigCategory.createBuilder()
                            .name(Text.literal("Slayers"))
                            .group(createGroup("Slayers", listOf(
                                ConfigOption("Slayer timer", "Sends a message in your chat telling you how long it took to kill your boss.", "slayertimer"),
                                ConfigOption("Slayer highlight", "Highlights your slayer boss.", "slayerhighlight"),
                                ConfigOption("Vengeance damager tracker", "Tracks and sends your vegeance damage in the chat.", "vengdmg"),
                                ConfigOption("Vengeance proc timer", "Time until vengeance procs.", "vengtimer"),
                                ConfigOption("Slayer stats", "Shows stats about your kill times", "slayerstats")
                            ), defaults, config))
                            .group(createGroup("Carrying", listOf(
                                ConfigOption("Carry counter", "Counts and sends the carries that you do.", "carrycounter"),
                                ConfigOption("Carry boss highlight", "Highlights your client's boss.", "carrybosshighlight"),
                                ConfigOption("Carry boss highlight color", "The color for boss highlight", "carrybosshighlightcolor"),
                                ConfigOption("Carry client highlight", "Highlights your client's boss.", "carryclienthighlight"),
                                ConfigOption("Carry client highlight color", "The color for client highlight", "carryclienthighlightcolor"),
                                ConfigOption("Carry value", "Carry values for the mod to automatically detect in a trade", "carryvalue")
                            ), defaults, config))
                            .build()
                    )
            }.generateScreen(parent)
        }

        private data class ConfigOption(
            val name: String,
            val description: String,
            val key: String,
            val intRange: IntRange? = null,
            val intStep: Int? = null,
            val floatRange: ClosedFloatingPointRange<Float>? = null,
            val floatStep: Float? = null,
            val formatValue: ((Any) -> Text)? = null
        ) {
            constructor(name: String, description: String, key: String) :
                    this(name, description, key, null, null, null, null, null)
            constructor(name: String, description: String, key: String, range: IntRange, step: Int = 1, formatter: ((Int) -> Text)? = null) :
                    this(name, description, key, range, step, null, null, formatter?.let { f -> { v -> f(v as Int) } })
            constructor(name: String, description: String, key: String, range: ClosedFloatingPointRange<Float>, step: Float = 0.1f, formatter: ((Float) -> Text)? = null) :
                    this(name, description, key, null, null, range, step, formatter?.let { f -> { v -> f(v as Float) } })
        }

        private fun createGroup(name: String, options: List<ConfigOption>, defaults: ZenConfig, config: ZenConfig): OptionGroup {
            val groupBuilder = OptionGroup.createBuilder().name(Text.literal(name))
            options.forEach { opt ->
                val option = createOptionForProperty(opt.name, opt.description, opt.key, defaults, config, opt)
                option?.let { groupBuilder.option(it) }
            }
            return groupBuilder.build()
        }

        @Suppress("UNCHECKED_CAST")
        private fun createOptionForProperty(name: String, desc: String, key: String, defaults: ZenConfig, config: ZenConfig, configOption: ConfigOption): Option<*>? {
            val property = ZenConfig::class.memberProperties.find { it.name == key } as? KMutableProperty1<ZenConfig, *>
                ?: return null

            return when (val defaultValue = property.get(defaults)) {
                is Boolean -> {
                    val boolProperty = property as KMutableProperty1<ZenConfig, Boolean>
                    createOption(
                        name, desc, key, defaultValue,
                        { boolProperty.get(config) },
                        { v -> boolProperty.set(config, v) }
                    ) { opt ->
                        BooleanControllerBuilder.create(opt)
                            .formatValue { value -> Text.literal(if (value) "On" else "Off") }
                            .coloured(true)
                    }
                }

                is String -> {
                    val stringProperty = property as KMutableProperty1<ZenConfig, String>
                    createOption(
                        name, desc, key, defaultValue,
                        { stringProperty.get(config) },
                        { v -> stringProperty.set(config, v) }
                    ) { opt -> StringControllerBuilder.create(opt) }
                }

                is Float -> {
                    val floatProperty = property as KMutableProperty1<ZenConfig, Float>
                    createOption(
                        name, desc, key, defaultValue,
                        { floatProperty.get(config) },
                        { v -> floatProperty.set(config, v) }
                    ) { opt ->
                        val builder = FloatSliderControllerBuilder.create(opt)
                        val range = configOption.floatRange ?: (0.0f..1.0f)
                        builder.range(range.start, range.endInclusive)
                        val step = configOption.floatStep ?: 0.1f
                        builder.step(step)
                        if (configOption.formatValue != null) builder.formatValue { value -> configOption.formatValue.invoke(value) }
                        else builder.formatValue { value -> Text.literal("%.2f".format(value)) }
                        builder
                    }
                }

                is FloatArray -> {
                    val floatArrayProperty = property as KMutableProperty1<ZenConfig, FloatArray>
                    createOption(
                        name, desc, key,
                        Color(defaultValue[0], defaultValue[1], defaultValue[2], if (defaultValue.size > 3) defaultValue[3] else 1f),
                        {
                            val arr = floatArrayProperty.get(config)
                            Color(arr[0], arr[1], arr[2], if (arr.size > 3) arr[3] else 1f)
                        },
                        { v ->
                            floatArrayProperty.set(config, floatArrayOf(
                                v.red / 255f,
                                v.green / 255f,
                                v.blue / 255f,
                                v.alpha / 255f
                            ))
                        }
                    ) { opt -> ColorControllerBuilder.create(opt).allowAlpha(true) }
                }

                is Int -> {
                    val intProperty = property as KMutableProperty1<ZenConfig, Int>
                    createOption(
                        name, desc, key, defaultValue,
                        { intProperty.get(config) },
                        { v -> intProperty.set(config, v) }
                    ) { opt ->
                        val builder = IntegerSliderControllerBuilder.create(opt)
                        val range = configOption.intRange ?: (0..100)
                        builder.range(range.first, range.last)
                        val step = configOption.intStep ?: 1
                        builder.step(step)
                        if (configOption.formatValue != null) {
                            builder.formatValue { value -> configOption.formatValue.invoke(value) }
                        } else {
                            builder.formatValue { value -> Text.literal("$value%") }
                        }
                        builder
                    }
                }

                else -> null
            }
        }

        private inline fun <T : Any> createOption(
            name: String,
            desc: String,
            configKey: String,
            defaultVal: T,
            noinline getter: () -> T,
            crossinline setter: (T) -> Unit,
            noinline controllerBuilder: (Option<T>) -> ControllerBuilder<T>
        ): Option<T> {
            return Option.createBuilder<T>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    if (getter() != v) {
                        setter(v)
                        Handler.save()
                        Zen.onConfigChange(configKey)
                    }
                }
                .controller(controllerBuilder)
                .build()
        }
    }

    @SerialEntry var automeow: Boolean = false
    @SerialEntry var meowsounds: Boolean = false
    @SerialEntry var meowdeathsounds: Boolean = false
    @SerialEntry var guildmessage: Boolean = false
    @SerialEntry var partymessage: Boolean = false
    @SerialEntry var guildjoinleave: Boolean = false
    @SerialEntry var friendjoinleave: Boolean = false
    @SerialEntry var betterah: Boolean = false
    @SerialEntry var betterbz: Boolean = false
    @SerialEntry var worldage: Boolean = false
    @SerialEntry var slayertimer: Boolean = false
    @SerialEntry var slayerhighlight: Boolean = false
    @SerialEntry var slayerstats: Boolean = false
    @SerialEntry var carrycounter: Boolean = false
    @SerialEntry var carrybosshighlight: Boolean = false
    @SerialEntry var carrybosshighlightcolor: FloatArray = floatArrayOf(0f, 1f, 1f, 0.5f)
    @SerialEntry var carryclienthighlightcolor: FloatArray = floatArrayOf(0f, 1f, 1f, 0.5f)
    @SerialEntry var carryclienthighlight: Boolean = false
    @SerialEntry var carryvalue: String = ""
    @SerialEntry var vengdmg: Boolean = false
    @SerialEntry var vengtimer: Boolean = false
}