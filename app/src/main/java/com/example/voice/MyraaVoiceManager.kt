package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyraaVoiceManager(context: Context) : TextToSpeech.OnInitListener {

  private val appContext = context.applicationContext
  private var tts: TextToSpeech? = null
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _lastSpokenPhrase = MutableStateFlow("")
  val lastSpokenPhrase: StateFlow<String> = _lastSpokenPhrase.asStateFlow()

  init {
    try {
      tts = TextToSpeech(appContext, this)
    } catch (e: Exception) {
      Log.e("MyraaVoiceManager", "Error creating TTS instance", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.US)
      if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setPitch(0.95f) // JARVIS-style calm pitch
        tts?.setSpeechRate(1.05f) // crisp, confident pace
        isInitialized = true
        setupUtteranceListener()
      }
    }
  }

  private fun setupUtteranceListener() {
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {
        _isSpeaking.value = true
      }

      override fun onDone(utteranceId: String?) {
        _isSpeaking.value = false
      }

      override fun onError(utteranceId: String?) {
        _isSpeaking.value = false
      }
    })
  }

  fun speak(text: String, isProactive: Boolean = true) {
    if (text.isBlank()) return
    _lastSpokenPhrase.value = text

    if (isInitialized && tts != null) {
      val utteranceId = UUID.randomUUID().toString()
      _isSpeaking.value = true
      tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    } else {
      // In tests / headless mode, simulate brief speech state
      _isSpeaking.value = true
    }
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (e: Exception) {
      // ignore
    }
    _isSpeaking.value = false
  }
}
