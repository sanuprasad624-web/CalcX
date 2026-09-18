package com.example.engine

import kotlin.math.*

data class CalculationStepResult(
    val title: String,
    val formulaUsed: String,
    val substitution: String,
    val answer: String,
    val unit: String,
    val numericalValue: Double
) {
    fun toShareableText(): String {
        return "$title\nFormula: $formulaUsed\nSubstitution: $substitution\nResult: $answer $unit"
    }
}

data class MomentOfInertiaItem(
    val id: String,
    val name: String,
    val axis: String,
    val formulaText: String,
    val explanation: String,
    val calculate: (m: Double, rOrL: Double, other: Double) -> Double
)

object PhysicsEngine {
    const val G = 6.67430e-11
    const val G_EARTH = 9.80665
    const val EPSILON_0 = 8.8541878128e-12
    const val K_COULOMB = 8.9875517923e9

    fun solveProjectile(u: Double, thetaDeg: Double, g: Double = G_EARTH): List<CalculationStepResult> {
        val rad = Math.toRadians(thetaDeg)
        val sinT = sin(rad)
        val sin2T = sin(2 * rad)

        val timeOfFlight = (2 * u * sinT) / g
        val maxHeight = (u * u * sinT * sinT) / (2 * g)
        val range = (u * u * sin2T) / g

        return listOf(
            CalculationStepResult(
                title = "Time of Flight (T)",
                formulaUsed = "T = (2 · u · sinθ) / g",
                substitution = "(2 · $u · sin(${thetaDeg}°)) / $g",
                answer = "%.4f".format(timeOfFlight),
                unit = "s",
                numericalValue = timeOfFlight
            ),
            CalculationStepResult(
                title = "Maximum Height (H_max)",
                formulaUsed = "H = (u² · sin²θ) / (2g)",
                substitution = "($u² · sin²(${thetaDeg}°)) / (2 · $g)",
                answer = "%.4f".format(maxHeight),
                unit = "m",
                numericalValue = maxHeight
            ),
            CalculationStepResult(
                title = "Horizontal Range (R)",
                formulaUsed = "R = (u² · sin(2θ)) / g",
                substitution = "($u² · sin(${2 * thetaDeg}°)) / $g",
                answer = "%.4f".format(range),
                unit = "m",
                numericalValue = range
            )
        )
    }

    fun solveKinematics(u: Double, a: Double, t: Double): List<CalculationStepResult> {
        val v = u + a * t
        val s = u * t + 0.5 * a * t * t
        return listOf(
            CalculationStepResult(
                title = "Final Velocity (v)",
                formulaUsed = "v = u + a · t",
                substitution = "$u + ($a) · $t",
                answer = "%.4f".format(v),
                unit = "m/s",
                numericalValue = v
            ),
            CalculationStepResult(
                title = "Displacement (s)",
                formulaUsed = "s = u · t + ½ · a · t²",
                substitution = "$u · $t + 0.5 · ($a) · $t²",
                answer = "%.4f".format(s),
                unit = "m",
                numericalValue = s
            )
        )
    }

    fun solveGravitation(m1: Double, m2: Double, r: Double): CalculationStepResult {
        val force = G * (m1 * m2) / (r * r)
        return CalculationStepResult(
            title = "Gravitational Force (F)",
            formulaUsed = "F = G · (m₁ · m₂) / r²",
            substitution = "($G · $m1 · $m2) / ($r)²",
            answer = "%.4e".format(force),
            unit = "N",
            numericalValue = force
        )
    }

    fun solveSHM(mass: Double, springK: Double): CalculationStepResult {
        val period = 2 * PI * sqrt(mass / springK)
        val frequency = 1.0 / period
        return CalculationStepResult(
            title = "Time Period (T)",
            formulaUsed = "T = 2π · √(m / k)",
            substitution = "2π · √($mass / $springK)",
            answer = "%.4f".format(period),
            unit = "s",
            numericalValue = period
        )
    }

    fun solveCoulomb(q1: Double, q2: Double, r: Double): CalculationStepResult {
        val force = K_COULOMB * abs(q1 * q2) / (r * r)
        return CalculationStepResult(
            title = "Electrostatic Force (F)",
            formulaUsed = "F = (1 / 4πε₀) · (|q₁ · q₂| / r²)",
            substitution = "(${K_COULOMB} · |$q1 · $q2|) / ($r)²",
            answer = "%.4e".format(force),
            unit = "N",
            numericalValue = force
        )
    }

    fun solveCapacitance(area: Double, distance: Double, dielectric: Double = 1.0): CalculationStepResult {
        val c = dielectric * EPSILON_0 * area / distance
        return CalculationStepResult(
            title = "Capacitance (C)",
            formulaUsed = "C = (κ · ε₀ · A) / d",
            substitution = "($dielectric · $EPSILON_0 · $area) / $distance",
            answer = "%.4e".format(c),
            unit = "F",
            numericalValue = c
        )
    }

    val momentOfInertiaLibrary: List<MomentOfInertiaItem> = listOf(
        MomentOfInertiaItem(
            id = "rod_center",
            name = "Thin Uniform Rod",
            axis = "Perpendicular through center",
            formulaText = "I = (1/12) · M · L²",
            explanation = "Uniform slender rod of mass M and length L rotated about transverse center axis.",
            calculate = { m, l, _ -> (1.0 / 12.0) * m * l * l }
        ),
        MomentOfInertiaItem(
            id = "rod_end",
            name = "Thin Uniform Rod",
            axis = "Perpendicular through one end",
            formulaText = "I = (1/3) · M · L²",
            explanation = "From parallel axis theorem: I = I_cm + M(L/2)² = ML²/12 + ML²/4 = ML²/3.",
            calculate = { m, l, _ -> (1.0 / 3.0) * m * l * l }
        ),
        MomentOfInertiaItem(
            id = "ring_center",
            name = "Thin Circular Ring / Hoop",
            axis = "Perpendicular through center (symmetry axis)",
            formulaText = "I = M · R²",
            explanation = "All mass particles are equidistant at radius R from central axis.",
            calculate = { m, r, _ -> m * r * r }
        ),
        MomentOfInertiaItem(
            id = "ring_diameter",
            name = "Thin Circular Ring",
            axis = "Diameter axis (in plane of ring)",
            formulaText = "I = (1/2) · M · R²",
            explanation = "By perpendicular axis theorem: I_z = I_x + I_y = 2·I_dia, so I_dia = MR²/2.",
            calculate = { m, r, _ -> 0.5 * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "disc_center",
            name = "Uniform Circular Disc",
            axis = "Perpendicular through center",
            formulaText = "I = (1/2) · M · R²",
            explanation = "Integrating concentric thin rings from r = 0 to R.",
            calculate = { m, r, _ -> 0.5 * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "disc_diameter",
            name = "Uniform Circular Disc",
            axis = "Diameter axis in plane",
            formulaText = "I = (1/4) · M · R²",
            explanation = "Perpendicular axis theorem for flat disc: I_dia = (1/2)·I_center = MR²/4.",
            calculate = { m, r, _ -> 0.25 * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "solid_sphere",
            name = "Solid Sphere",
            axis = "Any diameter axis",
            formulaText = "I = (2/5) · M · R²",
            explanation = "Uniform density solid sphere of mass M and radius R rotated through center.",
            calculate = { m, r, _ -> 0.4 * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "hollow_sphere",
            name = "Thin Spherical Shell",
            axis = "Any diameter axis",
            formulaText = "I = (2/3) · M · R²",
            explanation = "Hollow thin-walled spherical bubble of mass M and radius R.",
            calculate = { m, r, _ -> (2.0 / 3.0) * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "solid_cylinder",
            name = "Solid Cylinder",
            axis = "Central longitudinal axis",
            formulaText = "I = (1/2) · M · R²",
            explanation = "Equivalent to stacked circular discs along the cylinder height.",
            calculate = { m, r, _ -> 0.5 * m * r * r }
        ),
        MomentOfInertiaItem(
            id = "rect_plate",
            name = "Rectangular Plate (a × b)",
            axis = "Perpendicular through center",
            formulaText = "I = (1/12) · M · (a² + b²)",
            explanation = "Mass M, sides a and b. Sum of moments along two orthogonal axes.",
            calculate = { m, a, b -> (1.0 / 12.0) * m * (a * a + b * b) }
        )
    )
}
