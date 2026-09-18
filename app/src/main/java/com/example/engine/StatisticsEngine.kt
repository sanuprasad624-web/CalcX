package com.example.engine

import kotlin.math.pow
import kotlin.math.sqrt

data class StatisticsSummary(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double,
    val sampleVariance: Double,
    val populationVariance: Double,
    val sampleStdDev: Double,
    val populationStdDev: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double,
    val sortedData: List<Double>
)

data class RegressionResult(
    val slope: Double,
    val intercept: Double,
    val r: Double,
    val rSquared: Double,
    val equation: String
)

object StatisticsEngine {

    fun compute(dataset: List<Double>): StatisticsSummary? {
        if (dataset.isEmpty()) return null
        val sorted = dataset.sorted()
        val n = sorted.size
        val sum = sorted.sum()
        val mean = sum / n

        val median = if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }

        // Mode calculation
        val freqMap = mutableMapOf<Double, Int>()
        for (num in sorted) freqMap[num] = (freqMap[num] ?: 0) + 1
        val maxFreq = freqMap.values.maxOrNull() ?: 1
        val mode = if (maxFreq > 1) {
            freqMap.filter { it.value == maxFreq }.keys.sorted()
        } else {
            emptyList()
        }

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        val varianceSum = sorted.sumOf { (it - mean).pow(2) }
        val populationVariance = varianceSum / n
        val sampleVariance = if (n > 1) varianceSum / (n - 1) else 0.0
        val populationStdDev = sqrt(populationVariance)
        val sampleStdDev = sqrt(sampleVariance)

        val q1 = percentile(sorted, 0.25)
        val q3 = percentile(sorted, 0.75)
        val iqr = q3 - q1

        return StatisticsSummary(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            mode = mode,
            min = min,
            max = max,
            range = range,
            sampleVariance = sampleVariance,
            populationVariance = populationVariance,
            sampleStdDev = sampleStdDev,
            populationStdDev = populationStdDev,
            q1 = q1,
            q3 = q3,
            iqr = iqr,
            sortedData = sorted
        )
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        val n = sorted.size
        if (n == 1) return sorted[0]
        val rank = p * (n - 1)
        val low = rank.toInt()
        val high = if (low + 1 < n) low + 1 else low
        val weight = rank - low
        return sorted[low] * (1 - weight) + sorted[high] * weight
    }

    fun computeLinearRegression(xs: List<Double>, ys: List<Double>): RegressionResult? {
        val n = minOf(xs.size, ys.size)
        if (n < 2) return null
        val subX = xs.take(n)
        val subY = ys.take(n)

        val meanX = subX.average()
        val meanY = subY.average()

        var num = 0.0
        var denX = 0.0
        var denY = 0.0

        for (i in 0 until n) {
            val dx = subX[i] - meanX
            val dy = subY[i] - meanY
            num += dx * dy
            denX += dx * dx
            denY += dy * dy
        }

        if (denX == 0.0) return null
        val slope = num / denX
        val intercept = meanY - slope * meanX
        val r = if (denX * denY > 0) num / sqrt(denX * denY) else 0.0
        val rSquared = r * r

        val sign = if (intercept >= 0) "+ " else "- "
        val eq = "y = ${"%.4f".format(slope)}x $sign${"%.4f".format(kotlin.math.abs(intercept))}"
        return RegressionResult(slope, intercept, r, rSquared, eq)
    }
}
