package com.example.engine

enum class BitWordSize(val bits: Int, val mask: Long) {
    BYTE(8, 0xFFL),
    WORD(16, 0xFFFFL),
    DWORD(32, 0xFFFFFFFFL),
    QWORD(64, -1L)
}

object ProgrammerEngine {

    fun formatAllBases(value: Long, wordSize: BitWordSize): Map<String, String> {
        val masked = value and wordSize.mask
        val hex = java.lang.Long.toHexString(masked).uppercase()
        val dec = if (wordSize == BitWordSize.QWORD) {
            value.toString()
        } else {
            // Check sign bit
            val signBit = 1L shl (wordSize.bits - 1)
            if ((masked and signBit) != 0L) {
                // Negative in two's complement
                val signExtended = masked or (wordSize.mask.inv())
                signExtended.toString()
            } else {
                masked.toString()
            }
        }
        val oct = java.lang.Long.toOctalString(masked)
        val rawBin = java.lang.Long.toBinaryString(masked).padStart(wordSize.bits, '0')
        val formattedBin = rawBin.chunked(4).joinToString(" ")

        return mapOf(
            "HEX" to hex,
            "DEC" to dec,
            "OCT" to oct,
            "BIN" to formattedBin
        )
    }

    fun parseValue(text: String, base: Int, wordSize: BitWordSize): Long {
        val clean = text.replace(" ", "").trim()
        if (clean.isEmpty()) return 0L
        val raw = try {
            java.lang.Long.parseUnsignedLong(clean, base)
        } catch (_: Exception) {
            0L
        }
        return raw and wordSize.mask
    }
}
