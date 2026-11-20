package io.github.jameslebone.autocontactbridge.shared.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class CarTtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingSpeechQueue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
                for (pending in pendingSpeechQueue) {
                    speak(pending)
                }
                pendingSpeechQueue.clear()
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized) {
            pendingSpeechQueue.add(text)
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        if (onDone != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}

                override fun onDone(id: String?) {
                    if (id == utteranceId) {
                        onDone()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {}

                override fun onError(id: String?, errorCode: Int) {}
            })
        }

        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
