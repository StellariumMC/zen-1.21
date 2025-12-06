package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.features.Feature
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.zen.annotations.Module

@Module
object ItemAnimations : Feature(
    "itemAnimations",
    "Item animations",
    "Modifies item size and swing animations",
    "Visuals"
) {
    private val itemSize by config.slider("Size", 0.0, -1.0, 2.0, true)
    private val itemX by config.slider("X position", 0.0, -2.0, 2.0, true)
    private val itemY by config.slider("Y position", 0.0, -2.0, 2.0, true)
    private val itemZ by config.slider("Z position", 0.0, -2.0, 2.0, true)
    private val itemPitch by config.slider("Pitch", 0.0, -180.0, 180.0, true)
    private val itemYaw by config.slider("Yaw", 0.0, -180.0, 180.0, true)
    private val itemRoll by config.slider("Roll", 0.0, -180.0, 180.0, true)

    val swingSpeed by config.slider("Swing speed", 0.0, -2.0, 1.0, true)
    val cancelReEquip by config.switch("Cancel re-equip")
    val noSwing by config.switch("No swing")
    val noSwingTerm by config.switch("No terminator swing")

    @JvmStatic
    fun noSwingTerm(): Boolean {
        return noSwingTerm && KnitPlayer.heldItem?.getData(DataTypes.SKYBLOCK_ID)?.id == "TERMINATOR"
    }

    @JvmStatic
    fun getItemTransform(): ItemTransform {
        return ItemTransform(
            posX = (itemX * 75.0).toFloat() / 100f,
            posY = (itemY * 75.0).toFloat() / 100f,
            posZ = (itemZ * 25.0).toFloat() / 100f,
            rotX = itemPitch.toFloat(),
            rotY = itemYaw.toFloat(),
            rotZ = itemRoll.toFloat(),
            scale = (1.0 + itemSize).toFloat()
        )
    }

    data class ItemTransform(
        val posX: Float,
        val posY: Float,
        val posZ: Float,
        val rotX: Float,
        val rotY: Float,
        val rotZ: Float,
        val scale: Float
    ) {
        fun apply(matrices: PoseStack) {
            matrices.translate(posX, posY, posZ)
            matrices.mulPose(Axis.XP.rotationDegrees(rotX))
            matrices.mulPose(Axis.YP.rotationDegrees(rotY))
            matrices.mulPose(Axis.ZP.rotationDegrees(rotZ))
            matrices.scale(scale, scale, scale)
        }
    }
}