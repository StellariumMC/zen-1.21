package xyz.meowing.zen.api.skyblock

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.Arrow
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object EntityDetection {
    private val hashMap = HashMap<Entity, SkyblockMob>()
    private val slayerEntities = HashMap<Entity, SkyblockMob>()
    private val normalMobRegex = "\\[Lv\\d+k?] (?:[Ж༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙\uFE0E♃⛨✈⸙]+ )?(.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤".toRegex()
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val dungeonMobRegex = "(?:[Ж༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙︎♃⛨✈⸙]+ )?✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤".toRegex()
    private val patterns = listOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)
    private var inSlayerFight = false
    private var SlayerEntity: Entity? = null
    var bossID: Int? = null
        private set

    class SkyblockMob(val nameEntity: Entity, val skyblockMob: Entity) {
        var id: String? = null
    }

    init {
        TickScheduler.Client.repeat(5) {
            val world = KnitClient.world ?: return@repeat
            val player = KnitPlayer.player ?: return@repeat

            world.entitiesForRendering().forEach { entity ->
                if (player.distanceTo(entity) > 30 || entity !is ArmorStand || !entity.hasCustomName() || hashMap.containsKey(entity)) return@forEach
                val nameTag = entity.name.string
                val mobId = if (nameTag.contains("Withermancer")) entity.id - 3 else entity.id - 1
                val mob = world.getEntity(mobId) ?: return@forEach

                if (!mob.isAlive || mob is Arrow) return@forEach

                val skyblockMob = SkyblockMob(entity, mob)
                hashMap[entity] = skyblockMob
                updateMobData(skyblockMob)

                if (skyblockMob.id != null) {
                    EventBus.post(SkyblockEvent.EntitySpawn(skyblockMob))
                }
            }
        }

        TickScheduler.Client.repeat(100) {
            bossID?.let { id ->
                val world = KnitClient.world ?: return@repeat
                val boss = world.getEntity(id)
                if (boss == null || !boss.isAlive) {
                    EventBus.post(SkyblockEvent.Slayer.Cleanup())
                    bossID = null
                }
            }
        }

        EventBus.register<EntityEvent.Packet.Metadata> { event ->
            if (inSlayerFight) return@register
            val world = KnitClient.world ?: return@register
            val player = KnitPlayer.player ?: return@register

            val name = event.name
            if (name.contains("Spawned by") && name.endsWith("by: ${player.name.string}")) {
                val hasBlackhole = world.entitiesForRendering().any {
                    event.entity.distanceTo(it) <= 3f && it.name?.string?.removeFormatting()?.contains("black hole", true) == true
                }

                if (!hasBlackhole) {
                    bossID = event.packet.id - 3
                    SlayerEntity = world.getEntity(event.packet.id - 3)
                    inSlayerFight = true
                    EventBus.post(SkyblockEvent.Slayer.Spawn(event.entity, event.entity.id, event.packet))
                }
            }
        }

        EventBus.register<EntityEvent.Death> { event ->
            if (event.entity.id == bossID && inSlayerFight) {
                bossID = null
                SlayerEntity = null
                inSlayerFight = false
                EventBus.post(SkyblockEvent.Slayer.Death(event.entity, event.entity.id))
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            when (event.message.string.removeFormatting()) {
                "  SLAYER QUEST FAILED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    EventBus.post(SkyblockEvent.Slayer.Fail())
                }
                "  SLAYER QUEST STARTED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    EventBus.post(SkyblockEvent.Slayer.QuestStart())
                }
            }
        }

        EventBus.register<LocationEvent.WorldChange> {
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
                    1 -> {
                        match.value.also {
                            sbMob.id = it
                            slayerEntities[sbMob.skyblockMob] = sbMob
                        }
                    }
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
    fun getSlayerEntities(): Map<Entity, SkyblockMob> = slayerEntities
    fun getNameTag(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.nameEntity == entity }
    fun getSlayerEntity(): Entity? = SlayerEntity
}