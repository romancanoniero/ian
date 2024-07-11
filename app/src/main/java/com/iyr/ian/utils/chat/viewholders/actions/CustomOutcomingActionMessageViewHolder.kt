package com.iyr.ian.utils.chat.viewholders.actions

import android.view.View
import android.widget.TextView
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.SpeedMessageActions
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder

/*
 * Created by troy379 on 05.04.17.
 */
class CustomOutcomingActionMessageViewHolder(itemView: View, payload: Any?) :
    OutcomingTextMessageViewHolder<Message?>(itemView, payload) {
    private val tvTime: TextView
    private val tvMessage: TextView


    init {

        tvMessage = itemView.findViewById(R.id.message)
        tvTime = itemView.findViewById(R.id.messageTime)
    }



    override fun onBind(message: Message?) {
        super.onBind(message)
        var messageText = ""
        when (SpeedMessageActions.valueOf(message?.action?.actionType ?: "")) {
            SpeedMessageActions.GOING -> {
                messageText = String.format(instance.getString(R.string.im_going_message), message?.user?.name.toString())
            }
            SpeedMessageActions.NOT_GOING -> {
                messageText = String.format(instance.getString(R.string.im_not_going_message), message?.user?.name.toString())
            }
            SpeedMessageActions.CALLED ->
            {
                messageText = String.format(instance.getString(R.string.already_called_message), message?.user?.name.toString())
            }
            SpeedMessageActions.NOT_CALLED -> {
                messageText = String.format(instance.getString(R.string.not_called_message), message?.user?.name.toString())
            }
            SpeedMessageActions.IM_THERE -> {
                messageText = String.format(instance.getString(R.string.im_in_place_message), message?.user?.name.toString())
            }
            SpeedMessageActions.NOT_IN_THERE -> {
                messageText = String.format(instance.getString(R.string.not_in_place_message), message?.user?.name.toString())
            }

        }

        tvMessage.text = messageText
        tvTime.text = DateFormatter.format(message?.createdAt, DateFormatter.Template.TIME)
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