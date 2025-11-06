package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object ItemAnimations : Feature(
    "itemAnimations"
) {
    private val itemSize by ConfigDelegate<Double>("itemAnimations.itemSize")
    private val itemX by ConfigDelegate<Double>("itemAnimations.itemX")
    private val itemY by ConfigDelegate<Double>("itemAnimations.itemY")
    private val itemZ by ConfigDelegate<Double>("itemAnimations.itemZ")
    private val itemPitch by ConfigDelegate<Double>("itemAnimations.itemPitch")
    private val itemYaw by ConfigDelegate<Double>("itemAnimations.itemYaw")
    private val itemRoll by ConfigDelegate<Double>("itemAnimations.itemRoll")
    val cancelReEquip by ConfigDelegate<Boolean>("itemAnimations.cancelReEquip")
    val swingSpeed by ConfigDelegate<Double>("itemAnimations.swingSpeed")
    val noSwing by ConfigDelegate<Boolean>("itemAnimations.noSwing")
    val noSwingTerm by ConfigDelegate<Boolean>("itemAnimations.noSwingTerm")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Item animations",
                "Enable item animations",
                "Visuals",
                ConfigElement(
                    "itemAnimations",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Size",
                ConfigElement(
                    "itemAnimations.itemSize",
                    ElementType.Slider(-1.0, 2.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "X position",
                ConfigElement(
                    "itemAnimations.itemX",
                    ElementType.Slider(-2.0, 2.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Y position",
                ConfigElement(
                    "itemAnimations.itemY",
                    ElementType.Slider(-2.0, 2.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Z position",
                ConfigElement(
                    "itemAnimations.itemZ",
                    ElementType.Slider(-2.0, 2.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Pitch",
                ConfigElement(
                    "itemAnimations.itemPitch",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Yaw",
                ConfigElement(
                    "itemAnimations.itemYaw",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Roll",
                ConfigElement(
                    "itemAnimations.itemRoll",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Cancel re-equip",
                ConfigElement(
                    "itemAnimations.cancelReEquip",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Swing speed",
                ConfigElement(
                    "itemAnimations.swingSpeed",
                    ElementType.Slider(-2.0, 1.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "No swing",
                ConfigElement(
                    "itemAnimations.noSwing",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "No terminator swing",
                ConfigElement(
                    "itemAnimations.noSwingTerm",
                    ElementType.Switch(false)
                )
            )
    }

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
        fun apply(matrices: MatrixStack) {
            matrices.translate(posX, posY, posZ)
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotX))
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotZ))
            matrices.scale(scale, scale, scale)
        }
    }
}