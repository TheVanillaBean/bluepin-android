package com.bluepinapp.bluepin.holder;

import android.view.View;

import com.bluepinapp.bluepin.model.MessageDialog;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<MessageDialog> {


    public CustomIncomingTextMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(MessageDialog message) {
        super.onBind(message);

        if (time != null) {
            time.setText(DateFormatter.format(message.getCreatedAt(), "h:mm a"));
        }

    }
}
