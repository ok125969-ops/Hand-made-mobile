package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.DecisionType
import com.example.model.EventDecision
import com.example.model.EventType
import com.example.ui.components.DecisionCard
import com.example.ui.theme.MyApplicationTheme
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
  fun greeting_screenshot() {
    val sampleDecision = EventDecision(
      eventId = "sample-1",
      eventType = EventType.BUILD_FAILED,
      decisionType = DecisionType.SPEAK,
      speechText = "The build failed. I found an error in DashboardViewModel.kt.",
      relevanceScore = 0.95f,
      interruptScore = 0.82f,
      reason = "Active user task + important compiler error in DashboardViewModel.kt"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        DecisionCard(decision = sampleDecision)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
