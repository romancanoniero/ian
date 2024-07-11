package com.iyr.ian.utils.chat.viewholders.voice

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/*
 * Created by troy379 on 05.04.17.
 */
class CustomOutcomingVoiceMessageViewHolder(itemView: View, payload: Any?) :
    OutcomingTextMessageViewHolder<Message>(itemView, payload) {
    private val tvTime: TextView
    private val tvDuration: TextView
    private val mediaView: PlayerView
    private lateinit var player: Player
    private val statusIcon: ImageView
    //  private val playStopButton: ImageView

    init {
        tvDuration = itemView.findViewById(R.id.duration)
        tvTime = itemView.findViewById(R.id.messageTime)
        mediaView = itemView.findViewById(R.id.audioView)
        statusIcon = itemView.findViewById(R.id.status)
        // playStopButton = itemView.findViewById(R.id.play_stop_button)
    }

    override fun onBind(message: Message) {
        super.onBind(message)

        player = ExoPlayer.Builder(instance).build()
        mediaView.player = player

        val fileName = message.voice?.url.toString()

        GlobalScope.launch(Dispatchers.IO) {

            message.status = MessagesStatus.UPLOADING
            updateFileStatus(message)

            val finalPath = FirebaseStorage.getInstance()
                .getReference(fileName.getJustFileName())
                .downloadUrlWithCache(
                    instance,
                    fileName.replace("file:"+AppClass.instance.cacheDir.toString()+"/","").substringBeforeLast("/")
                )

            // Build the media item.

            if (finalPath == null) {
                message.status = MessagesStatus.ERROR
                updateFileStatus(message)
                return@launch
            } else {
                message.status = MessagesStatus.READY
                updateFileStatus(message)
            }

            if (message.status == MessagesStatus.READY) {
                withContext(Dispatchers.Main) {
                    val mediaItem = MediaItem.fromUri(File(finalPath).toString())
                    // Set the media item to be played.
                    player.setMediaItem(mediaItem)
                    player.prepare()

                    message.status = MessagesStatus.READY
                    updateFileStatus(message)

                    tvDuration.text = MultimediaUtils.getInstance(instance)
                        ?.getDurationFormated(Uri.parse(finalPath))
                }
            }
        }
        //     }

        tvTime.text = DateFormatter.format(message.createdAt, DateFormatter.Template.TIME)

        //We can set click listener on view from payload
        val payload = payload as Payload

    }


    private suspend fun updateFileStatus(message: Message?) {
        when (message?.status) {
            MessagesStatus.UPLOADING -> {

                withContext(Dispatchers.Main) {
                    statusIcon.setImageDrawable(AppCompatResources.getDrawable(instance,R.drawable.ico_cloud_upload_24))
                    statusIcon.visibility = View.VISIBLE

                    val animation: Animation = AlphaAnimation(1f, 0f)
                    animation.duration = 1000
                    animation.interpolator = LinearInterpolator()
                    animation.repeatCount = Animation.INFINITE
                    animation.repeatMode = Animation.REVERSE
                    statusIcon.startAnimation(animation)

                    Handler(Looper.getMainLooper()).postDelayed({
                        animation.cancel()
                    }, 500)

                }


            }

            MessagesStatus.READY -> {
                withContext(Dispatchers.Main) {
                    statusIcon.clearAnimation()
                    statusIcon.visibility = View.GONE
                }
            }

            else -> {
                /*
                    MessagesStatus.SENDING -> TODO()
                    MessagesStatus.SENT -> TODO()
                    MessagesStatus.DELIVERED -> TODO()
                    MessagesStatus.READ -> TODO()
                    MessagesStatus.ERROR -> TODO()
                    null -> TODO()
                  */
            }
        }
    }



    interface OnClicksListener {
        fun onPlayButtonClick(
            url: String?,
            mpInterface: IMultimediaPlayer?,
            callback: OnCompleteCallback?
        )
    }

    class Payload {
        var clicksListener: OnClicksListener? = null
    }
}