package xyz.meowing.zen.features.general

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.command.utils.GreedyString
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.utils.ChatUtils

@Zen.Command
object CalculatorCommand : Commodore("zencalc", "calc") {
    init {
        runs { equation: GreedyString ->
            try {
                val eq = equation.string
                val clean = eq.replace(Regex("[^0-9+\\-*/().\\s]"), "").replace("\\s".toRegex(), "")
                val result = eval(clean)
                val display = if (result == result.toInt().toDouble()) {
                    result.toInt().toString()
                } else {
                    "%.10f".format(result).trimEnd('0').trimEnd('.')
                }
                ChatUtils.addMessage("$prefix §b$eq §f= §b$display")
            } catch (e: Exception) {
                ChatUtils.addMessage("$prefix §fInvalid equation.")
            }
        }
    }

    private fun eval(s: String): Double {
        var i = 0
        fun next() = if (i < s.length) s[i++] else 0.toChar()
        fun peek() = if (i < s.length) s[i] else 0.toChar()

        fun num(): Double {
            var n = 0.0
            while (peek().isDigit()) n = n * 10 + (next() - '0')
            if (peek() == '.') {
                next()
                var d = 0.1
                while (peek().isDigit()) {
                    n += (next() - '0') * d
                    d *= 0.1
                }
            }
            return n
        }

        lateinit var expr: () -> Double
        lateinit var term: () -> Double
        lateinit var factor: () -> Double

        expr = {
            var r = term()
            while (peek() in "+-") r = if (next() == '+') r + term() else r - term()
            r
        }

        term = {
            var r = factor()
            while (peek() in "*/") r = if (next() == '*') r * factor() else r / factor()
            r
        }

        factor = {
            when (peek()) {
                '(' -> { next(); val r = expr(); next(); r }
                '-' -> { next(); -factor() }
                '+' -> { next(); factor() }
                else -> num()
            }
        }

        return expr()
    }
}