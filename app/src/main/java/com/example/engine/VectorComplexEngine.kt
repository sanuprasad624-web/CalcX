package com.example.engine

import kotlin.math.*

data class ComplexNumber(
    val real: Double,
    val imag: Double
) {
    val modulus: Double get() = sqrt(real * real + imag * imag)
    val argumentRad: Double get() = atan2(imag, real)
    val argumentDeg: Double get() = Math.toDegrees(argumentRad)

    operator fun plus(other: ComplexNumber) = ComplexNumber(real + other.real, imag + other.imag)
    operator fun minus(other: ComplexNumber) = ComplexNumber(real - other.real, imag - other.imag)
    operator fun times(other: ComplexNumber) = ComplexNumber(
        real * other.real - imag * other.imag,
        real * other.imag + imag * other.real
    )
    operator fun div(other: ComplexNumber): ComplexNumber {
        val denom = other.real * other.real + other.imag * other.imag
        require(denom != 0.0) { "Division by zero complex number" }
        return ComplexNumber(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        )
    }

    fun conjugate() = ComplexNumber(real, -imag)

    fun pow(n: Int): ComplexNumber {
        val r = modulus.pow(n)
        val theta = argumentRad * n
        return ComplexNumber(r * cos(theta), r * sin(theta))
    }

    fun toCartesianString(): String {
        val sign = if (imag >= 0) "+" else "-"
        return "${"%.4f".format(real)} $sign ${"%.4f".format(abs(imag))}i"
    }

    fun toPolarString(): String {
        return "${"%.4f".format(modulus)} ∠ ${"%.2f".format(argumentDeg)}°"
    }

    companion object {
        fun fromPolar(r: Double, thetaDeg: Double): ComplexNumber {
            val rad = Math.toRadians(thetaDeg)
            return ComplexNumber(r * cos(rad), r * sin(rad))
        }
    }
}

data class Vector3D(
    val x: Double,
    val y: Double,
    val z: Double = 0.0
) {
    val magnitude: Double get() = sqrt(x * x + y * y + z * z)

    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)

    infix fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

    infix fun cross(other: Vector3D): Vector3D = Vector3D(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun unitVector(): Vector3D {
        val mag = magnitude
        require(mag > 0.0) { "Zero vector has no unit vector" }
        return Vector3D(x / mag, y / mag, z / mag)
    }

    fun angleWith(other: Vector3D): Double {
        val magMul = magnitude * other.magnitude
        require(magMul > 0.0) { "Cannot compute angle with zero vector" }
        val cosTheta = (this dot other) / magMul
        val clamped = cosTheta.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    fun projectionOn(other: Vector3D): Vector3D {
        val otherMagSq = other.x * other.x + other.y * other.y + other.z * other.z
        require(otherMagSq > 0.0) { "Cannot project onto zero vector" }
        val scalar = (this dot other) / otherMagSq
        return other * scalar
    }

    fun toFormattedString(): String {
        return "(${ "%.3f".format(x) }î + ${ "%.3f".format(y) }ĵ + ${ "%.3f".format(z) }k̂)"
    }
}
