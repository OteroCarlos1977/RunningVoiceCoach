package com.otero.runningvoicecoach.voice

interface VoiceCoach {
    fun speak(message: String, flush: Boolean = false)
    fun stop()
    fun shutdown()
}
