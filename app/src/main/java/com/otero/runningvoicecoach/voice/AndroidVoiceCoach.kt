package com.otero.runningvoicecoach.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID

class AndroidVoiceCoach(
    context: Context
) : VoiceCoach {
    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private var spanishAvailable = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = textToSpeech ?: return@TextToSpeech
                val languageResult = tts.setLanguage(Locale("es", "AR"))
                spanishAvailable = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                isReady = true
            }
        }
    }

    override fun speak(message: String, flush: Boolean) {
        val cleanMessage = message.trim()
        if (!isReady || !spanishAvailable || cleanMessage.isBlank()) {
            return
        }

        val tts = textToSpeech ?: return
        if (tts.isSpeaking && !flush) {
            return
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts.speak(cleanMessage, queueMode, null, UUID.randomUUID().toString())
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
        spanishAvailable = false
    }
}
