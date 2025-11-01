package xyz.meowing.zen.ui

enum class Theme(val color: Int) {
    BgDark(0xFF0A0F18.toInt()),           // Color(10, 15, 24, 255)
    Bg(0xFF111A26.toInt()),               // Color(17, 26, 38, 255)
    BgLight(0xFF1A2533.toInt()),          // Color(26, 37, 51, 255)
    Text(0xFFE6EAF0.toInt()),             // Color(230, 234, 240, 255)
    TextMuted(0xFF9AA6B8.toInt()),        // Color(154, 166, 184, 255)
    Highlight(0xFF2F5D9F.toInt()),        // Color(47, 93, 159, 255)
    Border(0xFF1F2F46.toInt()),           // Color(31, 47, 70, 255)
    BorderMuted(0xFF162335.toInt()),      // Color(22, 35, 53, 255)
    Primary(0xFF3A6EA5.toInt()),          // Color(58, 110, 165, 255)
    Secondary(0xFF274B7A.toInt()),        // Color(39, 75, 122, 255)
    Danger(0xFFD25C5C.toInt()),           // Color(210, 92, 92, 255)
    Warning(0xFFE6B65C.toInt()),          // Color(230, 182, 92, 255)
    Success(0xFF4FAF83.toInt()),          // Color(79, 175, 131, 255)
    Info(0xFF4D8CC9.toInt());             // Color(77, 140, 201, 255)

    fun withAlpha(alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (color and 0x00FFFFFF) or (a shl 24)
    }
}