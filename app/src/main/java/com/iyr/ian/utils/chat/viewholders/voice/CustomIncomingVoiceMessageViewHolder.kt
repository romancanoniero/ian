package com.iyr.ian.utils.chat.viewholders.voice

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
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.toFormattedDuration
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CustomIncomingVoiceMessageViewHolder(
    itemView: View,
    payload: Any?
) : IncomingTextMessageViewHolder<Message>(itemView, payload) {

    private val tvTime: TextView = itemView.findViewById(R.id.messageTime)
    private val tvDuration: TextView = itemView.findViewById(R.id.duration)
    private val mediaView: PlayerView = itemView.findViewById(R.id.audioView)
    private val chatBubble: MaterialCardView = itemView.findViewById(R.id.chat_bubble)
    private val onlineIndicator: View = itemView.findViewById(R.id.onlineIndicator)
    private val networkIndicator: ImageView = itemView.findViewById(R.id.network_indicator)
    private val previewImage: ImageView = itemView.findViewById(R.id.preview)
    private val player: Player = ExoPlayer.Builder(instance).build()

    init {
        mediaView.player = player
    }

    override fun onBind(message: Message) {
        super.onBind(message)

        val fileName = message.voice?.url.toString()
        val mediaId = fileName.getJustFileName()

        (payload as Payload).lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            MapSituationFragmentViewModel.getInstance().mediaState.collectLatest { mediaState ->
                val mediaPath = mediaState[mediaId]

                if (mediaPath != null) {
                    val mediaItem = MediaItem.fromUri(File(mediaPath).toString())
                    withContext(Dispatchers.Main) {
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_READY) {
                                    val duration = player.duration

                                    tvDuration.text = duration.toFormattedDuration()
                                }
                            }
                        })
                        networkIndicator.visibility = View.GONE
                        previewImage.visibility = View.GONE
                        mediaView.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        networkIndicator.visibility = View.VISIBLE
                        tvDuration.visibility = View.GONE
                        MapSituationFragmentViewModel.getInstance()
                            .downloadMedia(fileName, mediaId)
                    }
                }
            }

        }
        tvTime.text = DateFormatter.format(message.createdAt, DateFormatter.Template.TIME)
        val isOnline = true
        onlineIndicator.setBackgroundResource(
            if (isOnline) R.drawable.shape_bubble_online else R.drawable.shape_bubble_offline
        )
    }

    interface OnClicksListener {
        fun onAvatarClick()
        fun onPlayButtonClick(
            url: String?,
            mpInterface: IMultimediaPlayer?,
            callback: OnCompleteCallback?
        )
    }

    class Payload {
        lateinit var lifecycleOwner: LifecycleOwner
        var clicksListener: OnClicksListener? = null
    }
}