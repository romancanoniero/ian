package com.iyr.ian.utils.chat.viewholders.video

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.card.MaterialCardView
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.ui.dialogs.FullscreenVideoActivity
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.getStoredFile
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.multimedia.MediaFileDownloadStatusEnum
import com.iyr.ian.utils.multimedia.MediaFileStatusEnum
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder

/*
 * Created by troy379 on 05.04.17.
 */
class CustomOutcomingVideoMessageViewHolder(itemView: View, payload: Any?) :
    OutcomingTextMessageViewHolder<Message?>(itemView, payload) {
    private val tvTime: TextView
    private val tvDuration: TextView

    //    private val playStopButton: ImageView
    //private val ivVideoHolder: ImageView
//    private val ivVideoView: VideoView
    private val ivVideoView: PlayerView
    private val ivMessageStatus: ImageView
    private val videoViewContainer: MaterialCardView
    private val ivMaximizeButton: ImageView

    // private val animLoading: LottieAnimationView
    private lateinit var player: Player

    init {
        tvDuration = itemView.findViewById(R.id.duration)
        tvTime = itemView.findViewById(R.id.messageTime)
        //playStopButton = itemView.findViewById(R.id.play_stop_button)
        ivMessageStatus = itemView.findViewById(R.id.iv_message_status)
        ivVideoView = itemView.findViewById(R.id.videoView)
        ivMaximizeButton = itemView.findViewById(R.id.exo_fullscreen_icon)

        videoViewContainer = itemView.findViewById(R.id.video_view_container)
        //    animLoading = itemView.findViewById(R.id.animation)
        //exoVideoView = itemView.findViewById(R.id.exoPlayerVIew);

    }

    var fullscreen: Boolean = false

    override fun onBind(message: Message?) {
        super.onBind(message)


        player = ExoPlayer.Builder(instance).build()
        val mediaController = MediaController(instance)
        val fullPath = message?.video?.url!!



        if (message.status == MessagesStatus.SENDING) {
            ivMessageStatus.visibility = View.VISIBLE
            ivMessageStatus.setImageDrawable(instance.getDrawable(R.drawable.ico_cloud_upload_24))
        } else
            if (message.status == MessagesStatus.SENT) {
                ivMessageStatus.visibility = View.VISIBLE
                ivMessageStatus.setImageDrawable(instance.getDrawable(R.drawable.ic_outline_check))
            } else
                if (message.status == MessagesStatus.ERROR) {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageDrawable(instance.getDrawable(R.drawable.ic_error))
                } else {
                    ivMessageStatus.visibility = View.GONE
                }





            try {
                val finalPath = (AppClass.instance as Context).getStoredFile(
                    fullPath.getJustFileName(),
                    fullPath.substringBeforeLast("/")
                )

                // Build the media item.
                val mediaItem = MediaItem.fromUri(finalPath)
                // Set the media item to be played.

                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.seekTo(1)

                    ivMaximizeButton.setOnClickListener(View.OnClickListener {
                        instance.getCurrentActivity()?.apply {
                            val intent = Intent(this, FullscreenVideoActivity::class.java)
                            intent.putExtra("fileLocation", finalPath)
                            startActivity(intent)
                        }
                    })


                ivVideoView.player = player

            } catch (ex: Exception) {
                var pp = ex
            }

            /*
                        var finalPath = FirebaseStorage.getInstance()
                            .getReference(fullPath.getJustFileName())
                            .downloadUrlWithCache(
                                instance,
                                fullPath.substringBeforeLast("/")
                            ) //  ejemplo de cadena : chats/-NiZbEApRCu2X17fW-u0/VID-8842081654125773802.mp4

                        //.substring(0, fileName.lastIndexOf('/'))
            */


        tvTime.text = DateFormatter.format(message.createdAt, DateFormatter.Template.TIME)

        //We can set click listener on view from payload
        val payload = payload as Payload
    }

    private fun stopMessage(commonMediaPlayer: MediaPlayer, message: Message) {
        commonMediaPlayer.stop()
        commonMediaPlayer.release()
        instance.setCurrentMediaPlayer(null)
        message.video.status = MediaFileStatusEnum.STOPPED.name
        updateUIPlayStopButton(message)
    }

    private fun playMessage(message: Message, url: String, payload: Payload) {/*
          ivVideoView.setOnCompletionListener {
              message.video.status = MediaFileStatusEnum.STOPPED.name
              updateUIPlayStopButton(message)
          }
  */
        if (message.video.status.isEmpty() || message.video.status == MediaFileStatusEnum.STOPPED.name || message.video.status == MediaFileDownloadStatusEnum.READY.name) {
            //          ivVideoView.start()

// Prepare the player.
            player.prepare()
// Start the playback.
            try {
                player.play()
            } catch (exception: Exception) {
                var pp = 3
            }



            message.video.status = MediaFileStatusEnum.PLAYING.name
            updateUIPlayStopButton(message)
        } else {
            message.video.status = MediaFileStatusEnum.STOPPED.name
            updateUIPlayStopButton(message)
        }
    }

    private fun updateUIPlayStopButton(message: Message) {/*
        if (message.video.status.compareTo(MediaFileDownloadStatusEnum.DOWNLOADING.name) == 0) {
            playStopButton.setImageResource(R.drawable.ic_sand_clock)
        } else if (message.video.status.compareTo(MediaFileDownloadStatusEnum.READY.name) == 0 ||
            message.video.status.compareTo(MediaFileStatusEnum.STOPPED.name) == 0
        ) {
            playStopButton.setImageResource(R.drawable.ic_audio_play)
        } else if (message.video.status.compareTo(MediaFileStatusEnum.PLAYING.name) == 0) {
            playStopButton.setImageResource(R.drawable.ic_audio_stop)
        }

         */
    }

    interface OnClicksListener {
        fun onPlayButtonClick(
            url: String?, mpInterface: IMultimediaPlayer?, callback: OnCompleteCallback?
        )
    }

    class Payload {
        var clicksListener: OnClicksListener? = null
    }
}