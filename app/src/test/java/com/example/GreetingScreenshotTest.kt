package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.math.AngleMode
import com.example.ui.components.DisplaySection
import com.example.ui.theme.CalcxTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun calcx_display_screenshot() {
    composeTestRule.setContent {
      CalcxTheme {
        DisplaySection(
          expression = "sin(30) + sqrt(16) * 2^3",
          resultPreview = "32.5",
          exactResult = "65/2",
          isExactMode = true,
          angleMode = AngleMode.DEG,
          errorMessage = null,
          onToggleAngleMode = {},
          onToggleExactMode = {},
          onSaveToNotebook = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/calcx_display.png")
  }
}
