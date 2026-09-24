package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.engine.subject.SubjectType
import com.example.engine.subject.mathematics.MathematicsCalculators
import com.example.ui.screens.subject.SubjectCalculatorScreen

@Composable
fun MathematicsScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    SubjectCalculatorScreen(
        subject = SubjectType.MATHEMATICS,
        calculators = MathematicsCalculators.calculators,
        onBack = onBack,
        onSaveToNotebook = onSaveToNotebook,
        modifier = modifier
    )
}
