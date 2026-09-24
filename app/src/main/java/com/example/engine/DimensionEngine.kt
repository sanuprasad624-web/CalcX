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

    fun toLatexString(): String {
        if (m == 0 && l == 0 && t == 0 && i == 0 && th == 0) return "[\\text{M}^0\\,\\text{L}^0\\,\\text{T}^0]"
        val sb = StringBuilder("[")
        fun appendUnit(letter: String, exp: Int) {
            if (exp != 0) {
                sb.append("\\text{").append(letter).append("}")
                if (exp != 1) sb.append("^{").append(exp).append("}")
                sb.append("\\,")
            }
        }
        appendUnit("M", m)
        appendUnit("L", l)
        appendUnit("T", t)
        appendUnit("A", i)
        appendUnit("K", th)
        return sb.toString().trimEnd('\\', ',') + "]"
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
        KnownQuantity("Work / Energy / Heat", Dimension(m = 1, l = 2, t = -2), "J (N·m)"),
        KnownQuantity("Power", Dimension(m = 1, l = 2, t = -3), "W (J/s)"),
        KnownQuantity("Pressure / Stress / Modulus", Dimension(m = 1, l = -1, t = -2), "Pa (N/m²)"),
        KnownQuantity("Linear Momentum / Impulse", Dimension(m = 1, l = 1, t = -1), "kg·m/s"),
        KnownQuantity("Torque / Moment of Force", Dimension(m = 1, l = 2, t = -2), "N·m"),
        KnownQuantity("Angular Momentum", Dimension(m = 1, l = 2, t = -1), "J·s (kg·m²/s)"),
        KnownQuantity("Moment of Inertia", Dimension(m = 1, l = 2), "kg·m²"),
        KnownQuantity("Density", Dimension(m = 1, l = -3), "kg/m³"),
        KnownQuantity("Frequency / Angular Velocity", Dimension(t = -1), "Hz / rad/s"),
        KnownQuantity("Gravitational Constant (G)", Dimension(m = -1, l = 3, t = -2), "N·m²/kg²"),
        KnownQuantity("Planck Constant (h)", Dimension(m = 1, l = 2, t = -1), "J·s"),
        KnownQuantity("Surface Tension / Spring Constant", Dimension(m = 1, t = -2), "N/m"),
        KnownQuantity("Coefficient of Viscosity (η)", Dimension(m = 1, l = -1, t = -1), "Pa·s"),
        KnownQuantity("Electric Charge", Dimension(t = 1, i = 1), "C (A·s)"),
        KnownQuantity("Electric Current", Dimension(i = 1), "A"),
        KnownQuantity("Electric Potential / Voltage / EMF", Dimension(m = 1, l = 2, t = -3, i = -1), "V"),
        KnownQuantity("Electric Resistance", Dimension(m = 1, l = 2, t = -3, i = -2), "Ω"),
        KnownQuantity("Capacitance", Dimension(m = -1, l = -2, t = 4, i = 2), "F"),
        KnownQuantity("Magnetic Field / Induction (B)", Dimension(m = 1, t = -2, i = -1), "T"),
        KnownQuantity("Magnetic Flux (Φ)", Dimension(m = 1, l = 2, t = -2, i = -1), "Wb"),
        KnownQuantity("Self / Mutual Inductance (L)", Dimension(m = 1, l = 2, t = -2, i = -2), "H"),
        KnownQuantity("Permittivity of Free Space (ε₀)", Dimension(m = -1, l = -3, t = 4, i = 2), "F/m"),
        KnownQuantity("Permeability of Free Space (μ₀)", Dimension(m = 1, l = 1, t = -2, i = -2), "H/m"),
        KnownQuantity("Specific Heat Capacity", Dimension(l = 2, t = -2, th = -1), "J/(kg·K)"),
        KnownQuantity("Thermal Conductivity (k)", Dimension(m = 1, l = 1, t = -3, th = -1), "W/(m·K)"),
        KnownQuantity("Stefan-Boltzmann Constant (σ)", Dimension(m = 1, t = -3, th = -4), "W/(m²·K⁴)"),
        KnownQuantity("Universal Gas Constant (R) / Boltzmann (k_B)", Dimension(m = 1, l = 2, t = -2, th = -1), "J/(mol·K) / J/K")
    )

    fun findMatches(dim: Dimension): List<KnownQuantity> {
        return KNOWN_QUANTITIES.filter { it.dimension == dim }
    }

    fun searchQuantities(query: String): List<KnownQuantity> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return KNOWN_QUANTITIES
        return KNOWN_QUANTITIES.filter {
            it.name.lowercase().contains(trimmed) ||
                    it.siUnit.lowercase().contains(trimmed) ||
                    it.dimension.toFormattedString().lowercase().contains(trimmed)
        }
    }
}
