package com.products.safetyfirst.modelhelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by rishabh on 14/10/17.
 */

public class UserHelper {

    private static UserHelper instance = new UserHelper();

    public static UserHelper getInstance() {
        return instance;
    }

    private UserHelper() {
    }

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public boolean isSignedIn() {
        return (auth.getCurrentUser() != null);
    }

    public FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    public String getUserId() {
        return auth.getCurrentUser().getUid();
    }

    public String getUserImgUrl() {
        if(auth.getCurrentUser().getPhotoUrl() != null) {
            return auth.getCurrentUser().getPhotoUrl().toString();
        }
        return "";  // return empty non-null string to prevent error
    }
}
