package xyz.meowing.zen.managers.feature

import io.github.classgraph.ClassGraph
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigManager.registerListener

@Module
object FeatureManager {
    var moduleCount = 0
        private set
    var commandCount = 0
        private set
    var loadTime: Long = 0
        private set

    private val pendingFeatures = mutableListOf<Feature>()
    private val islandFeatures = mutableListOf<Feature>()
    private val areaFeatures = mutableListOf<Feature>()
    private val skyblockFeatures = mutableListOf<Feature>()
    private val dungeonFloorFeatures = mutableListOf<Feature>()

    val features = mutableListOf<Feature>()

    init {
        EventBus.register<LocationEvent.SkyblockJoin> {
            skyblockFeatures.forEach { it.update() }
        }

        EventBus.register<LocationEvent.SkyblockLeave> {
            skyblockFeatures.forEach { it.update() }
        }

        EventBus.register<LocationEvent.IslandChange> {
            islandFeatures.forEach { it.update() }
        }

        EventBus.register<LocationEvent.AreaChange> {
            areaFeatures.forEach { it.update() }
        }

        EventBus.register<LocationEvent.DungeonFloorChange> {
            dungeonFloorFeatures.forEach { it.update() }
        }
    }

    fun addFeature(feature: Feature) = pendingFeatures.add(feature)

    fun loadFeatures() {
        val startTime = System.currentTimeMillis()

        ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .acceptPackages("xyz.meowing.zen")
            .scan()
            .use { result ->
                val features = result.getClassesWithAnnotation(Module::class.java.name).loadClasses()
                val commands = result.getClassesWithAnnotation(Command::class.java.name).loadClasses()

                features.forEach {
                    try {
                        Class.forName(it.name)
                        moduleCount++

                        Zen.LOGGER.debug("Loaded module: ${it.name}")
                    } catch (e: Exception) {
                        Zen.LOGGER.error("Error initializing module ${it.name}: $e")
                    }
                }

                commands.forEach {
                    try {
                        val instanceField = it.getDeclaredField("INSTANCE")
                        val command = instanceField.get(null) as Commodore

                        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                            command.register(dispatcher)
                        }
                        commandCount++

                        Zen.LOGGER.debug("Loaded command: ${it.name}")
                    } catch (e: Exception) {
                        Zen.LOGGER.error("Error initializing command ${it.name}: $e")
                    }
                }
            }

        loadTime = System.currentTimeMillis() - startTime
    }

    fun initializeFeatures() {
        pendingFeatures.forEach { feature ->
            features.add(feature)
            if (feature.hasIslands()) islandFeatures.add(feature)
            if (feature.hasAreas()) areaFeatures.add(feature)
            if (feature.hasDungeonFloors()) dungeonFloorFeatures.add(feature)
            if (feature.skyblockOnly) skyblockFeatures.add(feature)

            feature.initialize()
            feature.configKey?.let { registerListener(it, feature) }
            feature.update()
        }

        pendingFeatures.clear()
    }
}