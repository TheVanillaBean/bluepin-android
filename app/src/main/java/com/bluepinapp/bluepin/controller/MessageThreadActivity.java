package com.bluepinapp.bluepin.controller;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bluepinapp.bluepin.DataService.AuthService;
import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.R;
import com.bluepinapp.bluepin.fixtures.MessagesFixtures;
import com.bluepinapp.bluepin.holder.CustomIncomingTextMessageViewHolder;
import com.bluepinapp.bluepin.holder.CustomOutcomingTextMessageViewHolder;
import com.bluepinapp.bluepin.model.DialogUser;
import com.bluepinapp.bluepin.model.Message;
import com.bluepinapp.bluepin.model.MessageDialog;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.L;
import com.bluepinapp.bluepin.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MessageThreadActivity extends AppCompatActivity implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener,
        DateFormatter.Formatter {

    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<MessageDialog> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;
    private MessagesList messagesList;

    private String mChannelID;
    private String mChattingWith;
    private String mChattingWithID;
    private int iterator = 1;
    private ArrayList<String> mAllMessageIDSInChat = new ArrayList<>();
    private ArrayList<Message> mAllMessagesInChat = new ArrayList<>();
    private ArrayList<MessageDialog> mAllMessageDialogsInChat = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(MessageThreadActivity.this).load(url).into(imageView);
            }
        };

        messagesList = (MessagesList) findViewById(R.id.messagesList);
        loadData();

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveAllChatIDS(new Util.completionInterface() {
            @Override
            public void onComplete(String error) {
                loadMessages(iterator);
                observeNewMessages();
            }
        });

        //        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);

    }

    private void loadData(){

        if(getIntent().getStringExtra(Constants.EXTRA_CHANNEL_ID) != null){
            mChannelID = getIntent().getStringExtra(Constants.EXTRA_CHANNEL_ID);
            mChattingWith = getIntent().getStringExtra(Constants.EXTRA_CHATTING_WITH);
            mChattingWithID = getIntent().getStringExtra(Constants.EXTRA_CHATTING_WITH_ID);
            senderId = AuthService.getInstance().getCurrentUser().getUid();
            L.m(senderId + " userID");
            initAdapter();
        }

    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
//        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
//            loadMessages();
//        }
    }

    @Override
    public void onSelectionChanged(int count) {

    }

//    protected void loadMessages() {
//        new Handler().postDelayed(new Runnable() { //imitation of internet connection
//            @Override
//            public void run() {
//                ArrayList<MessageDialog> messages = MessagesFixtures.getMessages(lastLoadedDate);
//                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
//                messagesAdapter.addToEnd(messages, false);
//            }
//        }, 1000);
//    }


    @Override
    public boolean onSubmit(final CharSequence input) {

        final String currentUserID = AuthService.getInstance().getCurrentUser().getUid();
        final String key = FBDataService.getInstance().messagesRef().push().getKey();

        Message message = new Message(
                Constants.MESSAGE_TEXT_TYPE,
                key,
                currentUserID,
                mChattingWithID,
                mChannelID);

        FBDataService.getInstance().messagesRef().child(key).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    byte[] data = input.toString().getBytes(Charset.forName("UTF-8"));

                }else{
                    Toast.makeText(MessageThreadActivity.this, "Failed to create message. Please try again... ", Toast.LENGTH_LONG).show();

                }
            }
        });


        messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, "h:mm a");
        } else if (DateFormatter.isYesterday(date)) {
            return "Yesterday";
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    private void initAdapter() {
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder.class,
                        R.layout.item_incoming_text_message)
                .setOutcomingTextConfig(
                        CustomOutcomingTextMessageViewHolder.class,
                        R.layout.item_outcoming_text_message);

        messagesAdapter = new MessagesListAdapter<>(senderId, holdersConfig, null);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.setDateHeadersFormatter(this);
        messagesList.setAdapter(messagesAdapter);
    }

    private void retrieveAllChatIDS(final Util.completionInterface completionInterface){

        FBDataService.getInstance().channelsRef().child(mChannelID).orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageIDSSnapshot: dataSnapshot.getChildren()) {
                    mAllMessageIDSInChat.add(messageIDSSnapshot.getKey());
                }

                completionInterface.onComplete(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completionInterface.onComplete("Error " + databaseError.getMessage());
            }
        });
    }

    private void observeNewMessages(){

        FBDataService.getInstance().channelsRef().child(mChannelID).limitToLast(5).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String messageID = dataSnapshot.getKey();

                if(!mAllMessageIDSInChat.contains(messageID)){

                    convertMessageIDToMessageModel(messageID, new Util.completionInterfaceMessageDialog() {
                        @Override
                        public void onComplete(String error, final MessageDialog message) {
                            L.m("New Message");

                            messagesAdapter.addToStart(message, true);

                        }
                    });

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages(int iteratorStart){

        int iterator = iteratorStart;
        int iteratorEnd = iterator + 19;

        while(iterator < iteratorEnd){

            L.m(iterator + " iterator");
            L.m((mAllMessageIDSInChat.size()) + "  sizeofmessages");
            L.m((mAllMessageIDSInChat.size()  - iterator) + " messageIndex");

            String messageID = mAllMessageIDSInChat.get(mAllMessageIDSInChat.size() - iterator);

            L.m(messageID + " messageId");
            iterator++;
            this.iterator++;

            if(iterator > mAllMessageIDSInChat.size()){
                iteratorEnd = 0;
            }

            convertMessageIDToMessageModel(messageID, new Util.completionInterfaceMessageDialog() {
                @Override
                public void onComplete(String error, final MessageDialog message) {
                    L.m("Called In");

                    if(mAllMessageDialogsInChat.size() == mAllMessageIDSInChat.size()){
                        Collections.sort(mAllMessageDialogsInChat, new CustomComparator());

                        for(MessageDialog messageDialog: mAllMessageDialogsInChat){
                            messagesAdapter.addToStart(messageDialog, false);
                        }

                    }
                }
            });

        }

        L.m("Called Out");


    }

    private void convertMessageIDToMessageModel(String messageID, final Util.completionInterfaceMessageDialog completionInterface){

        Message.castMessage(messageID, new Util.completionInterfaceMessage() {
            @Override
            public void onComplete(String error, Message message) {
                MessageDialog messageDialog = convertMessageToMessageDialog(message);
                mAllMessagesInChat.add(message);
                mAllMessageDialogsInChat.add(messageDialog);
                completionInterface.onComplete(null, messageDialog);
            }
        });

    }

    private MessageDialog convertMessageToMessageDialog(Message message){
        Date date = new Date(message.getTimeStamp());
        DialogUser userDialog = new DialogUser(message.getSenderUID(), getName(message.getSenderUserObj()), null, false);
        L.m(message.getMessageID() + "messageID ---j");
        return new MessageDialog(message.getMessageID(), userDialog, message.getMessageData(), date);
    }
    private String getName(User user){
        if(user.getUserType().equals(Constants.USER_BUSINESS_TYPE)){
            return user.getBusinessName();
        }else{
            return user.getFullName();
        }
    }

    public class CustomComparator implements Comparator<MessageDialog> {
        @Override
        public int compare(MessageDialog o1, MessageDialog o2) {
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        }
    }

}
