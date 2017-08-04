package com.calendate.calendate.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable {

    String friendUid;
    String friendUsername;
    String friendEmail;
    boolean approved;
    String senderUsername;
    String senderUid;
    String senderEmail;

    public Friend() {
    }

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String riendUid) {
        this.friendUid = riendUid;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.friendUid);
        dest.writeString(this.friendUsername);
        dest.writeString(this.friendEmail);
        dest.writeByte(this.approved ? (byte) 1 : (byte) 0);
        dest.writeString(this.senderUsername);
        dest.writeString(this.senderUid);
        dest.writeString(this.senderEmail);
    }

    protected Friend(Parcel in) {
        this.friendUid = in.readString();
        this.friendUsername = in.readString();
        this.friendEmail = in.readString();
        this.approved = in.readByte() != 0;
        this.senderUsername = in.readString();
        this.senderUid = in.readString();
        this.senderEmail = in.readString();
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel source) {
            return new Friend(source);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
