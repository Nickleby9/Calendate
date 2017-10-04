package com.calendate.calendate.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.calendate.calendate.utils.MyUtils;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;


public class Event implements Parcelable {

    String eventUID;
    String title;
    String description;
    String date;
    ArrayList<Alert> alerts;
    String time;
    int repeatPos;
    String btnId;
    boolean own;
    String creator;

    public Event() {

    }

    public Event(String title, String description, LocalDateTime date, ArrayList<Alert> alerts, int hours, int minutes, int repeatPos, String eventUID, String btnId, boolean own, String creator) {
        this.title = title;
        this.description = description;
        this.date = date.toString(MyUtils.dateForamt);
        this.alerts = alerts;
        if (minutes < 10)
            this.time = hours + ":0" + minutes;
        else
            this.time = hours + ":" + minutes;
        this.repeatPos = repeatPos;
        this.eventUID = eventUID;
        this.btnId = btnId;
        this.own = own;
        this.creator = creator;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(ArrayList<Alert> alerts) {
        this.alerts = alerts;
    }

    public int getRepeatPos() {
        return repeatPos;
    }

    public void setRepeatPos(int repeatPos) {
        this.repeatPos = repeatPos;
    }

    public String getEventUID() {
        return eventUID;
    }

    public void setEventUID(String eventUID) {
        this.eventUID = eventUID;
    }

    public String getBtnId() {
        return btnId;
    }

    public void setBtnId(String btnId) {
        this.btnId = btnId;
    }

    public boolean isOwn() {
        return own;
    }

    public void setOwn(boolean own) {
        this.own = own;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.eventUID);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.date);
        dest.writeList(this.alerts);
        dest.writeString(this.time);
        dest.writeInt(this.repeatPos);
        dest.writeString(this.btnId);
        dest.writeByte(this.own ? (byte) 1 : (byte) 0);
        dest.writeString(this.creator);
    }

    protected Event(Parcel in) {
        this.eventUID = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.date = in.readString();
        this.alerts = new ArrayList<Alert>();
        in.readList(this.alerts, Alert.class.getClassLoader());
        this.time = in.readString();
        this.repeatPos = in.readInt();
        this.btnId = in.readString();
        this.own = in.readByte() != 0;
        this.creator = in.readString();
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
