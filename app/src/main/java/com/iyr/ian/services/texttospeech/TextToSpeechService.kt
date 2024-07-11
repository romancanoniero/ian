package com.iyr.ian.services.texttospeech

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import com.iyr.ian.utils.tts.speak


class TextToSpeechServiceImpl : Service() {
    private val mBinder: IBinder = LocalBinder()

   inner class LocalBinder : Binder() {
        fun getService(): TextToSpeechServiceImpl {
            return this@TextToSpeechServiceImpl
        }
    }

    private lateinit var tts: TextToSpeech

    override fun onCreate() {
        super.onCreate()

        // Crear un objeto TextToSpeech
        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // El objeto TextToSpeech se ha creado correctamente
            } else {
                // El objeto TextToSpeech no se ha creado correctamente
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    // Método para reproducir audio
    fun speak(text: String, queueMode: Int, params: HashMap<String, String>?) {
        Log.d("TTS...","Llegue al metodo speak")

//        tts.speak(text, queueMode, params)
       applicationContext.speak(text)
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)

//        TODO("Not yet implemented")
    }

    fun stop() {
  //      TODO("Not yet implemented")
        tts.stop()
    }

}


