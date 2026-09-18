package com.example.engine

import kotlin.math.*

object ChemistryEngine {
    const val R_GAS = 8.314462618 // J/(mol·K)
    const val FARADAY_CONST = 96485.33212 // C/mol

    // Standard atomic weights (IUPAC recommended values)
    val atomicMasses = mapOf(
        "H" to 1.008, "He" to 4.0026, "Li" to 6.94, "Be" to 9.0122, "B" to 10.81,
        "C" to 12.011, "N" to 14.007, "O" to 15.999, "F" to 18.998, "Ne" to 20.180,
        "Na" to 22.990, "Mg" to 24.305, "Al" to 26.982, "Si" to 28.085, "P" to 30.974,
        "S" to 32.06, "Cl" to 35.45, "Ar" to 39.95, "K" to 39.098, "Ca" to 40.078,
        "Sc" to 44.956, "Ti" to 47.867, "V" to 50.942, "Cr" to 51.996, "Mn" to 54.938,
        "Fe" to 55.845, "Co" to 58.933, "Ni" to 58.693, "Cu" to 63.546, "Zn" to 65.38,
        "Ga" to 69.723, "Ge" to 72.630, "As" to 74.922, "Se" to 78.971, "Br" to 79.904,
        "Kr" to 83.798, "Rb" to 85.468, "Sr" to 87.62, "Ag" to 107.868, "I" to 126.904,
        "Ba" to 137.327, "Pt" to 195.084, "Au" to 196.967, "Hg" to 200.592, "Pb" to 207.2,
        "U" to 238.029
    )

    data class MolarMassResult(
        val formula: String,
        val totalMolarMass: Double,
        val elementBreakdown: Map<String, Pair<Int, Double>> // Element -> (Count, MassPercent)
    )

    fun calculateMolarMass(formula: String): MolarMassResult? {
        val clean = formula.trim()
        if (clean.isEmpty()) return null
        return try {
            val counts = parseFormula(clean)
            var total = 0.0
            for ((elem, count) in counts) {
                val mass = atomicMasses[elem] ?: throw IllegalArgumentException("Unknown element: $elem")
                total += mass * count
            }
            val breakdown = counts.mapValues { (elem, count) ->
                val mass = (atomicMasses[elem] ?: 0.0) * count
                val percent = if (total > 0) (mass / total) * 100.0 else 0.0
                Pair(count, percent)
            }
            MolarMassResult(clean, total, breakdown)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFormula(formula: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val stack = mutableListOf<MutableMap<String, Int>>()
        stack.add(mutableMapOf())

        var i = 0
        while (i < formula.length) {
            val c = formula[i]
            when {
                c == '(' || c == '[' -> {
                    stack.add(mutableMapOf())
                    i++
                }
                c == ')' || c == ']' -> {
                    i++
                    // Read optional number after closing bracket
                    var count = 0
                    while (i < formula.length && formula[i].isDigit()) {
                        count = count * 10 + (formula[i] - '0')
                        i++
                    }
                    if (count == 0) count = 1
                    val top = stack.removeAt(stack.size - 1)
                    val parent = stack.last()
                    for ((elem, cnt) in top) {
                        parent[elem] = (parent[elem] ?: 0) + cnt * count
                    }
                }
                c.isUpperCase() -> {
                    val sb = StringBuilder()
                    sb.append(c)
                    i++
                    if (i < formula.length && formula[i].isLowerCase()) {
                        sb.append(formula[i])
                        i++
                    }
                    val elem = sb.toString()
                    var count = 0
                    while (i < formula.length && formula[i].isDigit()) {
                        count = count * 10 + (formula[i] - '0')
                        i++
                    }
                    if (count == 0) count = 1
                    val currentScope = stack.last()
                    currentScope[elem] = (currentScope[elem] ?: 0) + count
                }
                else -> i++
            }
        }
        return stack.first()
    }

    fun calculatePhFromH(hConcentration: Double): Double {
        require(hConcentration > 0.0) { "Concentration [H+] must be > 0" }
        return -log10(hConcentration)
    }

    fun calculateHFromPh(ph: Double): Double {
        return 10.0.pow(-ph)
    }

    fun calculateBufferPh(pKa: Double, saltMolarity: Double, acidMolarity: Double): Double {
        require(acidMolarity > 0.0 && saltMolarity > 0.0) { "Concentrations must be > 0" }
        return pKa + log10(saltMolarity / acidMolarity)
    }

    fun calculateGibbs(deltaH_kJ: Double, deltaS_J_K: Double, tempKelvin: Double): Double {
        // Delta G = Delta H - T * Delta S
        return deltaH_kJ - tempKelvin * (deltaS_J_K / 1000.0)
    }

    fun calculateNernst(eZeroCell: Double, nElectrons: Int, reactionQuotientQ: Double): Double {
        require(nElectrons > 0) { "Number of electrons must be > 0" }
        require(reactionQuotientQ > 0) { "Reaction quotient Q must be > 0" }
        return eZeroCell - (0.05916 / nElectrons) * log10(reactionQuotientQ)
    }

    fun calculateFirstOrderHalfLife(kRateConstant: Double): Double {
        require(kRateConstant > 0) { "Rate constant k must be > 0" }
        return ln(2.0) / kRateConstant
    }
}
