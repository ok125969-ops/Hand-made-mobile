package com.example.gesture

/**
 * All supported hand gesture types across Basic, Dynamic, and Hold classifications.
 */
enum class GestureType(val displayName: String, val category: GestureCategory, val description: String) {
  // Basic Static Gestures
  OPEN_PALM("Open Palm", GestureCategory.BASIC, "All 5 fingers extended and spread open"),
  CLOSED_FIST("Closed Fist", GestureCategory.BASIC, "All fingers curled tightly into a fist"),
  POINTING("Pointing", GestureCategory.BASIC, "Index finger extended, other fingers curled"),
  THUMB_UP("Thumb Up", GestureCategory.BASIC, "Thumb pointed upward, other fingers curled"),
  THUMB_DOWN("Thumb Down", GestureCategory.BASIC, "Thumb pointed downward, other fingers curled"),
  VICTORY_PEACE("Peace / Victory", GestureCategory.BASIC, "Index and middle fingers extended in a V-shape"),
  OK_GESTURE("OK Sign", GestureCategory.BASIC, "Thumb and index tips touching in a loop"),
  PINCH("Pinch", GestureCategory.BASIC, "Thumb and index tips pressed closely together"),
  TWO_HANDS("Two Hands", GestureCategory.BASIC, "Both hands visible simultaneously"),

  // Dynamic Motion Gestures
  SWIPE_LEFT("Swipe Left", GestureCategory.DYNAMIC, "Fast hand sweep toward the left"),
  SWIPE_RIGHT("Swipe Right", GestureCategory.DYNAMIC, "Fast hand sweep toward the right"),
  SWIPE_UP("Swipe Up", GestureCategory.DYNAMIC, "Fast hand sweep upward"),
  SWIPE_DOWN("Swipe Down", GestureCategory.DYNAMIC, "Fast hand sweep downward"),
  HAND_MOVE_LEFT("Move Left", GestureCategory.DYNAMIC, "Smooth horizontal shift to the left"),
  HAND_MOVE_RIGHT("Move Right", GestureCategory.DYNAMIC, "Smooth horizontal shift to the right"),
  HAND_MOVE_UP("Move Up", GestureCategory.DYNAMIC, "Smooth vertical shift upward"),
  HAND_MOVE_DOWN("Move Down", GestureCategory.DYNAMIC, "Smooth vertical shift downward"),
  PUSH_FORWARD("Push Forward", GestureCategory.DYNAMIC, "Hand thrust toward the camera lens"),
  PULL_BACK("Pull Back", GestureCategory.DYNAMIC, "Hand retracted away from camera"),
  CIRCULAR_MOTION("Circular Motion", GestureCategory.DYNAMIC, "Hand tracing a clockwise/counter-clockwise circle"),

  // Hold Continuous Gestures
  PINCH_HOLD("Pinch & Hold", GestureCategory.HOLD, "Pinch gesture maintained over time"),
  OPEN_PALM_HOLD("Open Palm & Hold", GestureCategory.HOLD, "Open palm maintained over time"),
  FIST_HOLD("Fist & Hold", GestureCategory.HOLD, "Closed fist maintained over time"),
  POINT_HOLD("Point & Hold", GestureCategory.HOLD, "Pointing maintained over time"),

  NONE("None", GestureCategory.NONE, "No gesture recognized")
}

enum class GestureCategory(val label: String) {
  BASIC("Basic Static"),
  DYNAMIC("Dynamic Motion"),
  HOLD("Hold & Repeat"),
  NONE("None")
}
