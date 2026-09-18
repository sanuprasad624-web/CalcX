package com.example.engine

data class FormulaItem(
    val id: String,
    val name: String,
    val category: String, // "Physics", "Chemistry", "Mathematics", "Engineering"
    val subCategory: String,
    val formulaDisplay: String,
    val variables: List<String>,
    val units: String,
    val conditions: String,
    val explanation: String,
    val example: String
)

object FormulaLibrary {

    val formulas: List<FormulaItem> = listOf(
        // PHYSICS - MECHANICS
        FormulaItem(
            id = "proj_range",
            name = "Horizontal Range of Projectile",
            category = "Physics",
            subCategory = "Kinematics",
            formulaDisplay = "R = (u² · sin(2θ)) / g",
            variables = listOf("u: Initial velocity", "θ: Launch angle with horizontal", "g: Acceleration due to gravity"),
            units = "u (m/s), θ (degrees/radians), g (m/s²), R (m)",
            conditions = "Flat horizontal surface, negligible air resistance, uniform gravitational field",
            explanation = "Maximum range is achieved at launch angle θ = 45° where sin(2θ) = 1.",
            example = "If u = 20 m/s and θ = 30° on Earth (g = 9.8 m/s²), R = (400 · sin 60°) / 9.8 ≈ 35.35 m."
        ),
        FormulaItem(
            id = "proj_height",
            name = "Maximum Height of Projectile",
            category = "Physics",
            subCategory = "Kinematics",
            formulaDisplay = "H = (u² · sin²θ) / (2g)",
            variables = listOf("u: Initial speed", "θ: Angle of projection", "g: Gravity"),
            units = "H (m)",
            conditions = "Flat terrain, no drag",
            explanation = "Height at which vertical velocity component v_y momentarily becomes zero.",
            example = "At θ = 90° (vertical throw), H = u² / (2g)."
        ),
        FormulaItem(
            id = "shm_spring",
            name = "Time Period of Spring-Mass System",
            category = "Physics",
            subCategory = "SHM & Oscillations",
            formulaDisplay = "T = 2π · √(m / k)",
            variables = listOf("m: Mass attached", "k: Spring stiffness constant"),
            units = "m (kg), k (N/m), T (s)",
            conditions = "Ideal massless spring obeying Hooke's law, small oscillations",
            explanation = "Independent of amplitude and local gravitational acceleration.",
            example = "If m = 1 kg and k = 100 N/m, T = 2π · √(0.01) = 2π · 0.1 ≈ 0.628 s."
        ),
        FormulaItem(
            id = "grav_force",
            name = "Newton's Law of Gravitation",
            category = "Physics",
            subCategory = "Gravitation",
            formulaDisplay = "F = G · (m₁ · m₂) / r²",
            variables = listOf("m₁, m₂: Masses", "r: Separation distance between centers", "G: Gravitational constant (6.674×10⁻¹¹)"),
            units = "F (N), m (kg), r (m)",
            conditions = "Point masses or spherically symmetric mass distributions",
            explanation = "Always attractive central force along the line connecting centers.",
            example = "Force between two 1 kg masses separated by 1 m is 6.674×10⁻¹¹ N."
        ),
        FormulaItem(
            id = "coulomb_law",
            name = "Coulomb's Law of Electrostatics",
            category = "Physics",
            subCategory = "Electrostatics",
            formulaDisplay = "F = (1 / (4πε₀)) · (|q₁ · q₂| / r²)",
            variables = listOf("q₁, q₂: Charges", "r: Separation distance", "ε₀: Vacuum permittivity"),
            units = "q (C), r (m), F (N)",
            conditions = "Stationary point charges in vacuum/air",
            explanation = "Like charges repel with positive force, unlike charges attract.",
            example = "Two 1 μC charges separated by 0.1 m experience F = 8.99×10⁹ · (10⁻⁶)² / (0.1)² ≈ 0.899 N."
        ),

        // CHEMISTRY
        FormulaItem(
            id = "ideal_gas",
            name = "Ideal Gas Equation",
            category = "Chemistry",
            subCategory = "Gaseous State",
            formulaDisplay = "P · V = n · R · T",
            variables = listOf("P: Pressure", "V: Volume", "n: Moles", "R: Universal gas constant (8.314 J/(mol·K))", "T: Absolute temperature in Kelvin"),
            units = "P (Pa or atm), V (m³ or L), T (K)",
            conditions = "High temperature and low pressure where molecular volume and intermolecular forces are negligible",
            explanation = "Combines Boyle's, Charles's, and Avogadro's laws into a unified equation of state.",
            example = "At STP (T = 273.15 K, P = 101.325 kPa), 1 mole occupies 22.414 L."
        ),
        FormulaItem(
            id = "nernst_eq",
            name = "Nernst Equation (at 298 K)",
            category = "Chemistry",
            subCategory = "Electrochemistry",
            formulaDisplay = "E_cell = E°_cell - (0.05916 / n) · log₁₀(Q)",
            variables = listOf("E_cell: Cell potential", "E°_cell: Standard cell potential", "n: Number of electrons transferred", "Q: Reaction quotient"),
            units = "E (Volts)",
            conditions = "Temperature at 298.15 K (25°C), reversible electrochemical cells",
            explanation = "Calculates reduction potential under non-standard concentration conditions.",
            example = "For Zn/Cu Daniell cell, E° = 1.10 V. If [Zn²⁺]/[Cu²⁺] = 10, E = 1.10 - (0.0592/2)·1 = 1.07 V."
        ),
        FormulaItem(
            id = "henderson_hasselbalch",
            name = "Henderson-Hasselbalch Equation",
            category = "Chemistry",
            subCategory = "Ionic Equilibrium",
            formulaDisplay = "pH = pK_a + log₁₀([A⁻] / [HA])",
            variables = listOf("[A⁻]: Conjugate base molarity", "[HA]: Weak acid molarity", "pK_a: -log₁₀(K_a)"),
            units = "Molarity (mol/L)",
            conditions = "Dilute buffer solutions where dissociation does not significantly alter equilibrium concentrations",
            explanation = "Predicts pH of buffer solutions; when [A⁻] = [HA], pH = pK_a.",
            example = "Acetic acid (pK_a = 4.76) with equal acetate and acetic acid concentrations has pH = 4.76."
        ),

        // MATHEMATICS
        FormulaItem(
            id = "quad_formula",
            name = "Quadratic Formula",
            category = "Mathematics",
            subCategory = "Algebra",
            formulaDisplay = "x = (-b ± √(b² - 4ac)) / (2a)",
            variables = listOf("a, b, c: Coefficients of ax² + bx + c = 0 (a ≠ 0)"),
            units = "Dimensionless / variable specific",
            conditions = "a ≠ 0",
            explanation = "Yields both real and complex roots based on the sign of the discriminant Δ = b² - 4ac.",
            example = "For x² - 5x + 6 = 0: x = (5 ± √(25 - 24))/2 = (5 ± 1)/2 = 3 or 2."
        ),
        FormulaItem(
            id = "ap_sum",
            name = "Sum of Arithmetic Progression (AP)",
            category = "Mathematics",
            subCategory = "Sequences & Series",
            formulaDisplay = "S_n = (n / 2) · [2a + (n - 1)d]",
            variables = listOf("n: Number of terms", "a: First term", "d: Common difference"),
            units = "Scalar",
            conditions = "n is a positive integer",
            explanation = "Sum of n evenly spaced terms; can also be expressed as n/2 · (first + last).",
            example = "Sum of first 100 positive integers (a=1, d=1, n=100): 50 · (2 + 99) = 5050."
        ),
        FormulaItem(
            id = "gp_infinite",
            name = "Sum of Infinite Geometric Progression",
            category = "Mathematics",
            subCategory = "Sequences & Series",
            formulaDisplay = "S_∞ = a / (1 - r)",
            variables = listOf("a: First term", "r: Common ratio"),
            units = "Scalar",
            conditions = "|r| < 1 (convergent geometric series)",
            explanation = "The series diverges if |r| ≥ 1.",
            example = "1 + 1/2 + 1/4 + 1/8 + ... has a = 1, r = 1/2. Sum = 1 / (1 - 0.5) = 2."
        ),

        // ENGINEERING
        FormulaItem(
            id = "ohms_law",
            name = "Ohm's Law & Electrical Power",
            category = "Engineering",
            subCategory = "Electrical Engineering",
            formulaDisplay = "V = I · R  |  P = V · I = I² · R = V² / R",
            variables = listOf("V: Voltage", "I: Current", "R: Resistance", "P: Power"),
            units = "V (Volts), I (Amperes), R (Ohms), P (Watts)",
            conditions = "Ohmic conductors with constant temperature",
            explanation = "Core relationship governing linear electrical circuits.",
            example = "A 230 V source across a 46 Ω heater draws 5 A and dissipates 1150 W."
        ),
        FormulaItem(
            id = "hookes_law_stress",
            name = "Young's Modulus (Hooke's Law in Solids)",
            category = "Engineering",
            subCategory = "Mechanical Engineering",
            formulaDisplay = "Y = Stress / Strain = (F / A) / (ΔL / L₀)",
            variables = listOf("F: Applied tensile force", "A: Cross-sectional area", "ΔL: Elongation", "L₀: Original length"),
            units = "Y (Pa or N/m²)",
            conditions = "Within proportional elastic limit of material",
            explanation = "Measures stiffness and ability of a solid material to withstand elastic deformation.",
            example = "Steel has Y ≈ 200 GPa = 2×10¹¹ Pa."
        )
    )

    fun search(query: String, categoryFilter: String? = null): List<FormulaItem> {
        val q = query.trim().lowercase()
        return formulas.filter { item ->
            val matchCat = categoryFilter == null || categoryFilter == "All" || item.category.equals(categoryFilter, ignoreCase = true)
            val matchQuery = q.isEmpty() ||
                    item.name.lowercase().contains(q) ||
                    item.formulaDisplay.lowercase().contains(q) ||
                    item.subCategory.lowercase().contains(q) ||
                    item.explanation.lowercase().contains(q)
            matchCat && matchQuery
        }
    }
}
