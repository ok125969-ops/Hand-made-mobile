package com.example.actions

/**
 * Supported Android actions that can be triggered via hand gestures.
 */
enum class GestureAction(
  val displayName: String,
  val category: ActionCategory,
  val description: String,
  val requiresAccessibility: Boolean = false
) {
  // Navigation
  BACK("Back", ActionCategory.NAVIGATION, "Navigates back in current app", true),
  HOME("Home", ActionCategory.NAVIGATION, "Returns to Android Home screen", true),
  RECENTS("Recent Apps", ActionCategory.NAVIGATION, "Opens multitasking app overview", true),
  NOTIFICATION_SHADE("Notifications", ActionCategory.NAVIGATION, "Expands Android notification shade", true),
  QUICK_SETTINGS("Quick Settings", ActionCategory.NAVIGATION, "Opens Android quick settings panel", true),

  // Scrolling
  SCROLL_UP("Scroll Up", ActionCategory.SCROLLING, "Scrolls current page or feed up", true),
  SCROLL_DOWN("Scroll Down", ActionCategory.SCROLLING, "Scrolls current page or feed down", true),

  // Media Playback
  MEDIA_PLAY_PAUSE("Play / Pause Media", ActionCategory.MEDIA, "Toggles music or video playback", false),
  MEDIA_NEXT("Next Track", ActionCategory.MEDIA, "Skips to the next music/video track", false),
  MEDIA_PREVIOUS("Previous Track", ActionCategory.MEDIA, "Skips to the previous track", false),

  // Volume & Audio
  VOLUME_UP("Volume Up", ActionCategory.AUDIO, "Increases media audio volume", false),
  VOLUME_DOWN("Volume Down", ActionCategory.AUDIO, "Decreases media audio volume", false),
  MUTE_TOGGLE("Toggle Mute", ActionCategory.AUDIO, "Mutes or unmutes device audio", false),

  // Device & Utility
  FLASHLIGHT_TOGGLE("Toggle Flashlight", ActionCategory.UTILITY, "Turns phone torch on or off", false),
  TAKE_SCREENSHOT("Take Screenshot", ActionCategory.UTILITY, "Captures device screen", true),

  // Custom / App
  NONE("Do Nothing", ActionCategory.SYSTEM, "Ignores this gesture", false)
}

enum class ActionCategory(val label: String) {
  NAVIGATION("System Navigation"),
  SCROLLING("Page Scrolling"),
  MEDIA("Media Playback"),
  AUDIO("Volume & Audio"),
  UTILITY("Device Tools"),
  SYSTEM("General")
}
