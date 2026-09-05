package com.fullstackagent.android.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import java.util.Locale

class VoiceController(activity: Activity) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(activity, this)
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale.getDefault() }
    fun speak(text: String) = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "agent-reply")
    fun listen(launcher: ActivityResultLauncher<Intent>) = launcher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to your agent") })
    fun close() { tts.stop(); tts.shutdown() }
}
