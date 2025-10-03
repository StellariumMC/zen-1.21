package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis

@Zen.Module
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

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Item Animations", ConfigElement(
                "itemanimations",
                "Enable item animations",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemsize",
                "Item size multiplier",
                ElementType.Slider(-1.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemx",
                "Item X position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemy",
                "Item Y position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Size", ConfigElement(
                "itemz",
                "Item Z position",
                ElementType.Slider(-2.0, 2.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itempitch",
                "Item pitch rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itemyaw",
                "Item yaw rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Rotation", ConfigElement(
                "itemroll",
                "Item roll rotation",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Swing", ConfigElement(
                "itemswingspeed",
                "Swing speed multiplier",
                ElementType.Slider(-2.0, 1.0, 0.0, true)
            ))
            .addElement("Visuals", "Item Animations", "Options", ConfigElement(
                "itemcancelrequip",
                "Cancel item re-equip animation",
                ElementType.Switch(false)
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