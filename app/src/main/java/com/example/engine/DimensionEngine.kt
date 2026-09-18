package com.example.engine

/**
 * Representation of physical dimensions [M^m L^l T^t I^i Θ^th N^n J^j]
 */
data class Dimension(
    val m: Int = 0, // Mass
    val l: Int = 0, // Length
    val t: Int = 0, // Time
    val i: Int = 0, // Current
    val th: Int = 0 // Temperature
) {
    operator fun times(other: Dimension) = Dimension(
        m = m + other.m,
        l = l + other.l,
        t = t + other.t,
        i = i + other.i,
        th = th + other.th
    )

    operator fun div(other: Dimension) = Dimension(
        m = m - other.m,
        l = l - other.l,
        t = t - other.t,
        i = i - other.i,
        th = th - other.th
    )

    fun pow(power: Int) = Dimension(
        m = m * power,
        l = l * power,
        t = t * power,
        i = i * power,
        th = th * power
    )

    fun toFormattedString(): String {
        if (m == 0 && l == 0 && t == 0 && i == 0 && th == 0) return "[Dimensionless: M⁰ L⁰ T⁰]"
        val sb = StringBuilder("[")
        fun appendUnit(letter: String, exp: Int) {
            if (exp != 0) {
                sb.append(letter)
                if (exp != 1) sb.append(toSuperscript(exp))
                sb.append(" ")
            }
        }
        appendUnit("M", m)
        appendUnit("L", l)
        appendUnit("T", t)
        appendUnit("A", i)
        appendUnit("K", th)
        return sb.toString().trim() + "]"
    }

    private fun toSuperscript(num: Int): String {
        val s = num.toString()
        val out = StringBuilder()
        for (c in s) {
            out.append(when (c) {
                '-' -> '⁻'
                '0' -> '⁰'
                '1' -> '¹'
                '2' -> '²'
                '3' -> '³'
                '4' -> '⁴'
                '5' -> '⁵'
                '6' -> '⁶'
                '7' -> '⁷'
                '8' -> '⁸'
                '9' -> '⁹'
                else -> c
            })
        }
        return out.toString()
    }
}

data class KnownQuantity(
    val name: String,
    val dimension: Dimension,
    val siUnit: String
)

object DimensionEngine {
    val KNOWN_QUANTITIES = listOf(
        KnownQuantity("Velocity / Speed", Dimension(l = 1, t = -1), "m/s"),
        KnownQuantity("Acceleration", Dimension(l = 1, t = -2), "m/s²"),
        KnownQuantity("Force", Dimension(m = 1, l = 1, t = -2), "N (kg·m/s²)"),
        KnownQuantity("Work / Energy", Dimension(m = 1, l = 2, t = -2), "J (N·m)"),
        KnownQuantity("Power", Dimension(m = 1, l = 2, t = -3), "W (J/s)"),
        KnownQuantity("Pressure / Stress", Dimension(m = 1, l = -1, t = -2), "Pa (N/m²)"),
        KnownQuantity("Linear Momentum / Impulse", Dimension(m = 1, l = 1, t = -1), "kg·m/s"),
        KnownQuantity("Torque", Dimension(m = 1, l = 2, t = -2), "N·m"),
        KnownQuantity("Gravitational Constant (G)", Dimension(m = -1, l = 3, t = -2), "N·m²/kg²"),
        KnownQuantity("Planck Constant (h)", Dimension(m = 1, l = 2, t = -1), "J·s"),
        KnownQuantity("Surface Tension", Dimension(m = 1, t = -2), "N/m"),
        KnownQuantity("Viscosity (η)", Dimension(m = 1, l = -1, t = -1), "Pa·s"),
        KnownQuantity("Electric Charge", Dimension(t = 1, i = 1), "C (A·s)"),
        KnownQuantity("Electric Potential / Voltage", Dimension(m = 1, l = 2, t = -3, i = -1), "V"),
        KnownQuantity("Electric Resistance", Dimension(m = 1, l = 2, t = -3, i = -2), "Ω"),
        KnownQuantity("Capacitance", Dimension(m = -1, l = -2, t = 4, i = 2), "F"),
        KnownQuantity("Magnetic Field (B)", Dimension(m = 1, t = -2, i = -1), "T")
    )

    fun findMatches(dim: Dimension): List<KnownQuantity> {
        return KNOWN_QUANTITIES.filter { it.dimension == dim }
    }
}
