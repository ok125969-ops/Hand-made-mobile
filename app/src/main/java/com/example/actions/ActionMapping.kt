package com.example.actions

import com.example.gesture.GestureType

/**
 * Preset profiles for quick configuration.
 */
enum class GesturePresetProfile(val title: String, val description: String) {
  BALANCED_DEFAULT("Balanced Standard", "Default navigation, media playback, and volume control"),
  MEDIA_CONTROLLER("Media Controller", "Optimized for Spotify, YouTube, and Netflix playback"),
  READER_BROWSER("E-Reader & Web", "Optimized for scrolling, next page, and back navigation"),
  PRESENTATION("Presentation & Slides", "Optimized for forward/back slide progression and mute")
}

object DefaultGestureMappings {

  fun getPreset(profile: GesturePresetProfile): Map<GestureType, GestureAction> {
    return when (profile) {
      GesturePresetProfile.BALANCED_DEFAULT -> mapOf(
        GestureType.SWIPE_LEFT to GestureAction.MEDIA_PREVIOUS,
        GestureType.SWIPE_RIGHT to GestureAction.MEDIA_NEXT,
        GestureType.SWIPE_UP to GestureAction.VOLUME_UP,
        GestureType.SWIPE_DOWN to GestureAction.VOLUME_DOWN,
        GestureType.OPEN_PALM to GestureAction.MEDIA_PLAY_PAUSE,
        GestureType.CLOSED_FIST to GestureAction.BACK,
        GestureType.POINTING to GestureAction.HOME,
        GestureType.VICTORY_PEACE to GestureAction.RECENTS,
        GestureType.THUMB_UP to GestureAction.VOLUME_UP,
        GestureType.THUMB_DOWN to GestureAction.VOLUME_DOWN,
        GestureType.OK_GESTURE to GestureAction.FLASHLIGHT_TOGGLE,
        GestureType.PINCH to GestureAction.SCROLL_DOWN,
        GestureType.PUSH_FORWARD to GestureAction.NOTIFICATION_SHADE,
        GestureType.PULL_BACK to GestureAction.BACK,
        GestureType.OPEN_PALM_HOLD to GestureAction.MUTE_TOGGLE,
        GestureType.PINCH_HOLD to GestureAction.SCROLL_DOWN,
        GestureType.POINT_HOLD to GestureAction.SCROLL_UP
      )

      GesturePresetProfile.MEDIA_CONTROLLER -> mapOf(
        GestureType.OPEN_PALM to GestureAction.MEDIA_PLAY_PAUSE,
        GestureType.SWIPE_RIGHT to GestureAction.MEDIA_NEXT,
        GestureType.SWIPE_LEFT to GestureAction.MEDIA_PREVIOUS,
        GestureType.SWIPE_UP to GestureAction.VOLUME_UP,
        GestureType.SWIPE_DOWN to GestureAction.VOLUME_DOWN,
        GestureType.THUMB_UP to GestureAction.VOLUME_UP,
        GestureType.THUMB_DOWN to GestureAction.VOLUME_DOWN,
        GestureType.OPEN_PALM_HOLD to GestureAction.MUTE_TOGGLE,
        GestureType.CLOSED_FIST to GestureAction.BACK
      )

      GesturePresetProfile.READER_BROWSER -> mapOf(
        GestureType.SWIPE_UP to GestureAction.SCROLL_DOWN,
        GestureType.SWIPE_DOWN to GestureAction.SCROLL_UP,
        GestureType.SWIPE_LEFT to GestureAction.BACK,
        GestureType.SWIPE_RIGHT to GestureAction.HOME,
        GestureType.POINTING to GestureAction.SCROLL_DOWN,
        GestureType.POINT_HOLD to GestureAction.SCROLL_DOWN,
        GestureType.PINCH_HOLD to GestureAction.SCROLL_UP,
        GestureType.CLOSED_FIST to GestureAction.BACK
      )

      GesturePresetProfile.PRESENTATION -> mapOf(
        GestureType.SWIPE_RIGHT to GestureAction.MEDIA_NEXT,
        GestureType.SWIPE_LEFT to GestureAction.MEDIA_PREVIOUS,
        GestureType.OPEN_PALM to GestureAction.MEDIA_PLAY_PAUSE,
        GestureType.THUMB_UP to GestureAction.VOLUME_UP,
        GestureType.OPEN_PALM_HOLD to GestureAction.MUTE_TOGGLE
      )
    }
  }
}
