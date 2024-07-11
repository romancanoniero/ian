package com.iyr.ian.utils.tts

import android.app.Activity
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechExt {

    companion object

    fun speak(context: Context, text: String) {
        context.speak(text)
    }
}

fun Activity.speak(text: String) = this.baseContext.speak(text)

fun Context.speak(text: String) {
    var TTS: TextToSpeech? = null

    TTS = TextToSpeech(this, object : TextToSpeech.OnInitListener {

        val TAG = "TTS"
        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {

                val avail: Set<Locale> = TTS?.availableLanguages as Set<Locale>
                for (locale in avail) {
                    Log.e(TAG, "local: $locale")
                    if (locale.displayVariant != null) {
                        Log.e(TAG, "  var: " + locale.variant)
                    }
                }
                val engineInfo: List<TextToSpeech.EngineInfo> =
                    TTS?.engines as List<TextToSpeech.EngineInfo>
                for (info in engineInfo) {
                    Log.e(TAG, "info: $info")
                }
                val res: Int? = TTS?.setLanguage(Locale.getDefault())
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not Supporter")
                } else {

                    //speak(context, text)
                    TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                }
            } else {
                Log.e("TTS", "Init Failed")
            }
        }
    })
}




