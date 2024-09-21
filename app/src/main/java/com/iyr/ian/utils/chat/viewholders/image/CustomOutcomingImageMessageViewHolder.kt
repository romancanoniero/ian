package com.iyr.ian.utils.chat.viewholders.image

import android.util.Pair
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.iyr.ian.AppConstants.Companion.chatMessagesWidthPercent
import com.iyr.ian.R
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.viewholders.voice.CustomIncomingVoiceMessageViewHolder.OnClicksListener
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingImageMessageViewHolder
import kotlin.math.roundToInt

/*
 * Created by troy379 on 05.04.17.
 */
class CustomOutcomingImageMessageViewHolder
    (itemView: View?, payload: Any?) :
    OutcomingImageMessageViewHolder<Message?>(itemView, payload) {

    private val viewModel: MessagesInEventFragmentViewModel by lazy { MessagesInEventFragmentViewModel.getInstance() }

    val image : PorterShapeImageView = itemView!!.findViewById(com.iyr.ian.R.id.image)
    private val networkIndicator: View = itemView!!.findViewById(R.id.network_indicator)

    override fun onBind(message: Message?) {
        super.onBind(message)

        val dimsInfo : HashMap<String, Any> = MapSituationFragmentViewModel.getInstance().windowInfo.value!!
        val screenWidth : Int = (dimsInfo.get("screen_width") as Int)
        val itemWidth : Int = (screenWidth * chatMessagesWidthPercent).roundToInt()

        val layoutParams = image.layoutParams
        layoutParams.width = (itemWidth ) // Example: Set width to 48% of screen width
        layoutParams.height = (itemWidth ) // Example: Set height to 30% of screen height

        networkIndicator.visibility = View.GONE
        time.text = time.text
    }

    //Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
    override fun getPayloadForImageLoader(message: Message?): Any {
        //For example you can pass size of placeholder before loading
        return Pair(100, 100)
    }


    class Payload {
        lateinit var lifecycleOwner: LifecycleOwner
        var clicksListener: OnClicksListener? = null
    }
}