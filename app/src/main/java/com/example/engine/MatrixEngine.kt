package com.example.engine

import kotlin.math.abs

data class Matrix(
    val rows: Int,
    val cols: Int,
    val data: Array<DoubleArray>
) {
    init {
        require(rows > 0 && cols > 0) { "Matrix dimensions must be positive" }
        require(data.size == rows) { "Invalid row count in matrix data" }
        data.forEach { require(it.size == cols) { "Invalid column count in matrix row" } }
    }

    val isSquare: Boolean get() = rows == cols

    operator fun plus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrices must have identical dimensions for addition" }
        val result = Array(rows) { r ->
            DoubleArray(cols) { c -> this.data[r][c] + other.data[r][c] }
        }
        return Matrix(rows, cols, result)
    }

    operator fun minus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrices must have identical dimensions for subtraction" }
        val result = Array(rows) { r ->
            DoubleArray(cols) { c -> this.data[r][c] - other.data[r][c] }
        }
        return Matrix(rows, cols, result)
    }

    operator fun times(scalar: Double): Matrix {
        val result = Array(rows) { r ->
            DoubleArray(cols) { c -> this.data[r][c] * scalar }
        }
        return Matrix(rows, cols, result)
    }

    operator fun times(other: Matrix): Matrix {
        require(cols == other.rows) { "Matrix A columns ($cols) must match Matrix B rows (${other.rows})" }
        val result = Array(rows) { r ->
            DoubleArray(other.cols) { c ->
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += this.data[r][k] * other.data[k][c]
                }
                sum
            }
        }
        return Matrix(rows, other.cols, result)
    }

    fun transpose(): Matrix {
        val result = Array(cols) { c ->
            DoubleArray(rows) { r -> this.data[r][c] }
        }
        return Matrix(cols, rows, result)
    }

    fun trace(): Double {
        require(isSquare) { "Trace is defined only for square matrices" }
        var sum = 0.0
        for (i in 0 until rows) sum += data[i][i]
        return sum
    }

    fun determinant(): Double {
        require(isSquare) { "Determinant is defined only for square matrices" }
        val n = rows
        if (n == 1) return data[0][0]
        if (n == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0]
        if (n == 3) {
            return data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1]) -
                    data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0]) +
                    data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0])
        }

        // Gaussian elimination with partial pivoting
        val a = Array(n) { r -> data[r].clone() }
        var det = 1.0
        for (i in 0 until n) {
            var pivot = i
            for (j in i + 1 until n) {
                if (abs(a[j][i]) > abs(a[pivot][i])) pivot = j
            }
            if (abs(a[pivot][i]) < 1e-12) return 0.0
            if (pivot != i) {
                val temp = a[i]
                a[i] = a[pivot]
                a[pivot] = temp
                det = -det
            }
            det *= a[i][i]
            for (j in i + 1 until n) {
                val factor = a[j][i] / a[i][i]
                for (k in i until n) {
                    a[j][k] -= factor * a[i][k]
                }
            }
        }
        return det
    }

    fun inverse(): Matrix {
        require(isSquare) { "Only square matrices have an inverse" }
        val n = rows
        val det = determinant()
        if (abs(det) < 1e-12) {
            throw ArithmeticException("Matrix is singular (determinant = 0), inverse does not exist")
        }

        val augmented = Array(n) { r ->
            DoubleArray(2 * n) { c ->
                if (c < n) data[r][c] else if (c - n == r) 1.0 else 0.0
            }
        }

        for (i in 0 until n) {
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(augmented[k][i]) > abs(augmented[maxRow][i])) maxRow = k
            }
            val temp = augmented[i]
            augmented[i] = augmented[maxRow]
            augmented[maxRow] = temp

            val pivot = augmented[i][i]
            if (abs(pivot) < 1e-12) throw ArithmeticException("Matrix is singular")

            for (j in 0 until 2 * n) augmented[i][j] /= pivot
            for (k in 0 until n) {
                if (k != i) {
                    val factor = augmented[k][i]
                    for (j in 0 until 2 * n) {
                        augmented[k][j] -= factor * augmented[i][j]
                    }
                }
            }
        }

        val invData = Array(n) { r ->
            DoubleArray(n) { c -> augmented[r][c + n] }
        }
        return Matrix(n, n, invData)
    }

    fun rank(): Int {
        val rCount = rows
        val cCount = cols
        val a = Array(rCount) { r -> data[r].clone() }
        var rank = 0
        val rowSelected = BooleanArray(rCount)

        for (c in 0 until cCount) {
            var pivot = -1
            for (r in 0 until rCount) {
                if (!rowSelected[r] && abs(a[r][c]) > 1e-10) {
                    if (pivot == -1 || abs(a[r][c]) > abs(a[pivot][c])) {
                        pivot = r
                    }
                }
            }
            if (pivot != -1) {
                rank++
                rowSelected[pivot] = true
                for (r in 0 until rCount) {
                    if (r != pivot && abs(a[r][c]) > 1e-10) {
                        val factor = a[r][c] / a[pivot][c]
                        for (k in c until cCount) {
                            a[r][k] -= factor * a[pivot][k]
                        }
                    }
                }
            }
        }
        return rank
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        if (rows != other.rows || cols != other.cols) return false
        for (r in 0 until rows) {
            if (!data[r].contentEquals(other.data[r])) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + data.contentDeepHashCode()
        return result
    }

    companion object {
        fun identity(size: Int): Matrix {
            val d = Array(size) { r ->
                DoubleArray(size) { c -> if (r == c) 1.0 else 0.0 }
            }
            return Matrix(size, size, d)
        }

        fun zeros(rows: Int, cols: Int): Matrix {
            val d = Array(rows) { DoubleArray(cols) { 0.0 } }
            return Matrix(rows, cols, d)
        }
    }
}
