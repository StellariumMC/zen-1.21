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
import java.util.function.Consumer
import java.util.function.Supplier

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
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Meowing"))
                                    .option(createBoolOption("Auto Meow", "Automatically responds with a meow message whenever someone sends meow in chat.", "automeow", defaults.automeow, { config.automeow }, { v -> config.automeow = v }))
                                    .option(createBoolOption("Meow Sounds", "Plays a cat sound whenever someone sends \"meow\" in chat", "meowsounds", defaults.meowsounds, { config.meowsounds }, { v -> config.meowsounds = v }))
                                    .option(createBoolOption("Meow Death Sounds", "Plays a cat sound whenever an entity dies", "meowdeathsounds", defaults.meowdeathsounds, { config.meowdeathsounds }, { v -> config.meowdeathsounds = v }))
                                    .build()
                            )
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Clean Chat"))
                                    .option(createBoolOption("Clean guild join/leave", "Replaces the guild and friend join messages with a cleaner version of them.", "guildjoinleave", defaults.guildjoinleave, { config.guildjoinleave }, { v -> config.guildjoinleave = v }))
                                    .option(createBoolOption("Clean friend join/leave", "Replaces the guild and friend join messages with a cleaner version of them.", "friendjoinleave", defaults.friendjoinleave, { config.friendjoinleave }, { v -> config.friendjoinleave = v }))
                                    .option(createBoolOption("Clean guild messages", "Replaces the guild chat messages with a cleaner version of them.", "cleanmsg", defaults.guildmessage, { config.guildmessage }, { v -> config.guildmessage = v }))
                                    .option(createBoolOption("Clean party messages", "Replaces the party chat messages with a cleaner version of them.", "cleanmsg", defaults.partymessage, { config.partymessage }, { v -> config.partymessage = v }))
                                    .option(createBoolOption("Better Auction house", "Better auction house messages.", "betterah", defaults.betterah, { config.betterah }, { v -> config.betterah = v }))
                                    .option(createBoolOption("Better Bazaar", "Better bazaar messages.", "betterbz", defaults.betterbz, { config.betterbz }, { v -> config.betterbz = v }))
                                    .build()
                            )
                            .group(
                                OptionGroup.createBuilder()
                                    .name(Text.literal("Misc"))
                                    .option(createBoolOption("Send world age", "Sends world age to your chat.", "worldage", defaults.worldage, { config.worldage }, { v -> config.worldage = v }))
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
                                    .option(createBoolOption("Slayer timer", "Sends a message in your chat telling you how long it took to kill your boss.", "slayertimer", defaults.slayertimer, { config.slayertimer }, { v -> config.slayertimer = v }))
                                    .option(createBoolOption("Slayer highlight", "Highlights your slayer boss.", "slayerhighlight", defaults.slayerhighlight, { config.slayerhighlight }, { v -> config.slayerhighlight = v }))
                                    .option(createBoolOption("Vengeance damager tracker", "Tracks and sends your vegeance damage in the chat.", "vengdmg", defaults.vengdmg, { config.vengdmg }, { v -> config.vengdmg = v }))
                                    .build()
                            )
                            .build()
                    )
            }.generateScreen(parent)
        }

        private fun createBoolOption(name: String, desc: String, configKey: String, defaultVal: Boolean, getter: Supplier<Boolean>, setter: Consumer<Boolean>): Option<Boolean> {
            return Option.createBuilder<Boolean>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    if (getter.get() != v) {
                        setter.accept(v)
                        Handler.save()
                        Zen.onConfigChange(configKey)
                    }
                }
                .controller { opt ->
                    BooleanControllerBuilder.create(opt)
                        .formatValue { value -> Text.literal(if (value) "On" else "Off") }
                        .coloured(true)
                }
                .build()
        }

        private fun createSliderOption(name: String, desc: String, configKey: String, defaultVal: Int, getter: Supplier<Int>, setter: Consumer<Int>): Option<Int> {
            return Option.createBuilder<Int>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    if (getter.get() != v) {
                        setter.accept(v)
                        Handler.save()
                        Zen.onConfigChange(configKey)
                    }
                }
                .controller { opt ->
                    IntegerSliderControllerBuilder.create(opt)
                        .range(0, 100)
                        .step(1)
                        .formatValue { value -> Text.literal("$value%") }
                }
                .build()
        }

        private fun createTextOption(name: String, desc: String, configKey: String, defaultVal: String, getter: Supplier<String>, setter: Consumer<String>): Option<String> {
            return Option.createBuilder<String>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    if (getter.get() != v) {
                        setter.accept(v)
                        Handler.save()
                        Zen.onConfigChange(configKey)
                    }
                }
                .controller { opt -> StringControllerBuilder.create(opt) }
                .build()
        }

        private fun createColorOption(name: String, desc: String, configKey: String, defaultVal: Color, getter: Supplier<Color>, setter: Consumer<Color>): Option<Color> {
            return Option.createBuilder<Color>()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter) { v ->
                    if (getter.get() != v) {
                        setter.accept(v)
                        Handler.save()
                        Zen.onConfigChange(configKey)
                    }
                }
                .controller { opt -> ColorControllerBuilder.create(opt).allowAlpha(true) }
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
    @SerialEntry var vengdmg: Boolean = false
}