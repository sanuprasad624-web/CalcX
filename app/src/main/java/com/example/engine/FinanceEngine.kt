package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

data class EmiResult(
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayment: Double
)

data class CompoundInterestResult(
    val principal: Double,
    val totalInterest: Double,
    val finalAmount: Double
)

object FinanceEngine {

    fun calculateSimpleInterest(principal: Double, ratePercent: Double, timeYears: Double): Double {
        val p = BigDecimal.valueOf(principal)
        val r = BigDecimal.valueOf(ratePercent)
        val t = BigDecimal.valueOf(timeYears)
        return p.multiply(r).multiply(t).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toDouble()
    }

    fun calculateCompoundInterest(
        principal: Double,
        ratePercent: Double,
        timeYears: Double,
        compoundingPerYear: Int = 12
    ): CompoundInterestResult {
        val r = (ratePercent / 100.0) / compoundingPerYear
        val n = compoundingPerYear * timeYears
        val finalAmount = principal * (1.0 + r).pow(n)
        val interest = finalAmount - principal
        return CompoundInterestResult(
            principal = principal,
            totalInterest = roundMoney(interest),
            finalAmount = roundMoney(finalAmount)
        )
    }

    fun calculateLoanEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
        if (principal <= 0 || tenureMonths <= 0) return EmiResult(0.0, 0.0, 0.0)
        if (annualRatePercent == 0.0) {
            val emi = principal / tenureMonths
            return EmiResult(roundMoney(emi), 0.0, roundMoney(principal))
        }

        val monthlyRate = (annualRatePercent / 100.0) / 12.0
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val monthlyEmi = principal * monthlyRate * factor / (factor - 1.0)
        val totalPayment = monthlyEmi * tenureMonths
        val totalInterest = totalPayment - principal

        return EmiResult(
            monthlyEmi = roundMoney(monthlyEmi),
            totalInterest = roundMoney(totalInterest),
            totalPayment = roundMoney(totalPayment)
        )
    }

    fun calculateGst(amount: Double, ratePercent: Double, isInclusive: Boolean): Pair<Double, Double> {
        // Returns Pair(GstAmount, TotalAmount)
        return if (isInclusive) {
            val base = amount / (1.0 + ratePercent / 100.0)
            val gst = amount - base
            Pair(roundMoney(gst), roundMoney(amount))
        } else {
            val gst = amount * (ratePercent / 100.0)
            val total = amount + gst
            Pair(roundMoney(gst), roundMoney(total))
        }
    }

    fun calculateTipSplit(billAmount: Double, tipPercent: Double, numPeople: Int): Map<String, Double> {
        val count = if (numPeople < 1) 1 else numPeople
        val tip = billAmount * (tipPercent / 100.0)
        val total = billAmount + tip
        val perPerson = total / count

        return mapOf(
            "tipAmount" to roundMoney(tip),
            "totalBill" to roundMoney(total),
            "perPerson" to roundMoney(perPerson)
        )
    }

    fun calculateInflation(currentAmount: Double, inflationRatePercent: Double, years: Double): Double {
        val fv = currentAmount * (1.0 + inflationRatePercent / 100.0).pow(years)
        return roundMoney(fv)
    }

    private fun roundMoney(value: Double): Double {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
