package com.example.math.formula

/**
 * Canonical JEE Formula Model.
 * Represents a mathematically verified formula with comprehensive metadata,
 * variable definitions, conditions, common mistakes, and solver bindings.
 */
data class CanonicalFormula(
    val id: String,
    val subject: FormulaSubject, // MATHEMATICS, PHYSICS, CHEMISTRY
    val classLevel: String, // "Class 11", "Class 12"
    val chapter: String,
    val topic: String,
    val name: String,
    val latex: String,
    val displayFormula: String = latex,
    val variables: List<VariableDef>,
    val siUnit: String = "",
    val conditions: String = "",
    val commonMistake: String = "",
    val explanation: String = "",
    val example: String = "",
    val jeeLevel: JeeLevel = JeeLevel.BOTH,
    val tags: List<String> = emptyList(),
    val searchKeywords: List<String> = emptyList(),
    val relatedFormulaIds: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

enum class FormulaSubject(val displayName: String) {
    MATHEMATICS("Mathematics"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry")
}

enum class JeeLevel(val displayName: String) {
    MAIN("JEE Main"),
    ADVANCED("JEE Advanced"),
    BOTH("Main & Advanced")
}

data class VariableDef(
    val symbol: String,
    val name: String,
    val unit: String = ""
)
