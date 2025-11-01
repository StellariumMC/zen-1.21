package xyz.meowing.zen.features.slayers.slayerdisplay

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.baseMaxHealth
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.util.math.Vec3d
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.configRegister
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object SlayerDisplay : Feature("slayerdisplay", true) {
    private val slayerEntities = mutableMapOf<Int, SlayerData>()
    private val nametagData = mutableMapOf<Int, String>()
    private val killTimers = mutableMapOf<Int, Triple<Long, String, Vec3d>>()
    private val hiddenArmorStands = mutableSetOf<Int>()

    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val hpRegex = "(\\d+(?:,\\d{3})*(?:\\.\\d+)?[kmb]?)❤".toRegex(RegexOption.IGNORE_CASE)
    private val hitsRegex = "(\\d+)\\s+Hits".toRegex()
    private val timeRegex = "(\\d+):(\\d+)".toRegex()

    private val useFullName by ConfigDelegate<Boolean>("slayerdisplayusefullname")
    private val displayOptions by ConfigDelegate<Set<Int>>("slayerdisplayoptions")
    private val shownBossesOption by ConfigDelegate<Int>("slayerdisplaybossshown")
    private val showKillTimer by ConfigDelegate<Boolean>("slayerdisplayshowkilltimer")
    private val hideOriginalNametags by ConfigDelegate<Boolean>("slayerdisplayhideoriginalnametags")

    private data class SlayerData(
        val spawnTime: Long,
        var displayText: String = "",
        var bossType: BossTypes? = null
    )

    override fun addConfig() {
        ConfigManager
            .addFeature("Slayer Display", "Shows slayer boss information", "Slayers", ConfigElement(
                    "slayerdisplay",
                    ElementType.Switch(false)
            ))
            .addFeatureOption("Shown Bosses", "", "Options", ConfigElement(
                "slayerdisplaybossshown",
                ElementType.Dropdown(
                    listOf(
                        "Show All",
                        "Show Carries Only",
                        "Show Own Only",
                        "Show Own and Carries"
                    ),
                    0
                )
            ))
            .addFeatureOption("Display Options", "", "Options", ConfigElement(
                "slayerdisplayoptions",
                ElementType.MultiCheckbox(
                    listOf(
                        "Show Mob Name",
                        "Show Health",
                        "Show Hits",
                        "Show Laser Timer",
                        "Show Timer Nametag",
                        "Compact Display"
                    ),
                    setOf(0, 1, 2, 3, 4)
                )
            ))
            .addFeatureOption("Use Full Mob Name", "", "Display", ConfigElement(
                "slayerdisplayusefullname",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Show Kill Timer", "", "Display", ConfigElement(
                "slayerdisplayshowkilltimer",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Hide Original Nametags", "", "Display", ConfigElement(
                "slayerdisplayhideoriginalnametags",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<LocationEvent.WorldChange> {
            slayerEntities.clear()
            nametagData.clear()
            killTimers.clear()
            hiddenArmorStands.clear()
        }

        register<EntityEvent.Packet.Metadata> { event ->
            val cleanName = event.name.removeFormatting()
            val entityId = event.entity.id
            nametagData[entityId] = cleanName

            if (slayerMobRegex.find(cleanName) != null) {
                val slayerEntityId = event.packet.id - 1

                if (!slayerEntities.containsKey(slayerEntityId)) {
                    val existingEntry = findBossTypeEntry(cleanName)
                    if (existingEntry != null && cleanName.contains("Inferno", true)) {
                        slayerEntities[slayerEntityId] = slayerEntities.remove(existingEntry.key)!!
                    } else {
                        slayerEntities[slayerEntityId] = SlayerData(System.currentTimeMillis())
                    }
                }

                if (hideOriginalNametags && shouldShowSlayer(slayerEntityId)) {
                    hiddenArmorStands.addAll(listOf(entityId, entityId + 1, entityId + 2))
                }
            }

            if (hideOriginalNametags && isSlayerRelatedNametag(entityId)) {
                val slayerEntityId = entityId - 1
                if (slayerEntities.containsKey(slayerEntityId) && shouldShowSlayer(slayerEntityId)) {
                    hiddenArmorStands.add(entityId)
                }
            }

            listOf(entityId - 1, entityId - 2).forEach { slayerId ->
                if (slayerEntities.containsKey(slayerId)) {
                    val nametagId = if (slayerId == entityId - 1) entityId else entityId - 1
                    updateSlayerDisplay(nametagId, nametagData[nametagId] ?: "")
                }
            }
        }

        configRegister<RenderEvent.Entity.Pre>(listOf("slayerdisplay", "slayerdisplayhideoriginalnametags"), priority = 1000, skyblockOnly = true) { event ->
            if (event.entity is ArmorStandEntity && hiddenArmorStands.contains(event.entity.id)) {
                event.cancel()
            }
        }

        configRegister<TickEvent.Client>(listOf("slayerdisplay", "slayerdisplayoptions"), requiredIndex = 3, skyblockOnly = true) {
            slayerEntities.forEach { (slayerEntityId, _) ->
                val nametagEntityId = slayerEntityId + 1
                nametagData[nametagEntityId]?.let {
                    updateSlayerDisplay(nametagEntityId, it)
                }
            }
        }

        register<EntityEvent.Leave> { event ->
            val entityId = event.entity.id

            slayerEntities[entityId]?.let { slayerData ->
                if (showKillTimer) {
                    val killTime = System.currentTimeMillis() - slayerData.spawnTime
                    val killTimeText = "§a${"%.1f".format(killTime / 1000.0)}s"
                    killTimers[entityId] = Triple(
                        System.currentTimeMillis(),
                        killTimeText,
                        //#if MC >= 1.21.9
                        //$$ event.entity.entityPos
                        //#else
                        event.entity.pos
                        //#endif
                    )
                }
            }

            slayerEntities.remove(entityId)
            nametagData.remove(entityId)
            (0..3).forEach {
                hiddenArmorStands.remove(entityId + it)
            }
        }

        register<RenderEvent.Entity.Post> { event ->
            if (event.entity is BlazeEntity || event.entity is ArmorStandEntity) return@register
            val entityId = event.entity.id
            val slayerEntityId = entityId - 1

            if (!shouldShowSlayer(slayerEntityId) && !shouldShowSlayer(entityId)) return@register

            val (displayText, yOffset) = if (slayerEntities[slayerEntityId]?.bossType?.fullName?.contains("inferno", true) == true) {
                slayerEntities[slayerEntityId]?.displayText to -1.75
            } else {
                slayerEntities[entityId]?.displayText to 0.25
            }

            displayText?.takeIf { it.isNotEmpty() }?.let {
                val depth = player?.canSee(event.entity) != true
                Render3D.drawString(
                    it,
                    //#if MC >= 1.21.9
                    //$$ event.entity.entityPos.add(0.0, yOffset, 0.0),
                    //#else
                    event.entity.pos.add(0.0, yOffset, 0.0),
                    //#endif
                    depth = depth,
                    scaleMultiplier = 1.5,
                    smallestDistanceView = 8.0
                )
            }
        }

        configRegister<RenderEvent.World.Last>(listOf("slayerdisplay", "slayerdisplayshowkilltimer"), skyblockOnly = true) {
            killTimers.entries.removeAll { (_, timerData) ->
                val expired = System.currentTimeMillis() - timerData.first > 3000
                if (!expired) {
                    Render3D.drawString(
                        timerData.second,
                        timerData.third.add(0.0, 0.25, 0.0),
                        scaleMultiplier = 1.5
                    )
                }
                expired
            }
        }
    }

    private fun isSlayerRelatedNametag(entityId: Int): Boolean {
        val name = nametagData[entityId] ?: return false
        return name.contains("Hits") || timeRegex.containsMatchIn(name) || name.contains("Spawned by")
    }

    private fun updateSlayerDisplay(nametagEntityId: Int, cleanName: String) {
        val slayerEntityId = nametagEntityId - 1
        val slayerData = slayerEntities[slayerEntityId] ?: return

        getBossType(cleanName)?.let { slayerData.bossType = it }
        val bossType = slayerData.bossType ?: return
        val entity = world?.getEntityById(slayerEntityId)

        val hpMatch = hpRegex.find(cleanName)
        val hitsMatch = hitsRegex.find(cleanName)
        val timerNametag = nametagData[nametagEntityId + 1] ?: ""

        slayerData.displayText = if ((hpMatch != null && 1 in displayOptions) || (hitsMatch != null && 2 in displayOptions)) {
            val prefix = when {
                cleanName.contains("✯") && cleanName.contains("ᛤ") -> "§b✯ §5ᛤ§r "
                cleanName.contains("✯") -> "§b✯§r "
                cleanName.contains("ᛤ") -> "§5ᛤ§r "
                else -> ""
            }

            buildDisplayText(entity, bossType, hpMatch, hitsMatch, timerNametag, prefix)
        } else ""
    }

    private fun shouldShowSlayer(slayerEntityId: Int): Boolean {
        val spawnerNametag = nametagData[slayerEntityId + 3] ?: ""
        val playerName = player?.name?.string ?: ""
        val cleanSpawnerNametag = spawnerNametag.removeFormatting()
        val cleanPlayerName = if(!Utils.nicked) playerName.removeFormatting() else Utils.nickedName

        return when (shownBossesOption) {
            0 -> true
            1 -> {
                CarryCounter.carryees.any {
                    cleanSpawnerNametag.endsWith("by: ${it.name.removeFormatting()}")
                }
            }
            2 -> {
                cleanSpawnerNametag.endsWith("by: $cleanPlayerName")
            }
            3 -> {
                cleanSpawnerNametag.endsWith("by: $cleanPlayerName") ||
                    CarryCounter.carryees.any {
                        cleanSpawnerNametag.endsWith("by: ${it.name.removeFormatting()}")
                    }
            }
            else -> false
        }
    }

    private fun buildDisplayText(entity: Entity?, bossType: BossTypes, hpMatch: MatchResult?, hitsMatch: MatchResult?, timerNametag: String, prefix: String = ""): String {
        val baseName = if (useFullName) bossType.fullName else bossType.shortName
        val mobName = if (prefix.isNotEmpty()) "$prefix$baseName" else baseName
        val laserTimer = entity?.let { getLaserTimer(it) } ?: ""
        val useCompactDisplay = 5 in displayOptions
        val showName = 0 in displayOptions
        val displayLines = mutableListOf<String>()

        val nameWithTimer = if (laserTimer.isNotEmpty()) "$mobName $laserTimer" else mobName

        if (4 in displayOptions && timerNametag.isNotEmpty()) {
            formatTimerNametag(timerNametag).takeIf { it.isNotEmpty() }?.let { displayLines.add(it) }
        }

        when {
            hpMatch != null && 1 in displayOptions -> {
                val currentHp = parseHp(hpMatch.groupValues[1])
                val maxHp = (entity as? LivingEntity)?.baseMaxHealth ?: 150_000_000
                val healthText = "${getHpColor(currentHp, maxHp)}${hpMatch.groupValues[1]}"
                addDisplayLine(displayLines, nameWithTimer, laserTimer, healthText, showName, useCompactDisplay)
            }
            hitsMatch != null && 2 in displayOptions -> {
                val hits = hitsMatch.groupValues[1].toInt()
                val tier = getTier(bossType)
                val hitsText = "${getHitsColor(hits, tier)}$hits Hits"
                addDisplayLine(displayLines, nameWithTimer, laserTimer, hitsText, showName, useCompactDisplay)
            }
        }

        return displayLines.joinToString("\n")
    }

    private fun addDisplayLine(displayLines: MutableList<String>, nameWithTimer: String, laserTimer: String, dataText: String, showName: Boolean, useCompactDisplay: Boolean) {
        when {
            showName && useCompactDisplay -> displayLines.add("$nameWithTimer $dataText")
            showName -> {
                displayLines.add(nameWithTimer)
                displayLines.add(dataText)
            }
            laserTimer.isNotEmpty() && useCompactDisplay -> displayLines.add("$laserTimer $dataText")
            laserTimer.isNotEmpty() -> {
                displayLines.add(laserTimer)
                displayLines.add(dataText)
            }
            else -> displayLines.add(dataText)
        }
    }

    private fun formatTimerNametag(timerNametag: String): String {
        val match = timeRegex.find(timerNametag) ?: return ""
        val prefix = when {
            timerNametag.contains("ASHEN", ignoreCase = true) -> "§8ASHEN "
            timerNametag.contains("SPIRIT", ignoreCase = true) -> "§fSPIRIT "
            timerNametag.contains("AURIC", ignoreCase = true) -> "§eAURIC "
            timerNametag.contains("CRYSTAL", ignoreCase = true) -> "§bCRYSTAL "
            else -> ""
        }
        val minutes = match.groupValues[1].toInt()
        val seconds = match.groupValues[2].toInt()
        val totalSeconds = minutes * 60 + seconds
        val color = getTimerColor(totalSeconds)
        return "$prefix$color${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    private fun findBossTypeEntry(cleanName: String): Map.Entry<Int, SlayerData>? {
        val bossType = getBossType(cleanName) ?: return null
        return slayerEntities.entries.find { it.value.bossType == bossType }
    }

    private fun getTimerColor(totalSeconds: Int): String = when {
        totalSeconds > 180 -> "§a"
        totalSeconds > 120 -> "§e"
        totalSeconds > 60 -> "§6"
        else -> "§c"
    }

    private fun parseHp(hp: String): Long {
        return when {
            hp.endsWith("k", true) -> hp.dropLast(1).toDouble() * 1_000
            hp.endsWith("m", true) -> hp.dropLast(1).toDouble() * 1_000_000
            hp.endsWith("b", true) -> hp.dropLast(1).toDouble() * 1_000_000_000
            else -> hp.replace(",", "").toDouble()
        }.toLong()
    }

    private fun getHpColor(currentHp: Long, maxHp: Int): String = when (currentHp.toFloat() / maxHp.toFloat()) {
        in 0.75f..1f -> "§a"
        in 0.5f..0.75f -> "§e"
        in 0.25f..0.5f -> "§6"
        else -> "§c"
    }

    private fun getBossType(cleanName: String): BossTypes? {
        val slayerMatch = slayerMobRegex.find(cleanName.removeFormatting().replace(",", "")) ?: return null
        val matchedName = slayerMatch.value
        val baseName = matchedName.substringBeforeLast(" ")
        val tier = Utils.decodeRoman(matchedName.substringAfterLast(" ")).toString()

        return BossTypes.entries.find { boss ->
            val bossClean = boss.fullName.removeFormatting()
            bossClean.contains(baseName, ignoreCase = true) && bossClean.contains(tier, ignoreCase = true)
        }
    }

    private fun getTier(bossType: BossTypes): Int = bossType.name.takeLast(1).toIntOrNull() ?: 1

    private fun getHitsColor(currentHits: Int, tier: Int): String {
        val maxHits = when (tier) {
            4 -> 100
            3 -> 60
            2 -> 30
            1 -> 15
            else -> 100
        }

        return when (currentHits.toFloat() / maxHits) {
            in 0.75f..1f -> "§a"
            in 0.5f..0.75f -> "§e"
            in 0.25f..0.5f -> "§6"
            else -> "§c"
        }
    }

    private fun getLaserTimer(entity: Entity): String {
        if (3 !in displayOptions) return ""

        val ridingEntity = entity.vehicle ?: return ""
        val time = maxOf(0.0, 8.2 - (ridingEntity.age / 20.0))
        val color = when {
            time > 6.0 -> "§a"
            time > 3.0 -> "§e"
            time > 1.0 -> "§6"
            else -> "§c"
        }
        return "$color${"%.1f".format(time)}s"
    }
}