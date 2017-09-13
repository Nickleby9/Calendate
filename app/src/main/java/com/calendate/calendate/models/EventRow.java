package com.calendate.calendate.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.calendate.calendate.utils.MyUtils;

import org.joda.time.LocalDateTime;


public class EventRow implements Parcelable {

    String eventUID;
    String title;
    String date;
    String btnId;
    String time;

    public EventRow() {
    }

    public EventRow(String eventUID, String title, LocalDateTime date, int hours, int minutes, String btnId) {
        this.eventUID = eventUID;
        this.title = title;
        this.date = date.toString(MyUtils.dateForamt);
        if (minutes < 10)
            this.time = hours + ":0" + minutes;
        else
            this.time = hours + ":" + minutes;
        this.btnId = btnId;
    }

    public String getEventUID() {
        return eventUID;
    }

    public void setEventUID(String eventUID) {
        this.eventUID = eventUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBtnId() {
        return btnId;
    }

    public void setBtnId(String btnId) {
        this.btnId = btnId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.eventUID);
        dest.writeString(this.title);
        dest.writeString(this.date);
    }

    protected EventRow(Parcel in) {
        this.eventUID = in.readString();
        this.title = in.readString();
        this.date = in.readString();
    }

    public static final Parcelable.Creator<EventRow> CREATOR = new Parcelable.Creator<EventRow>() {
        @Override
        public EventRow createFromParcel(Parcel source) {
            return new EventRow(source);
        }

        @Override
        public EventRow[] newArray(int size) {
            return new EventRow[size];
        }
    };
}