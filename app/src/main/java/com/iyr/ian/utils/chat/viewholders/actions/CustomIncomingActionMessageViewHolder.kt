package com.iyr.ian.utils.chat.viewholders.actions

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.SpeedMessageActions
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
    private val ivAction: ImageView
    private val onlineIndicator: View

    init {

        tvMessage = itemView.findViewById(R.id.message)
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
        tvTime = itemView.findViewById(R.id.messageTime)
        ivAction = itemView.findViewById(R.id.action_image)

    }


    override fun onBind(message: Message?) {
        super.onBind(message)


        var messageText = ""
        when (SpeedMessageActions.valueOf(message?.action?.actionType ?: "")) {
            SpeedMessageActions.GOING -> {
                messageText = String.format(instance.getString(R.string.im_going_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_man_running)
            }
            SpeedMessageActions.NOT_GOING -> {
                messageText = String.format(instance.getString(R.string.im_not_going_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_man_standing_up)
            }
            SpeedMessageActions.CALLED ->
            {
                messageText = String.format(instance.getString(R.string.already_called_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_phone_call_ok_by_smashicons)
            }
            SpeedMessageActions.NOT_CALLED -> {
                messageText = String.format(instance.getString(R.string.not_called_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_phone_call_guess)
            }
            SpeedMessageActions.IM_THERE -> {
                messageText = String.format(instance.getString(R.string.im_in_place_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_arrival_by_stockes_02)
            }
            SpeedMessageActions.NOT_IN_THERE -> {
                messageText = String.format(instance.getString(R.string.not_in_place_message), message?.user?.name.toString())
                ivAction.setImageResource(R.drawable.ic_error)
            }

        }

        tvMessage.text = messageText
        tvTime.text = DateFormatter.format(message?.createdAt, DateFormatter.Template.TIME)
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
        /*
        tvMessage.text = message?.text
        tvTime.text = DateFormatter.format(message?.createdAt, DateFormatter.Template.TIME)

         */
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

