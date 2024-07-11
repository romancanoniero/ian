package com.iyr.ian.utils.chat.viewholders.actions

import android.view.View
import android.widget.TextView
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.multimedia.MediaPlayerInterface
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder


/*
 * Created by troy379 on 05.04.17.
 */
class CustomIncomingActionMessageViewHolder(itemView: View, payload: Any?) :
    IncomingTextMessageViewHolder<Message?>(itemView, payload) {
    private val tvMessage: TextView
    private val tvTime: TextView
    private val onlineIndicator: View

    init {

        tvMessage = itemView.findViewById(R.id.message)
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
        tvTime = itemView.findViewById(R.id.messageTime)

    }


    override fun onBind(message: Message?) {
        super.onBind(message)

/*
        var localImage =
            AppClass.instance.getBitmapFromVectorDrawable(R.drawable.action_going_run_24)
        imgAction.setImageBitmap(localImage)
*/

        val isOnline = message?.user?.isOnline ?: false
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online)
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline)
        }
        tvMessage.text = message?.text
        tvTime.text = DateFormatter.format(message?.createdAt, DateFormatter.Template.TIME)
    }


    interface OnClicksListener {
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

