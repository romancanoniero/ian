package com.iyr.ian.utils.chat.viewholders.video

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.card.MaterialCardView
import com.iyr.ian.AppConstants.Companion.chatMessagesWidthPercent
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.ui.dialogs.FullscreenVideoActivity
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.MediaPlayerInterface
import com.iyr.ian.utils.toFormattedDuration
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class CustomIncomingVideoMessageViewHolder(
    itemView: View,
    payload: Any?
) : IncomingTextMessageViewHolder<Message>(itemView, payload) {

    private val viewModel: MessagesInEventFragmentViewModel by lazy { MessagesInEventFragmentViewModel.getInstance() }

    private val cardView: MaterialCardView = itemView.findViewById(R.id.video_view_container)
    private val tvDuration: TextView = itemView.findViewById(R.id.duration)
    private val onlineIndicator: ImageView = itemView.findViewById(R.id.onlineIndicator)
    private val tvTime: TextView = itemView.findViewById(R.id.messageTime)
    private val ivVideoView: PlayerView = itemView.findViewById(R.id.videoView)
    private val ivMaximizeButton: ImageView = itemView.findViewById(R.id.exo_fullscreen_icon)
    private val networkIndicator: ImageView = itemView.findViewById(R.id.network_indicator)
    private val ivVideoPreview: ImageView = itemView.findViewById(R.id.video_preview)
    private val player: Player = ExoPlayer.Builder(AppClass.instance).build()

    init {
        ivVideoView.player = player
    }

    override fun onBind(message: Message) {
        super.onBind(message)

        val dimsInfo: HashMap<String, Any> =
            MapSituationFragmentViewModel.getInstance().windowInfo.value!!
        val screenWidth: Int = dimsInfo["screen_width"] as Int
        val itemWidth: Int = (screenWidth * chatMessagesWidthPercent).roundToInt()
        val layoutParams = cardView.layoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemWidth
        cardView.layoutParams = layoutParams

        val mediaUrl = message.video?.url ?: ""
        val mediaId = mediaUrl.getJustFileName()

        ivVideoView.visibility = View.GONE
        ivMaximizeButton.visibility = View.GONE
        ivVideoPreview.visibility = View.VISIBLE
        networkIndicator.visibility = View.GONE

        (payload as Payload).lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            MapSituationFragmentViewModel.getInstance().mediaState.collectLatest { mediaState ->
                val mediaPath = mediaState[mediaId]

                if (mediaPath != null) {
                    withContext(Dispatchers.Main) {
                        val mediaItem = MediaItem.fromUri(Uri.parse(mediaPath))
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.seekTo(10)
                        player.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_READY) {
                                    val duration = player.duration
                                    tvDuration.text = duration.toFormattedDuration()
                                }
                            }
                        })
                        ivMaximizeButton.setOnClickListener {
                            AppClass.instance.getCurrentActivity()?.apply {
                                val intent = Intent(this, FullscreenVideoActivity::class.java)
                                intent.putExtra("fileLocation", mediaPath)
                                startActivity(intent)
                            }
                        }
                        ivVideoView.visibility = View.VISIBLE
                        ivMaximizeButton.visibility = View.VISIBLE
                        ivVideoPreview.visibility = View.GONE
                        networkIndicator.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        ivVideoView.visibility = View.INVISIBLE
                        ivMaximizeButton.visibility = View.INVISIBLE
                        ivVideoPreview.visibility = View.VISIBLE
                        networkIndicator.visibility = View.VISIBLE
                    }
                    MapSituationFragmentViewModel.getInstance().downloadMedia(mediaUrl, mediaId)
                }
            }
        }
    }

    private fun assignVideoInfo(path: String) {

    }


    interface OnClicksListener {
        fun onAvatarClick()
        fun onPlayButtonClick(
            url: String?,
            mpInterface: MediaPlayerInterface?,
            callback: OnCompleteCallback?
        )
    }

    class Payload {
        var clicksListener: OnClicksListener? = null
        lateinit var lifecycleOwner: LifecycleOwner
    }
}