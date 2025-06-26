package meowing.zen.hud

data class HudElement(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var scale: Float = 1.0f,
    var enabled: Boolean = true,
    val id: String,
    val name: String
) {
    fun getActualX(screenWidth: Int): Float = x
    fun getActualY(screenHeight: Int): Float = y
}

data class HudConfig(
    val elements: MutableMap<String, HudElementData> = mutableMapOf()
)

data class HudElementData(
    var x: Float = 0f,
    var y: Float = 0f,
    var scale: Float = 1.0f,
    var enabled: Boolean = true
)