package com.iyr.ian.utils.chat.viewholders;

import android.view.View;

import com.iyr.ian.utils.chat.models.Message;
import com.stfalcon.chatkit.messages.MessageHolders;

public class CustomTextMessageHandler extends MessageHolders.IncomingTextMessageViewHolder<Message> {


    public CustomTextMessageHandler(View itemView, Object payload) {
        super(itemView, payload);
    }


    @Override
    public void onBind(Message message) {
        super.onBind(message);
    }


}