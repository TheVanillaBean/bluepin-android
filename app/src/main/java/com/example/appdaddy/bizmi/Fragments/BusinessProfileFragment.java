package com.example.appdaddy.bizmi.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.EmailUpdateEvent;
import com.example.appdaddy.bizmi.POJO.PasswordResetEvent;
import com.example.appdaddy.bizmi.POJO.UploadFileEvent;
import com.example.appdaddy.bizmi.POJO.UploadProgressEvent;
import com.example.appdaddy.bizmi.POJO.UserCastEvent;
import com.example.appdaddy.bizmi.POJO.UserUpdateEvent;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.controller.BusinessMainActivity;
import com.example.appdaddy.bizmi.controller.EditLocationActivity;
import com.example.appdaddy.bizmi.controller.LoginActivity;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Dialog;
import com.example.appdaddy.bizmi.util.L;
import com.example.appdaddy.bizmi.util.Util;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageMetadata;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


@RuntimePermissions
public class BusinessProfileFragment extends Fragment implements BusinessMainActivity.OnListenerCallBacks{

    private BaseFragment.OnFragmentInteractionListener mListener;

    @BindView(R.id.business_image) ImageView mProfileImage;
    @BindView(R.id.upload_pic_btn) ImageButton mUploadPicBtn;
    @BindView(R.id.logout_btn) TextView mLogoutLabel;

    @BindView(R.id.name_label) TextView mNameLabel;
    @BindView(R.id.type_label) TextView mTypeLabel;
    @BindView(R.id.hours_label) TextView mHoursLabel;
    @BindView(R.id.desc_label) TextView mDeskLabel;
    @BindView(R.id.phone_label) TextView mPhoneLabel;
    @BindView(R.id.website_label) TextView mWebsiteLabel;
    @BindView(R.id.email_label) TextView mEmailLabel;
    @BindView(R.id.location_label) TextView mLocationLabel;
    @BindView(R.id.password_label) TextView mPasswordLabel;
    @BindView(R.id.contact_label) TextView mContactLabel;

    @BindView(R.id.name_btn) LinearLayout mNameBtn;
    @BindView(R.id.type_btn) LinearLayout mTypeBtn;
    @BindView(R.id.hours_btn) LinearLayout mHoursBtn;
    @BindView(R.id.desc_btn) LinearLayout mDeskBtn;
    @BindView(R.id.phone_btn) LinearLayout mPhoneBtn;
    @BindView(R.id.website_btn) LinearLayout mWebsiteBtn;
    @BindView(R.id.email_btn) LinearLayout mEmailBtn;
    @BindView(R.id.location_btn) LinearLayout mLocationBtn;
    @BindView(R.id.password_btn) LinearLayout mPasswordBtn;
    @BindView(R.id.contact_btn) LinearLayout mContactBtn;

    private User mCurrentUser;
    private MaterialDialog progressDialog;
    private MaterialDialog uploadProgressDialog;
    private boolean mProfileImageUpdated;

    private StringSignature mStringSignature;

    private GeoFire mGeoFire;
    private GeoLocation mLocation;

    public BusinessProfileFragment() {
    }

    public static BusinessProfileFragment newInstance() {
        return new BusinessProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_business_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);

        mProfileImageUpdated = true;
        mStringSignature = new StringSignature(Constants.SIG_NOT_UPDATED);
        progressDialog = Dialog.showProgressIndeterminateDialog(getActivity(), "Loading...", "Updating Profile...", false);
        uploadProgressDialog = Dialog.showProgressDeterminateDialog(getActivity(), "Loading...", "Uploading Profile Image...", false, true, 100);

        if(AuthService.getInstance().getCurrentUser() != null){
            User.castUser(AuthService.getInstance().getCurrentUser().getUid());
        }else{
            Dialog.showDialog(getActivity(), "Authentication Error", "Could not find user...", "Okay");
        }

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void populateDataFields() {
        mNameLabel.setText(mCurrentUser.getBusinessName());
        mTypeLabel.setText(mCurrentUser.getBusinessType());
        mHoursLabel.setText(mCurrentUser.getBusinessHours());
        mDeskLabel.setText(mCurrentUser.getBusinessDesc());
        mPhoneLabel.setText(mCurrentUser.getPhoneNumber());
        mWebsiteLabel.setText(mCurrentUser.getBusinessWebsite());
        mEmailLabel.setText(mCurrentUser.getEmail());
        if(mProfileImageUpdated){
            setProfileImage(getActivity(), Util.getImagePathPNG(mCurrentUser.getUUID()));
            toggleStringSignature();
            mProfileImageUpdated = false;
        }
    }

    private void toggleStringSignature(){
        StringSignature sig = new StringSignature(Constants.SIG_YES_UPDATED);
        if(mStringSignature.equals(sig)){
            mStringSignature = new StringSignature(Constants.SIG_NOT_UPDATED);
        }else{
            mStringSignature = new StringSignature(Constants.SIG_YES_UPDATED);
        }
    }

    private void setProfileImage(Context context, String path){
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(FBDataService.getInstance().profilePicsStorageRef().child(path))
                .placeholder(R.drawable.people_grey)
                .bitmapTransform(new RoundedCornersTransformation(context, 48, 0))
                .signature(mStringSignature)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mProfileImage);
    }

    private void showDialog(Context context, String title, String content, String hint, String preFill, final String KEY){
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .autoDismiss(true)
                .inputRange(5, 75)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(hint, preFill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        switch (KEY) {
                            case Constants.BUSINESS_NAME:
                                mCurrentUser.setBusinessName(input.toString());
                                break;
                            case Constants.BUSINESS_TYPE:
                                mCurrentUser.setBusinessType(input.toString());
                                break;
                            case Constants.BUSINESS_HOURS:
                                mCurrentUser.setBusinessHours(input.toString());
                                break;
                            case Constants.BUSINESS_DESC:
                                mCurrentUser.setBusinessDesc(input.toString());
                                break;
                            case Constants.PHONE_NUMBER:
                                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                                try {
                                    Phonenumber.PhoneNumber phoneProto = phoneUtil.parse(input.toString(), "US");
                                    mCurrentUser.setPhoneNumber(phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
                                } catch (NumberParseException e) {
                                    Toast.makeText(getActivity(), "Error Parsing Phone Number " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                break;
                            case Constants.BUSINESS_WEBSITE:
                                mCurrentUser.setBusinessWebsite(input.toString());
                                break;
                            case Constants.EMAIL:
                                progressDialog.show();
                                AuthService.getInstance().resetEmail(input.toString());
                                break;

                        }

                        FBDataService.getInstance().updateUser(mCurrentUser);

                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        if (event.getError() == null){
            mCurrentUser = event.getUser();
            populateDataFields();
            EasyImage.configuration(getActivity())
                    .setImagesFolderName("Choose Profile Picture")
                    .setCopyExistingPicturesToPublicLocation(true);
        }else{
            Dialog.showDialog(getActivity(), "Authentication Error", event.getError(), "Okay");
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserUpdateCallBack(UserUpdateEvent event) {
        if (event.getError() == null){
            populateDataFields();
        }else{
            Dialog.showDialog(getActivity(), "Error Updating User Information...", event.getError(), "Okay");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPasswordCallBack(PasswordResetEvent event) {
        progressDialog.dismiss();
        if (event.getError() == null){
            Dialog.showDialog(getActivity(), "Email Sent", "An email has been sent to " + mCurrentUser.getEmail() + " with a password reset link.", "Okay");
        }else{
            Dialog.showDialog(getActivity(), "Password Reset Error", event.getError(), "Okay");
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEmailCallBack(EmailUpdateEvent event) {
        progressDialog.dismiss();
        if (event.getError() == null){
            mCurrentUser.setEmail(event.getEmail());
            FBDataService.getInstance().updateUser(mCurrentUser);
        }else{
            Dialog.showDialog(getActivity(), "Email Reset Error", event.getError(), "Okay");
        }
    }

    @Override
    public void OnUploadProgressCallBack(UploadProgressEvent event) {
        int increment = (int) (event.getProgress() - uploadProgressDialog.getCurrentProgress());
        uploadProgressDialog.incrementProgress(increment);
    }

    @Override
    public void OnUploadFileCallBack(UploadFileEvent event) {
        if (event.getError() == null){
            Uri downloadUrl = event.getUploadTask().getMetadata().getDownloadUrl();
            mCurrentUser.setUserProfilePicLocation(downloadUrl.toString());

            if(!uploadProgressDialog.isCancelled()) {
                try {
                    Thread.sleep(1000);
                    uploadProgressDialog.dismiss();
                } catch (InterruptedException e) {
                    Toast.makeText(getActivity(), "Minor Error: Closing Dialog", Toast.LENGTH_SHORT).show();
                }
            }

            mProfileImageUpdated = true;
            FBDataService.getInstance().updateUser(mCurrentUser);
        }else{
            Toast.makeText(getActivity(), event.getError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void OnUserUpdateCallBack(UserUpdateEvent event) {
        if (event.getError() == null){
            populateDataFields();
        }else{
            Dialog.showDialog(getActivity(), "Error Updating User Information...", event.getError(), "Okay");
        }
    }

    @OnClick(R.id.upload_pic_btn)
    public void onUploadBtnPressed() {
        BusinessProfileFragmentPermissionsDispatcher.accessCameraAndGalleryWithCheck(this);
    }

    @OnClick(R.id.logout_btn)
    public void onLogoutBtnPressed() {
        AuthService.getInstance().getAuthInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.name_btn)
    public void onNameBtnPressed() {
        if(mCurrentUser != null){
            showDialog(getActivity(), "Edit User", "Update your Business Name", "Enter Business Name", mCurrentUser.getBusinessName(), Constants.BUSINESS_NAME);
        }
    }

    @OnClick(R.id.type_btn)
    public void onTypeBtnPressed() {
        if(mCurrentUser != null) {
            showDialog(getActivity(), "Edit User", "Update your Business Type", "Enter Business Type", mCurrentUser.getBusinessType(), Constants.BUSINESS_TYPE);
        }
    }

    @OnClick(R.id.hours_btn)
    public void onHoursBtnPressed() {
        if(mCurrentUser != null){
            showDialog(getActivity(), "Edit User", "Update your Business Hours", "Enter Business Hours", mCurrentUser.getBusinessHours(), Constants.BUSINESS_HOURS);
        }
    }

    @OnClick(R.id.desc_btn)
    public void onDescBtnPressed() {
        if(mCurrentUser != null){
            showDialog(getActivity(), "Edit User", "Update your Business Description", "Enter Business Description", mCurrentUser.getBusinessDesc(), Constants.BUSINESS_DESC);
        }
    }

    @OnClick(R.id.phone_btn)
    public void onPhoneNumberBtn() {
        if(mCurrentUser != null){
            showDialog(getActivity(), "Edit User", "Update your Business Phone Number", "Enter Business Phone Number", mCurrentUser.getPhoneNumber(), Constants.PHONE_NUMBER);
        }
    }

    @OnClick(R.id.website_btn)
    public void onWebsiteBtnPressed() {
        if(mCurrentUser != null){
            showDialog(getActivity(), "Edit User", "Update your Business Website", "Enter Business Website", mCurrentUser.getBusinessWebsite(), Constants.BUSINESS_WEBSITE);
        }
    }

    @OnClick(R.id.email_btn)
    public void onEmailBtnPressed() {
        if (mCurrentUser != null) {
            showDialog(getActivity(), "Edit User", "Update your Email", "Enter Email", mCurrentUser.getEmail(), Constants.EMAIL);
        }
    }

    @OnClick(R.id.location_btn)
    public void onLocationBtnPressed() {
        showMapActivity();
    }

    @OnClick(R.id.password_btn)
    public void onPasswordBtnPressed() {
        new MaterialDialog.Builder(getActivity())
                .title("Reset Password")
                .content("Do you want to reset your password?")
                .positiveText("Yes")
                .negativeText("No")
                .autoDismiss(true)
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        progressDialog.show();
                        AuthService.getInstance().resetPassword(mCurrentUser.getEmail());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick(R.id.contact_btn)
    public void onContactBtnPressed() {
    }

    public void showMapActivity() {

        Bundle bundle = new Bundle();
        Parcelable wrapped = Parcels.wrap(mCurrentUser);
        bundle.putParcelable(Constants.EXTRA_USER_PARCEL, wrapped);

        Intent intent = new Intent(getActivity(), EditLocationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();

                uploadProgressDialog.show();
                FBDataService.getInstance().uploadFile(FBDataService.getInstance().profilePicsStorageRef().child(mCurrentUser.getUUID() + ".png"), imageFile, metadata);

            }

            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(getActivity(), "Image Error " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getActivity());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BusinessMainActivity a;
        if (context instanceof Activity){
            a= (BusinessMainActivity) context;
            a.setListener(this);
        }
    }

    @Override
    public void onDestroy() {
        EasyImage.clearConfiguration(getActivity());
        super.onDestroy();
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void accessCameraAndGallery() {
        EasyImage.openChooserWithGallery(this, "Select Source", 0);
    }

    @OnShowRationale({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForCamera(final PermissionRequest request) {
        showRationaleDialog("Camera Access", request);
    }

    @OnPermissionDenied({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedForCamera() {
        Toast.makeText(getActivity(), "Camera Permission Denied", Toast.LENGTH_SHORT).show();
        EasyImage.openGallery(this, 0);
    }

    @OnNeverAskAgain({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showNeverAskForCamera() {
        Toast.makeText(getActivity(), "Camera Permission Denied Always", Toast.LENGTH_SHORT).show();
        EasyImage.openGallery(this, 0);
    }

    private void showRationaleDialog(String messageResId, final PermissionRequest request) {

        new MaterialDialog.Builder(getActivity())
                .title(messageResId)
                .content("Bizmi needs to access your camera so you can choose a profile picture.")
                .positiveText("Yes")
                .negativeText("No")
                .autoDismiss(false)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        request.proceed();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        request.cancel();
                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BusinessProfileFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
