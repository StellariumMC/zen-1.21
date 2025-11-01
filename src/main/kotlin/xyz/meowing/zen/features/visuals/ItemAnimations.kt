package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object ItemAnimations : Feature("itemanimations") {
    private val itemSize by ConfigDelegate<Double>("itemsize")
    private val itemX by ConfigDelegate<Double>("itemx")
    private val itemY by ConfigDelegate<Double>("itemy")
    private val itemZ by ConfigDelegate<Double>("itemz")
    private val itemPitch by ConfigDelegate<Double>("itempitch")
    private val itemYaw by ConfigDelegate<Double>("itemyaw")
    private val itemRoll by ConfigDelegate<Double>("itemroll")
    val cancelReEquip by ConfigDelegate<Boolean>("itemcancelrequip")
    val swingSpeed by ConfigDelegate<Double>("itemswingspeed")

    override fun addConfig() {
        ConfigManager
            .addFeature("Item Animations", "Enable item animations", "Visuals", ConfigElement(
                "itemanimations",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Size", "Item size multiplier", "Size", ConfigElement(
                "itemsize",
                ElementType.Slider(-1.0, 2.0, 0.0, true)
            ))
            .addFeatureOption("X Position", "Item X position", "Position", ConfigElement(
                "itemx",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addFeatureOption("Y Position", "Item Y position", "Position", ConfigElement(
                "itemy",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addFeatureOption("Z Position", "Item Z position", "Position", ConfigElement(
                "itemz",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addFeatureOption("Pitch", "Item pitch rotation", "Rotation", ConfigElement(
                "itempitch",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Yaw", "Item yaw rotation", "Rotation", ConfigElement(
                "itemyaw",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Roll", "Item roll rotation", "Rotation", ConfigElement(
                "itemroll",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Cancel Re-Equip", "Cancel item re-equip animations", "Other", ConfigElement(
                    "itemcancelrequip",
                    ElementType.Switch(false)
            ))
            .addFeatureOption("Swing Speed", "Item swing speed multiplier", "Other", ConfigElement(
                    "itemswingspeed",
                    ElementType.Slider(-2.0, 1.0, 0.0, true)
            ))
    }

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