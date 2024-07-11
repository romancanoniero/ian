package com.iyr.ian.utils.chat.viewholders.text;

import android.view.View;

import androidx.annotation.Nullable;

import com.iyr.ian.R;
import com.iyr.ian.utils.chat.models.Message;
import com.stfalcon.chatkit.messages.MessageHolders;


public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {

    private final View onlineIndicator;

    public CustomIncomingTextMessageViewHolder(View itemView, @Nullable Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(view -> {
            if (payload != null && payload.avatarClickListener != null) {
                payload.avatarClickListener.onAvatarClick();
            }
        });
    }

    public interface OnAvatarClickListener {
        void onAvatarClick();
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }
}
