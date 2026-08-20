package com.example.engine

import com.example.model.MYRAAEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MYRAAEventBus {
  private val _events = MutableSharedFlow<MYRAAEvent>(
    replay = 0,
    extraBufferCapacity = 64
  )
  val events: SharedFlow<MYRAAEvent> = _events.asSharedFlow()

  suspend fun postEvent(event: MYRAAEvent) {
    _events.emit(event)
  }

  fun tryPostEvent(event: MYRAAEvent): Boolean {
    return _events.tryEmit(event)
  }
}
