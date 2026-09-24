package com.example.math.editor

import java.util.UUID

/**
 * Structured Mathematical Input Element in the AST.
 */
sealed interface MathElement {
    fun toLatex(activeSlotId: String?, cursorIndex: Int): String
    fun toCalculusString(): String
    fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String
    fun getChildSlots(): List<MathSlot>
}

/**
 * Single editable character or symbol (digit, variable x/y, operator +, -, *, =, etc.).
 */
data class CharElement(val char: Char) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String = when (char) {
        '*', '·' -> "\\cdot "
        '×' -> "\\times "
        '÷' -> "\\div "
        'π' -> "\\pi "
        'θ' -> "\\theta "
        '<' -> "< "
        '>' -> "> "
        '=' -> "= "
        '+' -> "+ "
        '-' -> "- "
        ' ' -> "\\; "
        else -> char.toString()
    }

    override fun toCalculusString(): String = when (char) {
        '·', '×' -> "*"
        '÷' -> "/"
        'π' -> "pi"
        'θ' -> "theta"
        else -> char.toString()
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String = char.toString()

    override fun getChildSlots(): List<MathSlot> = emptyList()
}

/**
 * An independently editable slot containing a sequence of MathElements.
 */
data class MathSlot(
    val id: String = UUID.randomUUID().toString(),
    val elements: MutableList<MathElement> = mutableListOf(),
    val placeholder: String = "⬚"
) {
    val isEmpty: Boolean get() = elements.isEmpty()

    fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val isThisActive = (id == activeSlotId)
        if (elements.isEmpty()) {
            return if (isThisActive) {
                "{\\color{#2563EB}\\vert}"
            } else {
                "{\\color{#94A3B8}\\Box}"
            }
        }

        val sb = StringBuilder()
        for (i in 0..elements.size) {
            if (isThisActive && i == cursorIndex) {
                sb.append("{\\color{#2563EB}\\vert}")
            }
            if (i < elements.size) {
                sb.append(elements[i].toLatex(activeSlotId, cursorIndex))
            }
        }
        return sb.toString()
    }

    fun toCalculusString(): String {
        if (elements.isEmpty()) return ""
        return elements.joinToString("") { it.toCalculusString() }
    }

    fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        val isThisActive = (id == activeSlotId)
        if (elements.isEmpty()) {
            return if (isThisActive) "|" else placeholder
        }

        val sb = StringBuilder()
        for (i in 0..elements.size) {
            if (isThisActive && i == cursorIndex) {
                sb.append("|")
            }
            if (i < elements.size) {
                sb.append(elements[i].toPlainDisplay(activeSlotId, cursorIndex))
            }
        }
        return sb.toString()
    }

    fun deepCopy(): MathSlot {
        val newSlot = MathSlot(id = UUID.randomUUID().toString(), placeholder = placeholder)
        for (el in elements) {
            when (el) {
                is CharElement -> newSlot.elements.add(el.copy())
                is FunctionTemplate -> newSlot.elements.add(
                    FunctionTemplate(el.name, el.power, el.argSlot.deepCopy())
                )
                is FractionTemplate -> newSlot.elements.add(
                    FractionTemplate(el.numSlot.deepCopy(), el.denSlot.deepCopy())
                )
                is SqrtTemplate -> newSlot.elements.add(
                    SqrtTemplate(el.radicandSlot.deepCopy())
                )
                is NthRootTemplate -> newSlot.elements.add(
                    NthRootTemplate(el.indexSlot.deepCopy(), el.radicandSlot.deepCopy())
                )
                is PowerTemplate -> newSlot.elements.add(
                    PowerTemplate(el.expSlot.deepCopy())
                )
                is SubscriptTemplate -> newSlot.elements.add(
                    SubscriptTemplate(el.subSlot.deepCopy())
                )
                is AbsTemplate -> newSlot.elements.add(
                    AbsTemplate(el.slot.deepCopy())
                )
            }
        }
        return newSlot
    }
}

/**
 * Function template, e.g. sin(slot), cos(slot), sin^{-1}(slot).
 */
data class FunctionTemplate(
    val name: String,
    val power: String? = null,
    val argSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val funcPrefix = when {
            power == "-1" -> "\\${name}^{-1}"
            power != null -> "\\${name}^{$power}"
            name in listOf("sin", "cos", "tan", "cot", "sec", "csc", "sinh", "cosh", "tanh", "coth", "sech", "csch", "ln", "exp") -> "\\$name"
            name == "cosec" -> "\\operatorname{cosec}"
            name in listOf("log", "log10") -> "\\log"
            else -> "\\text{$name}"
        }
        val innerLatex = argSlot.toLatex(activeSlotId, cursorIndex)
        return "$funcPrefix\\left($innerLatex\\right)"
    }

    override fun toCalculusString(): String {
        val innerCalc = argSlot.toCalculusString().ifEmpty { "x" }
        return when {
            power == "-1" -> {
                when (name) {
                    "sin" -> "asin($innerCalc)"
                    "cos" -> "acos($innerCalc)"
                    "tan" -> "atan($innerCalc)"
                    "cot" -> "acot($innerCalc)"
                    "sec" -> "asec($innerCalc)"
                    "csc", "cosec" -> "acsc($innerCalc)"
                    else -> "asin($innerCalc)"
                }
            }
            power != null -> "($name($innerCalc))^$power"
            else -> "$name($innerCalc)"
        }
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        val dispName = when {
            power == "-1" -> "${name}⁻¹"
            power != null -> "$name^$power"
            else -> name
        }
        return "$dispName(${argSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(argSlot)
}

/**
 * Vertical fraction template: \frac{numSlot}{denSlot}.
 */
data class FractionTemplate(
    val numSlot: MathSlot = MathSlot(),
    val denSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val numLatex = numSlot.toLatex(activeSlotId, cursorIndex)
        val denLatex = denSlot.toLatex(activeSlotId, cursorIndex)
        return "\\frac{$numLatex}{$denLatex}"
    }

    override fun toCalculusString(): String {
        val numStr = numSlot.toCalculusString().ifEmpty { "1" }
        val denStr = denSlot.toCalculusString().ifEmpty { "1" }
        return "(($numStr)/($denStr))"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "(${numSlot.toPlainDisplay(activeSlotId, cursorIndex)})/(${denSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(numSlot, denSlot)
}

/**
 * Square root template: \sqrt{radicandSlot}.
 */
data class SqrtTemplate(
    val radicandSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val inner = radicandSlot.toLatex(activeSlotId, cursorIndex)
        return "\\sqrt{$inner}"
    }

    override fun toCalculusString(): String {
        val inner = radicandSlot.toCalculusString().ifEmpty { "0" }
        return "sqrt($inner)"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "√(${radicandSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(radicandSlot)
}

/**
 * N-th root template: \sqrt[indexSlot]{radicandSlot}.
 */
data class NthRootTemplate(
    val indexSlot: MathSlot = MathSlot(),
    val radicandSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val idx = indexSlot.toLatex(activeSlotId, cursorIndex)
        val rad = radicandSlot.toLatex(activeSlotId, cursorIndex)
        return "\\sqrt[$idx]{$rad}"
    }

    override fun toCalculusString(): String {
        val idx = indexSlot.toCalculusString().ifEmpty { "2" }
        val rad = radicandSlot.toCalculusString().ifEmpty { "0" }
        return "(($rad)^(1/($idx)))"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "root[${indexSlot.toPlainDisplay(activeSlotId, cursorIndex)}](${radicandSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(indexSlot, radicandSlot)
}

/**
 * Exponent / Superscript template: ^{expSlot}.
 */
data class PowerTemplate(
    val expSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val inner = expSlot.toLatex(activeSlotId, cursorIndex)
        return "^{$inner}"
    }

    override fun toCalculusString(): String {
        val inner = expSlot.toCalculusString().ifEmpty { "1" }
        return "^($inner)"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "^(${expSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(expSlot)
}

/**
 * Subscript template: _{subSlot}.
 */
data class SubscriptTemplate(
    val subSlot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val inner = subSlot.toLatex(activeSlotId, cursorIndex)
        return "_{$inner}"
    }

    override fun toCalculusString(): String {
        val inner = subSlot.toCalculusString().ifEmpty { "0" }
        return "_$inner"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "_(${subSlot.toPlainDisplay(activeSlotId, cursorIndex)})"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(subSlot)
}

/**
 * Absolute value template: \left| slot \right|.
 */
data class AbsTemplate(
    val slot: MathSlot = MathSlot()
) : MathElement {
    override fun toLatex(activeSlotId: String?, cursorIndex: Int): String {
        val inner = slot.toLatex(activeSlotId, cursorIndex)
        return "\\left|$inner\\right|"
    }

    override fun toCalculusString(): String {
        val inner = slot.toCalculusString().ifEmpty { "0" }
        return "abs($inner)"
    }

    override fun toPlainDisplay(activeSlotId: String?, cursorIndex: Int): String {
        return "|${slot.toPlainDisplay(activeSlotId, cursorIndex)}|"
    }

    override fun getChildSlots(): List<MathSlot> = listOf(slot)
}

/**
 * Interactive Structured Math Input Model with real independent slots and cursor management.
 */
class MathInputModel(
    var rootSlot: MathSlot = MathSlot(id = "root")
) {
    var cursorSlotId: String = rootSlot.id
    var cursorIndex: Int = 0

    companion object {
        private val KNOWN_FUNCS = listOf(
            "arcsin" to Pair("sin", "-1"),
            "arccos" to Pair("cos", "-1"),
            "arctan" to Pair("tan", "-1"),
            "arccot" to Pair("cot", "-1"),
            "arcsec" to Pair("sec", "-1"),
            "arccsc" to Pair("csc", "-1"),
            "arccosec" to Pair("cosec", "-1"),
            "asin" to Pair("sin", "-1"),
            "acos" to Pair("cos", "-1"),
            "atan" to Pair("tan", "-1"),
            "acot" to Pair("cot", "-1"),
            "asec" to Pair("sec", "-1"),
            "acsc" to Pair("csc", "-1"),
            "sinh" to Pair("sinh", null),
            "cosh" to Pair("cosh", null),
            "tanh" to Pair("tanh", null),
            "coth" to Pair("coth", null),
            "sech" to Pair("sech", null),
            "csch" to Pair("csch", null),
            "cosec" to Pair("cosec", null),
            "sin" to Pair("sin", null),
            "cos" to Pair("cos", null),
            "tan" to Pair("tan", null),
            "cot" to Pair("cot", null),
            "sec" to Pair("sec", null),
            "csc" to Pair("csc", null),
            "sqrt" to Pair("sqrt", null),
            "abs" to Pair("abs", null),
            "log" to Pair("log", null),
            "ln" to Pair("ln", null),
            "exp" to Pair("exp", null)
        )

        /**
         * Parses a flat string like "x^2 + y^2 = 5" or "sin(x)" into the structured MathInputModel.
         */
        fun parseFromFlatString(input: String): MathInputModel {
            val model = MathInputModel()
            if (input.isBlank()) return model

            var i = 0
            val s = input.trim()
            while (i < s.length) {
                val ch = s[i]
                if (ch == '^') {
                    i++
                    if (i < s.length && s[i] == '{') {
                        i++
                        val start = i
                        while (i < s.length && s[i] != '}') i++
                        val expText = s.substring(start, i)
                        if (i < s.length && s[i] == '}') i++
                        val pTemp = PowerTemplate()
                        for (c in expText) pTemp.expSlot.elements.add(CharElement(c))
                        model.getCurrentSlot()?.elements?.add(pTemp)
                    } else if (i < s.length) {
                        val pTemp = PowerTemplate()
                        pTemp.expSlot.elements.add(CharElement(s[i]))
                        model.getCurrentSlot()?.elements?.add(pTemp)
                        i++
                    }
                } else if (ch == '/') {
                    i++
                    val frac = FractionTemplate()
                    val curElements = model.getCurrentSlot()?.elements ?: mutableListOf()
                    if (curElements.isNotEmpty()) {
                        val lastEl = curElements.removeAt(curElements.size - 1)
                        frac.numSlot.elements.add(lastEl)
                    }
                    curElements.add(frac)
                    model.cursorSlotId = frac.denSlot.id
                    model.cursorIndex = 0
                } else {
                    model.insertChar(ch)
                    i++
                }
            }
            model.cursorSlotId = model.rootSlot.id
            model.cursorIndex = model.rootSlot.elements.size
            return model
        }
    }

    fun findSlotById(slot: MathSlot = rootSlot, targetId: String): MathSlot? {
        if (slot.id == targetId) return slot
        for (el in slot.elements) {
            for (child in el.getChildSlots()) {
                val found = findSlotById(child, targetId)
                if (found != null) return found
            }
        }
        return null
    }

    fun findParentSlotAndIndex(slot: MathSlot = rootSlot, targetId: String): Pair<MathSlot, Int>? {
        for (idx in slot.elements.indices) {
            val el = slot.elements[idx]
            for (child in el.getChildSlots()) {
                if (child.id == targetId) {
                    return Pair(slot, idx)
                }
                val found = findParentSlotAndIndex(child, targetId)
                if (found != null) return found
            }
        }
        return null
    }

    fun getCurrentSlot(): MathSlot? {
        val slot = findSlotById(rootSlot, cursorSlotId)
        if (slot == null) {
            cursorSlotId = rootSlot.id
            cursorIndex = rootSlot.elements.size
            return rootSlot
        }
        return slot
    }

    /**
     * Inserts a single character with real-time automatic recognition of typed function names!
     */
    fun insertChar(ch: Char) {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)
        slot.elements.add(clampedCursor, CharElement(ch))
        cursorIndex = clampedCursor + 1

        // Check if trailing typed characters match any known function name (e.g. "sin", "cos", "arcsin")
        checkAndTransformAutoFunction(slot)
    }

    /**
     * Checks if trailing characters form a recognized function name and converts to structured template.
     */
    private fun checkAndTransformAutoFunction(slot: MathSlot) {
        val elList = slot.elements
        val curPos = cursorIndex.coerceIn(0, elList.size)

        // Build trailing text of simple CharElements ending at cursor
        val sb = StringBuilder()
        var checkIndex = curPos - 1
        while (checkIndex >= 0 && elList[checkIndex] is CharElement) {
            val c = (elList[checkIndex] as CharElement).char
            if (c.isLetter()) {
                sb.insert(0, c)
                checkIndex--
            } else {
                break
            }
        }

        val word = sb.toString().lowercase()
        for ((kw, pair) in KNOWN_FUNCS) {
            if (word.endsWith(kw)) {
                val (fnName, power) = pair
                val kwLen = kw.length
                val removeStart = curPos - kwLen
                if (removeStart >= 0) {
                    // Remove the raw typed letters
                    repeat(kwLen) {
                        slot.elements.removeAt(removeStart)
                    }
                    // Insert structured FunctionTemplate with real empty slot
                    val tmpl = when (fnName) {
                        "sqrt" -> SqrtTemplate()
                        "abs" -> AbsTemplate()
                        else -> FunctionTemplate(name = fnName, power = power)
                    }
                    slot.elements.add(removeStart, tmpl)

                    // Place cursor immediately inside the empty argument slot!
                    val childSlot = tmpl.getChildSlots().first()
                    cursorSlotId = childSlot.id
                    cursorIndex = 0
                    return
                }
            }
        }
    }

    /**
     * Inserts a function template directly (e.g. from custom keypad).
     */
    fun insertFunction(name: String, power: String? = null) {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val tmpl = when (name.lowercase()) {
            "sqrt" -> SqrtTemplate()
            "abs" -> AbsTemplate()
            else -> FunctionTemplate(name = name, power = power)
        }

        slot.elements.add(clampedCursor, tmpl)
        val childSlot = tmpl.getChildSlots().first()
        cursorSlotId = childSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts vertical fraction template.
     */
    fun insertFraction() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val frac = FractionTemplate()
        slot.elements.add(clampedCursor, frac)
        cursorSlotId = frac.numSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts square root template.
     */
    fun insertSqrt() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val sq = SqrtTemplate()
        slot.elements.add(clampedCursor, sq)
        cursorSlotId = sq.radicandSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts N-th root template.
     */
    fun insertNthRoot() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val nth = NthRootTemplate()
        slot.elements.add(clampedCursor, nth)
        cursorSlotId = nth.indexSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts exponent power template.
     */
    fun insertPower() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val pow = PowerTemplate()
        slot.elements.add(clampedCursor, pow)
        cursorSlotId = pow.expSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts subscript template.
     */
    fun insertSubscript() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val sub = SubscriptTemplate()
        slot.elements.add(clampedCursor, sub)
        cursorSlotId = sub.subSlot.id
        cursorIndex = 0
    }

    /**
     * Inserts absolute value template.
     */
    fun insertAbs() {
        val slot = getCurrentSlot() ?: return
        val clampedCursor = cursorIndex.coerceIn(0, slot.elements.size)

        val absT = AbsTemplate()
        slot.elements.add(clampedCursor, absT)
        cursorSlotId = absT.slot.id
        cursorIndex = 0
    }

    /**
     * Slot-aware, character-level backspace handler matching MathQuill / KaTeX behavior!
     */
    fun backspace() {
        val slot = getCurrentSlot() ?: return

        // 1. If cursor is at index 0 of an empty child slot inside a template:
        if (slot.id != rootSlot.id && slot.elements.isEmpty() && cursorIndex == 0) {
            val parentInfo = findParentSlotAndIndex(rootSlot, slot.id)
            if (parentInfo != null) {
                val (parentSlot, templateIndex) = parentInfo
                // UNWRAP / DELETE ENTIRE TEMPLATE AT ONCE!
                parentSlot.elements.removeAt(templateIndex)
                cursorSlotId = parentSlot.id
                cursorIndex = templateIndex
                return
            }
        }

        // 2. If cursor is at index > 0:
        if (cursorIndex > 0 && cursorIndex <= slot.elements.size) {
            val prevElement = slot.elements[cursorIndex - 1]
            if (prevElement.getChildSlots().isNotEmpty()) {
                // If previous element is a template, step cursor into its last child slot at the end!
                val lastChild = prevElement.getChildSlots().last()
                cursorSlotId = lastChild.id
                cursorIndex = lastChild.elements.size
            } else {
                // Normal character-level deletion
                slot.elements.removeAt(cursorIndex - 1)
                cursorIndex--
            }
            return
        }

        // 3. If cursor is at index 0 of a multi-slot template (e.g. denominator):
        if (slot.id != rootSlot.id && cursorIndex == 0) {
            val parentInfo = findParentSlotAndIndex(rootSlot, slot.id)
            if (parentInfo != null) {
                val (parentSlot, templateIndex) = parentInfo
                val tmpl = parentSlot.elements[templateIndex]
                val childSlots = tmpl.getChildSlots()
                val currentSlotIdx = childSlots.indexOfFirst { it.id == slot.id }
                if (currentSlotIdx > 0) {
                    // Step into the previous slot of this template (e.g. numerator)
                    val prevSlot = childSlots[currentSlotIdx - 1]
                    cursorSlotId = prevSlot.id
                    cursorIndex = prevSlot.elements.size
                    return
                } else {
                    // Step out to the left of the template in parent slot
                    cursorSlotId = parentSlot.id
                    cursorIndex = templateIndex
                    return
                }
            }
        }
    }

    /**
     * Move cursor one position to the left, stepping in/out of slots smoothly.
     */
    fun moveCursorLeft() {
        val slot = getCurrentSlot() ?: return
        if (cursorIndex > 0) {
            val prevEl = slot.elements[cursorIndex - 1]
            if (prevEl.getChildSlots().isNotEmpty()) {
                val lastChild = prevEl.getChildSlots().last()
                cursorSlotId = lastChild.id
                cursorIndex = lastChild.elements.size
            } else {
                cursorIndex--
            }
        } else if (slot.id != rootSlot.id) {
            val parentInfo = findParentSlotAndIndex(rootSlot, slot.id)
            if (parentInfo != null) {
                val (parentSlot, templateIndex) = parentInfo
                val tmpl = parentSlot.elements[templateIndex]
                val childSlots = tmpl.getChildSlots()
                val currentSlotIdx = childSlots.indexOfFirst { it.id == slot.id }
                if (currentSlotIdx > 0) {
                    val prevSlot = childSlots[currentSlotIdx - 1]
                    cursorSlotId = prevSlot.id
                    cursorIndex = prevSlot.elements.size
                } else {
                    cursorSlotId = parentSlot.id
                    cursorIndex = templateIndex
                }
            }
        }
    }

    /**
     * Move cursor one position to the right, stepping in/out of slots smoothly.
     */
    fun moveCursorRight() {
        val slot = getCurrentSlot() ?: return
        if (cursorIndex < slot.elements.size) {
            val nextEl = slot.elements[cursorIndex]
            if (nextEl.getChildSlots().isNotEmpty()) {
                val firstChild = nextEl.getChildSlots().first()
                cursorSlotId = firstChild.id
                cursorIndex = 0
            } else {
                cursorIndex++
            }
        } else if (slot.id != rootSlot.id) {
            val parentInfo = findParentSlotAndIndex(rootSlot, slot.id)
            if (parentInfo != null) {
                val (parentSlot, templateIndex) = parentInfo
                val tmpl = parentSlot.elements[templateIndex]
                val childSlots = tmpl.getChildSlots()
                val currentSlotIdx = childSlots.indexOfFirst { it.id == slot.id }
                if (currentSlotIdx >= 0 && currentSlotIdx < childSlots.size - 1) {
                    val nextSlot = childSlots[currentSlotIdx + 1]
                    cursorSlotId = nextSlot.id
                    cursorIndex = 0
                } else {
                    cursorSlotId = parentSlot.id
                    cursorIndex = templateIndex + 1
                }
            }
        }
    }

    /**
     * Tab / Next slot navigation: jumps to next slot across the entire tree.
     */
    fun nextSlot() {
        val allSlots = getAllSlots()
        val curIdx = allSlots.indexOfFirst { it.id == cursorSlotId }
        if (curIdx >= 0 && curIdx < allSlots.size - 1) {
            val target = allSlots[curIdx + 1]
            cursorSlotId = target.id
            cursorIndex = 0
        } else {
            cursorSlotId = rootSlot.id
            cursorIndex = rootSlot.elements.size
        }
    }

    private fun getAllSlots(slot: MathSlot = rootSlot): List<MathSlot> {
        val list = mutableListOf(slot)
        for (el in slot.elements) {
            for (child in el.getChildSlots()) {
                list.addAll(getAllSlots(child))
            }
        }
        return list
    }

    /**
     * Returns true if root slot has no elements.
     */
    fun isEmpty(): Boolean = rootSlot.elements.isEmpty()

    /**
     * Clears all elements and resets cursor to root at 0.
     */
    fun clear() {
        rootSlot = MathSlot(id = "root")
        cursorSlotId = rootSlot.id
        cursorIndex = 0
    }

    /**
     * Serializes the AST into pristine, 100% valid KaTeX LaTeX with zero missing braces or dangling commands.
     */
    fun toLatex(showCursor: Boolean = true): String {
        val activeId = if (showCursor) cursorSlotId else null
        val cIdx = if (showCursor) cursorIndex else -1
        return rootSlot.toLatex(activeId, cIdx)
    }

    /**
     * Serializes into mathematical expression string for calculus / graphing engine.
     */
    fun toCalculusExprString(): String {
        return rootSlot.toCalculusString()
    }

    /**
     * Serializes into plain readable string.
     */
    fun toPlainDisplay(showCursor: Boolean = true): String {
        val activeId = if (showCursor) cursorSlotId else null
        val cIdx = if (showCursor) cursorIndex else -1
        return rootSlot.toPlainDisplay(activeId, cIdx)
    }

    fun deepCopy(): MathInputModel {
        val newModel = MathInputModel(rootSlot.deepCopy())
        newModel.cursorSlotId = newModel.rootSlot.id
        newModel.cursorIndex = newModel.rootSlot.elements.size
        return newModel
    }
}
