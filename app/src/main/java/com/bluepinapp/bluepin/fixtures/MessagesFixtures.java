package com.bluepinapp.bluepin.fixtures;

import com.bluepinapp.bluepin.model.DialogUser;
import com.bluepinapp.bluepin.model.MessageDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by troy379 on 12.12.16.
 */
public final class MessagesFixtures extends FixturesData {
    private MessagesFixtures() {
        throw new AssertionError();
    }

    public static MessageDialog getImageMessage() {
        MessageDialog message = new MessageDialog(getRandomId(), getUser(), null);
        message.setImage(new MessageDialog.Image(getRandomImage()));
        return message;
    }

    public static MessageDialog getVoiceMessage() {
        MessageDialog message = new MessageDialog(getRandomId(), getUser(), null);
        message.setVoice(new MessageDialog.Voice("http://example.com", rnd.nextInt(200) + 30));
        return message;
    }

    public static MessageDialog getTextMessage() {
        return getTextMessage(getRandomMessage());
    }

    public static MessageDialog getTextMessage(String text) {
        return new MessageDialog(getRandomId(), getUser(), text);
    }

    public static ArrayList<MessageDialog> getMessages(Date startDate) {
        ArrayList<MessageDialog> messages = new ArrayList<>();
        for (int i = 0; i < 10/*days count*/; i++) {
            int countPerDay = rnd.nextInt(5) + 1;

            for (int j = 0; j < countPerDay; j++) {
                MessageDialog message;
                if (i % 2 == 0 && j % 3 == 0) {
                    message = getImageMessage();
                } else {
                    message = getTextMessage();
                }

                Calendar calendar = Calendar.getInstance();
                if (startDate != null) calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

                message.setCreatedAt(calendar.getTime());
                messages.add(message);
            }
        }
        return messages;
    }

    private static DialogUser getUser() {
        boolean even = rnd.nextBoolean();
        return new DialogUser(
                even ? "0" : "1",
                even ? names.get(0) : names.get(1),
                even ? avatars.get(0) : avatars.get(1),
                true);
    }
}
