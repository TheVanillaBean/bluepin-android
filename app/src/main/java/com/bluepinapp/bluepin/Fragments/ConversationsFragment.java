package com.bluepinapp.bluepin.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bluepinapp.bluepin.DataService.AuthService;
import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.R;
import com.bluepinapp.bluepin.controller.MessageThreadActivity;
import com.bluepinapp.bluepin.model.Dialog;
import com.bluepinapp.bluepin.model.DialogUser;
import com.bluepinapp.bluepin.model.Message;
import com.bluepinapp.bluepin.model.MessageDialog;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.L;
import com.bluepinapp.bluepin.util.Util;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class ConversationsFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<Dialog>, DialogsListAdapter.OnDialogLongClickListener<Dialog>, DateFormatter.Formatter {

    protected ImageLoader imageLoader;
    protected DialogsListAdapter<Dialog> dialogsAdapter;
    private DialogsList dialogsList;
    private List<String> mChannelIDS = new ArrayList<>();
    private List<Message> mLastMessages = new ArrayList<>();
    private List<Dialog> mDialogs = new ArrayList<>();


    public ConversationsFragment() {
        // Required empty public constructor
    }

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

      @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {

                L.m(url + " onImage");

                Glide.with(getActivity())
                        .using(new FirebaseImageLoader())
                        .load(FBDataService.getInstance().profilePicsStorageRef().child(url))
                        .placeholder(R.drawable.people_grey)
                        .bitmapTransform(new RoundedCornersTransformation(getActivity(), 48, 0))
                        .into(imageView);

            }
        };

        dialogsList = (DialogsList) getActivity().findViewById(R.id.dialogsList);
        initAdapter();
    }

    @Override
    public void onDialogClick(Dialog dialog) {
        Intent intent = new Intent(getActivity(), MessageThreadActivity.class);
        intent.putExtra(Constants.EXTRA_CHANNEL_ID, dialog.getId());
        intent.putExtra(Constants.EXTRA_CHATTING_WITH, getChattingName(dialog.getUsers()));
        intent.putExtra(Constants.EXTRA_CHATTING_WITH_ID, getChattingID(dialog.getUsers()));
        startActivity(intent);
    }

    @Override
    public void onDialogLongClick(Dialog dialog) {

    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, "h:mm a");
        } else if (DateFormatter.isYesterday(date)) {
            return "Yesterday";
        } else if (DateFormatter.isCurrentYear(date)) {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    private void initAdapter() {
        dialogsAdapter = new DialogsListAdapter<>(imageLoader);
//        dialogsAdapter.setItems(DialogsFixtures.getDialogs());

        dialogsAdapter.setOnDialogClickListener(this);
        dialogsAdapter.setOnDialogLongClickListener(this);
        dialogsAdapter.setDatesFormatter(this);

        dialogsList.setAdapter(dialogsAdapter);

        observeChannelsForUser(AuthService.getInstance().getCurrentUser().getUid());
    }

    private void convertLastMessageToMessageModel(final String channelName, final Util.completionInterfaceDialog completionInterfaceDialog){
        FBDataService.getInstance().channelsRef().child(channelName).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){


                    String id = "";
                    for (DataSnapshot messageSnaps: dataSnapshot.getChildren()) {
                        id = messageSnaps.getKey();
                    }

                    final String messageID = id;
                    L.m("convertLastMessage --" + messageID);
                    Message.castMessage(messageID, new Util.completionInterfaceMessage() {
                        @Override
                        public void onComplete(String error, Message message) {

                            L.m("OnComplete --");

                            ArrayList<DialogUser> userDialogs = new ArrayList<>();

                            L.m(message.getSenderUserObj().getUserProfilePicLocation());
                            DialogUser senderUser = new DialogUser(message.getSenderUserObj().getUUID(), getName(message.getSenderUserObj()),  Util.getImagePathPNG(message.getSenderUID()), false);
                            DialogUser recipientUser = new DialogUser(message.getRecipientUserObj().getUUID(), getName(message.getRecipientUserObj()),  Util.getImagePathPNG(message.getRecipientUID()), false);

                            userDialogs.add(senderUser);
                            userDialogs.add(recipientUser);

                            L.m(message.getTimeStamp() + "gf");
                            MessageDialog senderMessageDialog = new MessageDialog(message.getMessageID(), senderUser, message.getMessageData(), new Date(message.getTimeStamp()));

                            Dialog dialog = new Dialog(channelName, recipientUser.getName(), Util.getImagePathPNG(message.getRecipientUID()), userDialogs, senderMessageDialog, 0);

                            completionInterfaceDialog.onComplete(null, dialog);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getName(User user){
        if(user.getUserType().equals(Constants.USER_BUSINESS_TYPE)){
            return user.getBusinessName();
        }else{
            return user.getFullName();
        }
    }

    private String getChattingName(ArrayList<DialogUser> dialogUsers){
        if(dialogUsers.get(0).getId().equals(AuthService.getInstance().getCurrentUser().getUid())){
            return dialogUsers.get(1).getName();
        }else{
            return dialogUsers.get(0).getName();
        }
    }

    private String getChattingID(ArrayList<DialogUser> dialogUsers){
        if(dialogUsers.get(0).getId().equals(AuthService.getInstance().getCurrentUser().getUid())){
            return dialogUsers.get(1).getId();
        }else{
            return dialogUsers.get(0).getId();
        }
    }

    private void observeChannelsForUser(String uuid){
        FBDataService.getInstance().userChannelsRef().child(uuid).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()){
                    L.m("observed --");
                    String channelName = dataSnapshot.getKey();
                    mChannelIDS.add(channelName);
                    convertLastMessageToMessageModel(channelName, new Util.completionInterfaceDialog() {
                        @Override
                        public void onComplete(String error, Dialog dialog) {
                            dialogsAdapter.addItem(dialog);
                        }
                    });
                }
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    L.m("observed --");
                    String channelName = dataSnapshot.getKey();
                    convertLastMessageToMessageModel(channelName, new Util.completionInterfaceDialog() {
                        @Override
                        public void onComplete(String error, Dialog dialog) {
                            dialogsAdapter.updateItemById(dialog);
                        }
                    });
                }
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


}
