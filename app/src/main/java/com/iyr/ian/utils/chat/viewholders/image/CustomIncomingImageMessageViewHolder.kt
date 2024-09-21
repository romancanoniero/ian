package com.iyr.ian.utils.chat.viewholders.image

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.iyr.ian.AppConstants.Companion.chatMessagesWidthPercent
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.viewholders.voice.CustomIncomingVoiceMessageViewHolder.OnClicksListener
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.stfalcon.chatkit.messages.MessageHolders.IncomingImageMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/*
* Created by troy379 on 05.04.17.
*/
class CustomIncomingImageMessageViewHolder
    (itemView: View, payload: Any?) : IncomingImageMessageViewHolder<Message>(itemView, payload) {
    private val onlineIndicator: View = itemView.findViewById(R.id.onlineIndicator)
    private val networkIndicator: View = itemView.findViewById(R.id.network_indicator)

    private val viewModel: MessagesInEventFragmentViewModel by lazy { MessagesInEventFragmentViewModel.getInstance() }

val image: PorterShapeImageView = itemView.findViewById(R.id.image)
    override fun onBind(message: Message) {
        try {
            super.onBind(message)
        } catch (e: Exception) {
            GlideApp.with(image).load(instance.getDrawable(R.drawable.ic_error)).into(image)
        }

        val dimsInfo : HashMap<String, Any> = MapSituationFragmentViewModel.getInstance().windowInfo.value!!
        val screenWidth : Int = (dimsInfo.get("screen_width") as Int)
        val itemWidth : Int = (screenWidth * chatMessagesWidthPercent).roundToInt()

        val layoutParams = image.layoutParams
        layoutParams.width = (itemWidth ) // Example: Set width to 48% of screen width
        layoutParams.height = (itemWidth ) // Example: Set height to 30% of screen height

        val mediaUrl = message?.image?.url ?: ""
        val mediaId = message?.image?.url?.getJustFileName() ?: ""
        // Mostrar imagen temporal mientras se descarga
        (payload as Payload).lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            MapSituationFragmentViewModel.getInstance().mediaState.collect { mediaState ->
                val mediaPath = mediaState[mediaId]
                if (mediaPath != null) {
                    withContext(Dispatchers.Main)
                    {
                        networkIndicator.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main)
                    {
                        networkIndicator.visibility = View.VISIBLE
                    }
                }
            }
        }

        val isOnline = message.user.isOnline
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online)
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline)
        }

    }

    class Payload {
        lateinit var lifecycleOwner: LifecycleOwner
        var clicksListener: OnClicksListener? = null
    }
}