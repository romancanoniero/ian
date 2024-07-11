package com.iyr.ian.utils.multimedia

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import java.io.IOException


class MediaPlayerUtils private constructor(val context: Context) : MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private lateinit var mInstance: MediaPlayerUtils
    private val mPlayer: MediaPlayer
    private var mVolumeLevel = -1
    override fun onPrepared(player: MediaPlayer) {
        player.start()
    }

    override fun onCompletion(mp: MediaPlayer) {
        stopFindingPhones()
    }



    private fun stop() {
        mPlayer.stop()
        mPlayer.reset()
    }

    private val vibrateHandler = Handler(Looper.getMainLooper())

    fun startFindPhone() {
        var afd: AssetFileDescriptor? = null
        stopSound()
        try {
            afd = context.assets.openFd("alarm.mp3")
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am != null) {
                mVolumeLevel = am.getStreamVolume(AudioManager.STREAM_ALARM)
                am.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    0
                )
            }
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mPlayer.stop()
            mPlayer.reset()
            mPlayer.reset()
            mPlayer.isLooping = false
            mPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mPlayer.prepareAsync()
        } catch (e: IOException) {
//            AppClass.instance.handleError(e, true)
            throw e
        } finally {
            if (afd != null) {
                try {
                    afd.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw e

                }
            }
        }
    }

    private val vibration: Runnable = object : Runnable {
        override fun run() {

            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                v.vibrate(500)
            }
            vibrateHandler.postDelayed(this, 1000)
        }
    }

    init {
        mPlayer = MediaPlayer()
        mPlayer.setOnPreparedListener(this)
        mPlayer.setOnCompletionListener(this)
    }

    val isSound: Boolean
        get() = mPlayer.isPlaying

    fun startSoundDisconnected() {
        stopSound()
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.mode != AudioManager.MODE_NORMAL) {
            //AppClass.instance.faDisconnectDuringCall()
            return
        }
        var afd: AssetFileDescriptor? = null
        try {
            afd = context.assets.openFd("lost.mp3")
            mVolumeLevel = am.getStreamVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
            mPlayer.stop()
            mPlayer.reset()
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mPlayer.reset()
            mPlayer.isLooping = true
            mPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mPlayer.prepareAsync()
            //           mPlayer.start();
        } catch (e: IOException) {
//            AppClass.instance.handleError(e, true)
            throw  e
        } finally {
            if (afd != null) {
                try {
                    afd.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw  e
                }
            }
        }
    }

    fun stopSound() {
        if (instance!!.isSound) {
            stopFindingPhones()
            instance!!.stop()
            if (mVolumeLevel >= 0) {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (am != null) {
                    am.setStreamVolume(AudioManager.STREAM_ALARM, mVolumeLevel, 0)
                    mVolumeLevel = -1
                }
            }
        }
        stopVibrate(context)
    }

    private fun stopFindingPhones() {}


    private fun stopVibrate(context : Context) {
        vibrateHandler.removeCallbacks(vibration(context))
    }

    private fun vibration(context: Context) = object : Runnable {
        override
        fun run() {

            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                v.vibrate(500)
            }
            vibrateHandler.postDelayed(this, 1000)
        }

    }

    fun startVibrate() {
        stopVibrate(context )
        vibrateHandler.post(vibration)
    }




    companion object {

        @Volatile
        private var instance: MediaPlayerUtils? = null

            fun getInstance(context: Context): MediaPlayerUtils =
            instance ?: synchronized(this) {
                instance ?: MediaPlayerUtils(context).also { instance = it

                }
            }


    }

}
