package com.iyr.ian.utils.chat.viewholders.image;

import android.view.View;


import com.iyr.ian.R;
import com.iyr.ian.app.AppClass;
import com.iyr.ian.glide.GlideApp;
import com.iyr.ian.utils.chat.models.Message;
import com.stfalcon.chatkit.messages.MessageHolders;

/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<Message> {

    private final View onlineIndicator;

    public CustomIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
    }

    @Override
    public void onBind(Message message) {
      try {
          super.onBind(message);
      }
      catch (Exception e){

          GlideApp.with(image).load(AppClass.getInstance().getDrawable(R.drawable.ic_error)).into(image);

      }
        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }
    }
}