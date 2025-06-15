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
import meowing.zen.featManager
import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier

class zencfg {
    companion object {
        val Handler: ConfigClassHandler<zencfg> = ConfigClassHandler.createBuilder(zencfg::class.java)
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
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Meowing"))
                                    .option(
                                        createBoolOption(
                                            "Auto Meow",
                                            "Automatically responds with a meow message whenever someone sends meow in chat.",
                                            defaults.automeow,
                                            { config.automeow },
                                            { v -> config.automeow = v }
                                        )
                                    )
                                    .option(
                                        createBoolOption(
                                            "Meow Sounds",
                                            "Plays a cat sound whenever someone sends \"meow\" in chat",
                                            defaults.meowsounds,
                                            { config.meowsounds },
                                            { v -> config.meowsounds = v }
                                        )
                                    )
                                    .option(
                                        createBoolOption(
                                            "Meow Death Sounds",
                                            "Plays a cat sound whenever an entity dies",
                                            defaults.meowdeathsounds,
                                            { config.meowdeathsounds },
                                            { v -> config.meowdeathsounds = v }
                                        )
                                    )
                                    .build()
                            )
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Clean Chat"))
                                    .option(
                                        createBoolOption(
                                            "Clean join",
                                            "Replaces the guild and friend join messages with a cleaner version of them.",
                                            defaults.cleanjoin,
                                            { config.cleanjoin },
                                            { v -> config.cleanjoin = v }
                                        )
                                    )
                                    .option(
                                        createBoolOption(
                                            "Clean messages",
                                            "Replaces the guild and friend chat messages with a cleaner version of them.",
                                            defaults.cleanmsg,
                                            { config.cleanmsg },
                                            { v -> config.cleanmsg = v }
                                        )
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .category(
                        ConfigCategory.createBuilder()
                            .name(Text.literal("Slayers"))
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Slayers"))
                                    .option(
                                        createBoolOption(
                                            "Slayer timer",
                                            "Sends a message in your chat telling you how long it took to kill your boss.",
                                            defaults.slayertimer,
                                            { config.slayertimer },
                                            { v -> config.slayertimer = v }
                                        )
                                    )
                                    .option(
                                        createBoolOption(
                                            "Slayer highlight",
                                            "Highlights your slayer boss.",
                                            defaults.slayerhighlight,
                                            { config.slayerhighlight },
                                            { v -> config.slayerhighlight = v }
                                        )
                                    )
                                    .build()
                            )
                            .build()
                    )
            }.generateScreen(parent)
        }

        private fun createBoolOption(
            name: String,
            desc: String,
            defaultVal: Boolean,
            getter: Supplier<Boolean>,
            setter: Consumer<Boolean>
        ): Option<Boolean> {
            return Option.createBuilder<Boolean>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    setter.accept(v)
                    featManager.onConfigChange()
                }
                .controller { opt ->
                    BooleanControllerBuilder.create(opt)
                        .formatValue { value -> Text.literal(if (value) "On" else "Off") }
                        .coloured(true)
                }
                .build()
        }

        private fun createSliderOption(
            name: String,
            desc: String,
            defaultVal: Int,
            getter: Supplier<Int>,
            setter: Consumer<Int>
        ): Option<Int> {
            return Option.createBuilder<Int>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    setter.accept(v)
                    featManager.onConfigChange()
                }
                .controller { opt ->
                    IntegerSliderControllerBuilder.create(opt)
                        .range(0, 100)
                        .step(1)
                        .formatValue { value -> Text.literal("$value%") }
                }
                .build()
        }

        private fun createTextOption(
            name: String,
            desc: String,
            defaultVal: String,
            getter: Supplier<String>,
            setter: Consumer<String>
        ): Option<String> {
            return Option.createBuilder<String>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    setter.accept(v)
                    featManager.onConfigChange()
                }
                .controller { opt -> StringControllerBuilder.create(opt) }
                .build()
        }

        private fun createColorOption(
            name: String,
            desc: String,
            defaultVal: Color,
            getter: Supplier<Color>,
            setter: Consumer<Color>
        ): Option<Color> {
            return Option.createBuilder<Color>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    setter.accept(v)
                    featManager.onConfigChange()
                }
                .controller { opt ->
                    ColorControllerBuilder.create(opt)
                        .allowAlpha(true)
                }
                .build()
        }
    }

    @SerialEntry
    var automeow: Boolean = false

    @SerialEntry
    var meowsounds: Boolean = false

    @SerialEntry
    var meowdeathsounds: Boolean = false

    @SerialEntry
    var cleanmsg: Boolean = false

    @SerialEntry
    var cleanjoin: Boolean = false

    @SerialEntry
    var vipcolor: String = "a"

    @SerialEntry
    var vippluscolor: String = "a"

    @SerialEntry
    var mvpcolor: String = "b"

    @SerialEntry
    var mvppluscolor: String = "b"

    @SerialEntry
    var mvppluspluscolor: String = "6"

    @SerialEntry
    var slayertimer: Boolean = false

    @SerialEntry
    var slayerhighlight: Boolean = false
}