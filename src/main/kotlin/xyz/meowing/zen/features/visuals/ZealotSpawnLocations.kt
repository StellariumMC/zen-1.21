package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.ClientTick
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.SimpleTimeMark
import xyz.meowing.zen.utils.TimeUtils.fromNow
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.Utils.toFormattedDuration
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockAreas
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@Module
object ZealotSpawnLocations : Feature(
    "zealotspawnvisual",
    true,
    SkyBlockIsland.THE_END,
    listOf(SkyBlockAreas.ZEALOT_BRUISER_HIDEOUT, SkyBlockAreas.DRAGONS_NEST)
) {
    private val zealotSpawns: List<BlockPos> = listOf(
        BlockPos(-646, 5, -274),
        BlockPos(-633, 5, -277),
        BlockPos(-639, 7, -305),
        BlockPos(-631, 5, -327),
        BlockPos(-619, 6, -313),
        BlockPos(-665, 10, -313),
        BlockPos(-632, 5, -260),
        BlockPos(-630, 7, -229),
        BlockPos(-647, 5, -221),
        BlockPos(-684, 5, -261),
        BlockPos(-699, 5, -263),
        BlockPos(-683, 5, -292),
        BlockPos(-698, 5, -319),
        BlockPos(-714, 5, -289),
        BlockPos(-732, 5, -295),
        BlockPos(-731, 5, -275)
    )

    private val bruiserSpawns: List<BlockPos> = listOf(
        BlockPos(-595, 80, -190),
        BlockPos(-575, 72, -201),
        BlockPos(-560, 64, -220),
        BlockPos(-554, 56, -237),
        BlockPos(-571, 51, -240),
        BlockPos(-585, 52, -232),
        BlockPos(-96, 55, -216),
        BlockPos(-578, 53, -214),
        BlockPos(-598, 55, -201),
        BlockPos(-532, 38, -223),
        BlockPos(-520, 38, -235),
        BlockPos(-530, 38, -246),
        BlockPos(-515, 39, -250),
        BlockPos(-516, 39, -264),
        BlockPos(-513, 38, -279),
        BlockPos(-524, 44, -268),
        BlockPos(-536, 48, -252),
        BlockPos(-526, 38, -294),
        BlockPos(-514, 39, -304),
        BlockPos(-526, 39, -317)
    )

    private var spawnTime = SimpleTimeMark(0)
    private var displayText = "§dZealot Spawn: §510s"

    private val drawZealotSpawnBox by ConfigDelegate<Boolean>("drawzealotspawnbox")
    private val zealotSpawnColor by ConfigDelegate<Color>("drawzealotspawncolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Zealot Spawn Locations", "", "Visuals", ConfigElement(
                "zealotspawnvisual",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Zealot Spawn Location Boxes", "", "Options", ConfigElement(
                "drawzealotspawnbox",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Box color", "", "Options", ConfigElement(
                "drawzealotspawncolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["drawzealotspawnbox"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        setupLoops {
            loop<ClientTick>(10) {
                val timeUntilSpawn = spawnTime.until
                val remaining = if (timeUntilSpawn.isPositive() && timeUntilSpawn.millis > 1000) timeUntilSpawn.millis.toFormattedDuration() else "§aReady"
                val mobType = if (SkyBlockAreas.DRAGONS_NEST.inArea()) "Zealot" else "Bruiser"
                displayText = "§d$mobType Spawn: §5$remaining"
            }
        }


        register<SkyblockEvent.EntitySpawn> { event ->
            val mobId = event.skyblockMob.id

            if (SkyBlockAreas.ZEALOT_BRUISER_HIDEOUT.inArea() && mobId == "Zealot Bruiser" ||
                (SkyBlockAreas.DRAGONS_NEST.inArea() && mobId == "Zealot")) {
                spawnTime = 8.seconds.fromNow
            }
        }

        register<RenderEvent.World.Last> { event ->
            val positions = if (SkyBlockAreas.DRAGONS_NEST.inArea()) zealotSpawns else bruiserSpawns
            positions.forEach { pos ->
                val aabb = Box(pos.x - 5.0, pos.y + 0.1, pos.z - 5.0, pos.x + 5.0, pos.y - 3.0, pos.z + 5.0)
                if (drawZealotSpawnBox) Render3D.drawSpecialBB(aabb, zealotSpawnColor, event.context.consumers(), event.context.matrixStack())
                Render3D.drawString(
                    displayText,
                    Vec3d(pos).add(0.0, 1.5, 0.0),
                    -1
                )
            }
        }
    }
}