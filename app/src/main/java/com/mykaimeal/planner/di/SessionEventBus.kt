package com.mykaimeal.planner.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredFlow = _sessionExpiredFlow.asSharedFlow()

    fun emitSessionExpired() {
        _sessionExpiredFlow.tryEmit(Unit)
    }
}