package com.iyr.ian.utils.chat.viewholders.video


import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.ui.dialogs.FullscreenVideoActivity
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.MediaFileDownloadStatusEnum
import com.iyr.ian.utils.multimedia.MediaFileStatusEnum
import com.iyr.ian.utils.multimedia.MediaPlayerInterface
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/*
 * Created by troy379 on 05.04.17.
 */
class CustomIncomingVideoMessageViewHolder(itemView: View, payload: Any?) :
    IncomingTextMessageViewHolder<Message>(itemView, payload) {
    private val tvTime: TextView
    private val tvDuration: TextView

    //  private val playStopButton: ImageView
    private val onlineIndicator: View

    //    private val ivVideoHolder: ImageView
    private val ivMaximizeButton: ImageView
    private val ivVideoView: PlayerView
 //   private val animLoading: LottieAnimationView

    private lateinit var player: Player

    init {
        tvDuration = itemView.findViewById(R.id.duration)
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
        tvTime = itemView.findViewById(R.id.messageTime)
        ivVideoView = itemView.findViewById(R.id.videoView)
        ivMaximizeButton = itemView.findViewById(R.id.exo_fullscreen_icon)
      //  animLoading = itemView.findViewById(R.id.animation)
    }

    var fullscreen: Boolean = false

    override fun onBind(message: Message) {
        super.onBind(message)
        updateUIPlayStopButton(message)

        player = ExoPlayer.Builder(instance).build()

        val isOnline = message.user.isOnline
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online)
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline)
        }
        tvDuration.text = StringUtils.getDurationString(
            message.video.duration / 1000
        )
        tvTime.text = DateFormatter.format(message.createdAt, DateFormatter.Template.TIME)
        //----------------
        val justFileName = FileUtils().getJustFileName(message.video.url)

        ivVideoView.player = player

        val fileName = message.video?.url!!

        var finalPath = ""
        if (fileName.startsWith("file:") == false) {
            finalPath = "file:" + instance.cacheDir.toString() + "/" + fileName
        } else {
            finalPath = fileName
        }




        ivMaximizeButton.setOnClickListener(View.OnClickListener {

            instance.getCurrentActivity()?.apply {
                val intent = Intent(this, FullscreenVideoActivity::class.java)
                intent.putExtra("fileLocation", finalPath)
                startActivity(intent)
            }


            /*
                        AppClass.instance.getCurrentActivity()?.apply {

                            var fullScreenVideoPlayer: PlayerView? =
                                (AppClass.instance.getCurrentActivity() as MainActivity).binding.fullScreenVideoPlayer
                            if (fullscreen) {
                                ivMaximizeButton.setImageDrawable(instance.getDrawable(com.iyr.ian.R.drawable.baseline_close_fullscreen_24))
                                getWindow()?.getDecorView()?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
                                /*
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().show()
                                }

                                 */
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                val params =
                                    fullScreenVideoPlayer?.getLayoutParams() as ConstraintLayout.LayoutParams
                                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                                params.height =
                                    (200 * AppClass.instance.resources.displayMetrics.density).toInt()
                                ivVideoView.setLayoutParams(params)
                                fullScreenVideoPlayer?.visibility = View.GONE

                                fullscreen = false
                            } else {
                                ivMaximizeButton.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        instance,
                                        com.iyr.ian.R.drawable.baseline_close_fullscreen_24
                                    )
                                )
                                getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_FULLSCREEN
                                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                )
                                /*
                                                    if (getSupportActionBar() != null) {
                                                        getSupportActionBar().hide()
                                                    }

                                 */
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)


                                val params =
                                    fullScreenVideoPlayer?.getLayoutParams() as ConstraintLayout.LayoutParams
                                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                                fullScreenVideoPlayer.setLayoutParams(params)
                                fullscreen = true


                                fullScreenVideoPlayer?.player = player

                                player.prepare()

                                player.play()
                                fullScreenVideoPlayer?.visibility = View.VISIBLE
                            }

                        }
            */
        })


        /*
                if (message?.video != null) {
                    if (message?.video?.status?.compareTo(MediaFileDownloadStatusEnum.DOWNLOADING.name) == 0) {
                        playStopButton.setImageResource(R.drawable.ic_sand_clock)
                    } else
                        playStopButton.setImageResource(R.drawable.ic_audio_play)
                }
        */


        GlobalScope.launch(Dispatchers.Main) {

            try {
                finalPath = FirebaseStorage.getInstance()
                    .getReference(fileName.getJustFileName())
                    .downloadUrlWithCache(
                        instance,
                        fileName.substringBeforeLast("/")
                    )

            }
            catch (e: Exception) {
                var pp = 3
            }




            val payload = payload as Payload


            val mediaItem = MediaItem.fromUri(finalPath)
            // Set the media item to be played.

/*
            if (message.status == MessagesStatus.SENDING) {
                animLoading.visibility = View.VISIBLE
            }
            else
                animLoading.visibility = View.GONE
*/

            player.setMediaItem(mediaItem)
            player.prepare()
            /*
                        playStopButton.setOnClickListener {
                            if (message.video.status.isEmpty() || message.video.status.compareTo(
                                    MediaFileDownloadStatusEnum.READY.name
                                ) == 0 ||
                                message.video.status.compareTo(MediaFileStatusEnum.STOPPED.name) == 0
                            ) {
                                //                 if (payload != null && payload.clicksListener != null) {
                                playMessage(message, finalPath, payload)
                                //               }
                            } else {
                                // Press stop
                                val commonMediaPlayer = instance.getCurrentMediaPlayer()
                                commonMediaPlayer?.let { stopMessage(it, message) }
                            }
                        }
            */
        }
    }

    private fun stopMessage(commonMediaPlayer: MediaPlayer, message: Message) {
        commonMediaPlayer.stop()
        commonMediaPlayer.release()
        instance.setCurrentMediaPlayer(null)
        message.video.status = MediaFileStatusEnum.STOPPED.name
        updateUIPlayStopButton(message)
    }

    private fun playMessage(message: Message, url: String, payload: Payload) {
        val mpInterface: MediaPlayerInterface = object : MediaPlayerInterface {
            override fun onBeforePlay() {
                message.video.status = MediaFileStatusEnum.PLAYING.name
                updateUIPlayStopButton(message)
            }

            override fun onAfterPlay() {
                val despues = 33
                message.video.status = MediaFileStatusEnum.STOPPED.name
                updateUIPlayStopButton(message)
            }
        }
        if (message.video.status == MediaFileStatusEnum.STOPPED.name || message.video.status == MediaFileDownloadStatusEnum.READY.name) {
            payload.clicksListener!!.onPlayButtonClick(url, mpInterface, null)
        }

        GlobalScope.launch(Dispatchers.Main) {
            // Build the media item.
            val mediaItem = MediaItem.fromUri(url)
            // Set the media item to be played.
            player.setMediaItem(mediaItem)
            try {
                //ivVideoHolder.visibility = View.GONE
                ivVideoView.visibility = View.VISIBLE
                player.prepare()
                // Start the playback.
                player.play()
            } catch (exception: Exception) {
                var pp = 3
            }
        }

    }

    private fun updateUIPlayStopButton(message: Message) {
        /*
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
        fun onAvatarClick()

        //   void onPlayButtonClick(String url);
        fun onPlayButtonClick(
            url: String?,
            mpInterface: MediaPlayerInterface?,
            callback: OnCompleteCallback?
        )
    }

    class Payload {
        var clicksListener: OnClicksListener? = null
    }
}