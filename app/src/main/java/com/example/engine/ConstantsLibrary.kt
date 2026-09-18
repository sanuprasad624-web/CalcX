package com.example.engine

data class PhysicalConstant(
    val name: String,
    val symbol: String,
    val value: Double,
    val unit: String,
    val category: String,
    val formattedString: String
)

object ConstantsLibrary {

    val allConstants: List<PhysicalConstant> = listOf(
        PhysicalConstant(
            name = "Speed of Light in Vacuum",
            symbol = "c",
            value = 299792458.0,
            unit = "m/s",
            category = "Universal",
            formattedString = "2.99792458 × 10⁸ m/s"
        ),
        PhysicalConstant(
            name = "Newtonian Constant of Gravitation",
            symbol = "G",
            value = 6.67430e-11,
            unit = "m³/(kg·s²)",
            category = "Universal",
            formattedString = "6.67430 × 10⁻¹¹ N·m²/kg²"
        ),
        PhysicalConstant(
            name = "Planck Constant",
            symbol = "h",
            value = 6.62607015e-34,
            unit = "J·s",
            category = "Quantum",
            formattedString = "6.62607015 × 10⁻³⁴ J·s"
        ),
        PhysicalConstant(
            name = "Reduced Planck Constant (ħ)",
            symbol = "ħ",
            value = 1.054571817e-34,
            unit = "J·s",
            category = "Quantum",
            formattedString = "1.054571817 × 10⁻³⁴ J·s"
        ),
        PhysicalConstant(
            name = "Elementary Charge",
            symbol = "e",
            value = 1.602176634e-19,
            unit = "C",
            category = "Electromagnetic",
            formattedString = "1.602176634 × 10⁻¹⁹ C"
        ),
        PhysicalConstant(
            name = "Electron Rest Mass",
            symbol = "m_e",
            value = 9.1093837015e-31,
            unit = "kg",
            category = "Atomic",
            formattedString = "9.10938370 × 10⁻³¹ kg"
        ),
        PhysicalConstant(
            name = "Proton Rest Mass",
            symbol = "m_p",
            value = 1.67262192369e-27,
            unit = "kg",
            category = "Atomic",
            formattedString = "1.67262192 × 10⁻²⁷ kg"
        ),
        PhysicalConstant(
            name = "Neutron Rest Mass",
            symbol = "m_n",
            value = 1.67492749804e-27,
            unit = "kg",
            category = "Atomic",
            formattedString = "1.67492750 × 10⁻²⁷ kg"
        ),
        PhysicalConstant(
            name = "Avogadro Constant",
            symbol = "N_A",
            value = 6.02214076e23,
            unit = "mol⁻¹",
            category = "Physico-chemical",
            formattedString = "6.02214076 × 10²³ mol⁻¹"
        ),
        PhysicalConstant(
            name = "Molar Gas Constant",
            symbol = "R",
            value = 8.314462618,
            unit = "J/(mol·K)",
            category = "Physico-chemical",
            formattedString = "8.314462618 J/(mol·K)"
        ),
        PhysicalConstant(
            name = "Boltzmann Constant",
            symbol = "k_B",
            value = 1.380649e-23,
            unit = "J/K",
            category = "Thermodynamic",
            formattedString = "1.380649 × 10⁻²³ J/K"
        ),
        PhysicalConstant(
            name = "Vacuum Permittivity (ε₀)",
            symbol = "ε₀",
            value = 8.8541878128e-12,
            unit = "F/m",
            category = "Electromagnetic",
            formattedString = "8.85418781 × 10⁻¹² F/m"
        ),
        PhysicalConstant(
            name = "Vacuum Permeability (μ₀)",
            symbol = "μ₀",
            value = 1.25663706212e-6,
            unit = "N/A²",
            category = "Electromagnetic",
            formattedString = "1.25663706 × 10⁻⁶ N/A²"
        ),
        PhysicalConstant(
            name = "Standard Earth Gravity",
            symbol = "g",
            value = 9.80665,
            unit = "m/s²",
            category = "Universal",
            formattedString = "9.80665 m/s²"
        ),
        PhysicalConstant(
            name = "Stefan-Boltzmann Constant",
            symbol = "σ",
            value = 5.670374419e-8,
            unit = "W/(m²·K⁴)",
            category = "Thermodynamic",
            formattedString = "5.67037442 × 10⁻⁸ W/(m²·K⁴)"
        ),
        PhysicalConstant(
            name = "Rydberg Constant",
            symbol = "R_∞",
            value = 10973731.568160,
            unit = "m⁻¹",
            category = "Atomic",
            formattedString = "1.09737316 × 10⁷ m⁻¹"
        ),
        PhysicalConstant(
            name = "Faraday Constant",
            symbol = "F",
            value = 96485.33212,
            unit = "C/mol",
            category = "Electrochemical",
            formattedString = "96485.332 C/mol"
        ),
        PhysicalConstant(
            name = "Bohr Radius",
            symbol = "a₀",
            value = 5.29177210903e-11,
            unit = "m",
            category = "Atomic",
            formattedString = "5.29177211 × 10⁻¹¹ m"
        )
    )

    fun search(query: String): List<PhysicalConstant> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return allConstants
        return allConstants.filter {
            it.name.lowercase().contains(q) ||
            it.symbol.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }
}
