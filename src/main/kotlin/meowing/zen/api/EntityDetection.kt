package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.ChatEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.EventBus.post
import meowing.zen.events.SkyblockEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.projectile.ArrowEntity

@Zen.Module
object EntityDetection {
    private val hashMap = HashMap<Entity, SkyblockMob>()
    private val normalMobRegex = "\\[Lv\\d+k?] (?:[༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙\uFE0E♃⛨✈⸙]+ )?(.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤".toRegex()
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val dungeonMobRegex = "(?:[༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙︎♃⛨✈⸙]+ )?✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤".toRegex()
    private val patterns = listOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)
    private var inSlayerFight = false
    private var SlayerEntity: Entity? = null
    var bossID: Int? = null
        private set

    class SkyblockMob(val nameEntity: Entity, val skyblockMob: Entity) {
        var id: String? = null
    }

    init {
        TickUtils.loop(5) {
            val world = mc.world ?: return@loop
            val player = mc.player ?: return@loop

            world.entities.forEach { entity ->
                if (player.distanceTo(entity) > 30 || entity !is ArmorStandEntity || !entity.hasCustomName() || hashMap.containsKey(entity)) return@forEach
                val nameTag = entity.name.string
                val mobId = if (nameTag.contains("Withermancer")) entity.id - 3 else entity.id - 1
                val mob = world.getEntityById(mobId) ?: return@forEach

                if (!mob.isAlive || mob is ArrowEntity) return@forEach

                val skyblockMob = SkyblockMob(entity, mob)
                hashMap[entity] = skyblockMob
                updateMobData(skyblockMob)

                if (skyblockMob.id != null) {
                    post(SkyblockEvent.EntitySpawn(skyblockMob))
                }
            }
        }

        TickUtils.loop(100) {
            bossID?.let { id ->
                val world = mc.world ?: return@loop
                val boss = world.getEntityById(id)
                if (boss == null || !boss.isAlive) {
                    post(SkyblockEvent.Slayer.Cleanup())
                    bossID = null
                }
            }
        }

        EventBus.register<EntityEvent.Metadata> { event ->
            if (inSlayerFight) return@register
            val world = mc.world ?: return@register
            val player = mc.player ?: return@register

            val name = event.name
            if (name.contains("Spawned by") && name.endsWith("by: ${player.name.string}")) {
                val hasBlackhole = world.entities.any {
                    event.entity.distanceTo(it) <= 3f && it.name?.string?.removeFormatting()?.contains("black hole", true) == true
                }

                if (!hasBlackhole) {
                    bossID = event.packet.id - 3
                    SlayerEntity = world.getEntityById(event.packet.id - 3)
                    inSlayerFight = true
                    post(SkyblockEvent.Slayer.Spawn(event.entity, event.entity.id, event.packet))
                }
            }
        }

        EventBus.register<EntityEvent.Death> { event ->
            if (event.entity.id == bossID && inSlayerFight) {
                bossID = null
                SlayerEntity = null
                inSlayerFight = false
                post(SkyblockEvent.Slayer.Death(event.entity, event.entity.id))
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            when (event.message.string.removeFormatting()) {
                "  SLAYER QUEST FAILED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    post(SkyblockEvent.Slayer.Fail())
                }
                "  SLAYER QUEST STARTED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    post(SkyblockEvent.Slayer.QuestStart())
                }
            }
        }

        EventBus.register<WorldEvent.Change> {
            hashMap.clear()
        }

        EventBus.register<EntityEvent.Death> { event ->
            hashMap.remove(event.entity)
            hashMap.entries.removeAll { it.value.skyblockMob == event.entity }
        }
    }

    private fun updateMobData(sbMob: SkyblockMob) {
        val rawMobName = sbMob.nameEntity.displayName?.string.removeFormatting().replace(",", "")

        patterns.forEachIndexed { index, pattern ->
            pattern.find(rawMobName)?.let { match ->
                sbMob.id = when (index) {
                    0 -> match.groupValues[1]
                    1 -> match.value
                    2 -> {
                        val mobName = match.groupValues[1]
                        if (rawMobName.startsWith("ൠ")) "$mobName Pest" else mobName
                    }
                    else -> return
                }

                sbMob.id?.let { id ->
                    if (id.startsWith("a") && id.length > 2 && Character.isUpperCase(id[1])) {
                        sbMob.id = id.substring(1, id.length - 2)
                    }
                }
                return
            }
        }
    }

    inline val Entity.sbMobID: String? get() = getSkyblockMob(this)?.id

    fun getSkyblockMob(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.skyblockMob == entity }
    fun getNameTag(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.nameEntity == entity }
    fun getSlayerEntity(): Entity? = SlayerEntity
}