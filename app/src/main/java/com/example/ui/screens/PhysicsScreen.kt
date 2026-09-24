package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.engine.subject.SubjectType
import com.example.engine.subject.physics.PhysicsCalculators
import com.example.ui.screens.subject.SubjectCalculatorScreen

@Composable
fun PhysicsScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    SubjectCalculatorScreen(
        subject = SubjectType.PHYSICS,
        calculators = PhysicsCalculators.calculators,
        onBack = onBack,
        onSaveToNotebook = onSaveToNotebook,
        modifier = modifier
    )
}
