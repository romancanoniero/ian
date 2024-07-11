package com.iyr.ian.utils.chat.interfaces.user_avatar.click_message;

import com.stfalcon.chatkit.commons.models.IMessage;

public interface OnMessageClickListener<MESSAGE extends IMessage> {
    void onMessageClick(MESSAGE message);
}
