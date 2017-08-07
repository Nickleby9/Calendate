package com.calendate.calendate.models;

import com.google.firebase.auth.FirebaseUser;

public class User {

    String uid;
    String username;
    String email;
    String profileImage = "http://www.freeiconspng.com/uploads/profile-icon-9.png";

    public User() {
    }

    public User(FirebaseUser user) {
        this.uid = user.getUid();
        this.username = user.getDisplayName();
        this.email = user.getEmail();
        if (user.getPhotoUrl() != null) {
            profileImage = user.getPhotoUrl().toString();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        if (profileImage != null)
            this.profileImage = profileImage;
    }
}
