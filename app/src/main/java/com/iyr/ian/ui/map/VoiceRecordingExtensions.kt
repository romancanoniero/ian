package com.iyr.ian.ui.map

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.iyr.ian.AppConstants.Companion.CHAT_FILES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.ui.chat.MessagesInEventFragment
import com.iyr.ian.utils.createDirectoryStructure
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.toMediaFile
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import com.visualizer.amplitude.AudioRecordView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID

class MapSituationFragmentExtensions {
}

fun MessagesInEventFragment.startMonitoringWave() {

    var iconRecorder = popupView?.findViewById<ImageView>(R.id.icon_audio)
    val iconRecordView: AudioRecordView = popupView?.findViewById(R.id.audioRecordView)!!
    val captionRecorder: TextView = popupView?.findViewById(R.id.caption_record)!!
    captionRecorder.text = requireContext().getString(R.string.recording)
    iconRecordView.visibility = View.VISIBLE
    lifecycleScope.launch(Dispatchers.Main) {
        while (iconRecordView.visibility == View.VISIBLE) {
            val currentMaxAmplitude = recordSession?.maxAmplitude ?: 0
            Log.d("WAVE", currentMaxAmplitude.toString())
            iconRecordView.update(currentMaxAmplitude)   //redraw view
            delay(100)
        }
    }
}


fun MessagesInEventFragment.stopMonitoringWave() {

    val iconRecorder: ImageView = popupView?.findViewById(R.id.icon_audio)!!
    val captionRecorder: TextView = popupView?.findViewById(R.id.caption_record)!!
    val iconRecordView: AudioRecordView = popupView?.findViewById(R.id.audioRecordView)!!

    captionRecorder.text = requireContext().getText(R.string.voice_message)
    iconRecorder.setImageDrawable(
        AppCompatResources.getDrawable(
            requireContext(), R.drawable.ic_audio_mic_outline
        )
    )
    iconRecordView.visibility = View.INVISIBLE
    iconRecordView.recreate()
}


fun MessagesInEventFragment.recordingManagement(motionEvent: MotionEvent): Boolean {
    val eventKey = MapSituationFragmentViewModel.getInstance().auxEventKey.value
    val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
    when (motionEvent.action) {
        MotionEvent.ACTION_DOWN -> {

            recordingFilename =
                AppClass.instance.cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH+"/" + eventKey + "/" + UUID.randomUUID()
                    .toString() + ".3gp"

            requireContext().createDirectoryStructure(recordingFilename!!)

            recordSession = MultimediaUtils(this.requireContext()).startRecording(
                AppClass.instance.getCurrentActivity()!!, recordingFilename!!
            )!!

            startMonitoringWave()

            val iconBitmap = requireContext().getBitmapFromVectorDrawable(
                R.drawable.ic_recording
            )

            val iconRecorder: ImageView = popupView?.findViewById(R.id.icon_audio)!!
            iconRecorder.setImageBitmap(iconBitmap)
            requireActivity().playSound(R.raw.recording_start, null, null)
        }

        MotionEvent.ACTION_UP -> {
            Log.d("RECORDING ", "END")
            try {
                popupView?.performClick()
                requireActivity().playSound(R.raw.recording_stop, null, null)

                var iconBitmap = requireContext().getBitmapFromVectorDrawable(
                    R.drawable.ic_microphone
                )
                //                  recordButton.setImageBitmap(iconBitmap)

                //Do Nothing
                MultimediaUtils(requireContext()).stopRecording(
                    recordSession, recordingFilename!!
                )

                val callback = object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {

                    }
                }


                stopMonitoringWave()

                val voiceFile = File(recordingFilename)
                try {
                    val fileName = recordingFilename.toString().getJustFileName()
                    /*
                                        val mediaFile = prepareMediaMessage(
                                            MediaTypesEnum.AUDIO, fileName, recordingFilename.toString()
                                        )
                    */
                    lifecycleScope.launch(Dispatchers.IO) {

                        val mediaFile = File(recordingFilename).toMediaFile(requireContext(),"${CHAT_FILES_STORAGE_PATH}${eventKey}")

                        when (mediaFile) {
                            is MediaFile -> {
                                mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                                mediaFile.time = Date().time



                                MapSituationFragmentViewModel.getInstance().downloadMedia( mediaFile.localFullPath.replace( requireContext().cacheDir.toString()+"/" ,""), mediaFile.localFullPath.getJustFileName())
                                UserViewModel.getInstance().getUser()?.let { user ->
                                    viewModel.onNewMediaMessage(
                                        user, mediaFile, voiceFile
                                    )
                                }

                                recordSession = null
                            }

                            else -> {
                                launch(Dispatchers.Main) {
                                    requireActivity().showErrorDialog(mediaFile.toString())
                                }
                            }

                        }
                    }
                } catch (ex: Exception) {
                    requireActivity().showErrorDialog(ex.localizedMessage)
                }

            } catch (ex: Exception) {
                recordSession = null
            }
        }
    }
    return false
}
