package com.bluepinapp.bluepin.DataService;

import android.support.annotation.NonNull;

import com.bluepinapp.bluepin.POJO.AuthEvent;
import com.bluepinapp.bluepin.POJO.EmailUpdateEvent;
import com.bluepinapp.bluepin.POJO.PasswordResetEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Alex on 1/21/2017.
 */

public class AuthService {

    private static final AuthService _instance = new AuthService();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static AuthService getInstance() {
        return _instance;
    }

    public FirebaseAuth getAuthInstance() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user;
        } else {
            return null;
        }
    }

    public void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            EventBus.getDefault().post(new AuthEvent(null, task.getResult().getUser()));
                        }else{
                            EventBus.getDefault().post(new AuthEvent(task.getException().getMessage(), null));
                        }
                    }
                });
    }

    public void signUp(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            EventBus.getDefault().post(new AuthEvent(null, task.getResult().getUser()));
                        }else{
                            EventBus.getDefault().post(new AuthEvent(task.getException().getMessage(), null));
                        }
                    }
                });
    }

    public void resetPassword(String email) {

       mAuth.sendPasswordResetEmail(email)
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()) {
                            EventBus.getDefault().post(new PasswordResetEvent(null));
                       }else{
                           EventBus.getDefault().post(new PasswordResetEvent(task.getException().getMessage()));
                       }

                   }
               });
    }

    public void resetEmail(final String email) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){

            user.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                EventBus.getDefault().post(new EmailUpdateEvent(null, email));
                            }else{
                                EventBus.getDefault().post(new EmailUpdateEvent(task.getException().getMessage(), null));
                            }
                        }
                    });

        }

    }

    private static void handleFirebaseError(Exception exception){
    }

    public AuthService(){
    }

}
