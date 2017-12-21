package com.calendate.calendate.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Alert implements Parcelable {

    int id;
    int count;
    int kind;
    boolean visible;

    public Alert() {
    }

    public Alert(int id, int count, int kind, boolean visible) {
        this.id = id;
        this.count = count;
        this.kind = kind;
        this.visible = visible;
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public int getKind() {
        return kind;
    }
    public void setKind(int kind) {
        this.kind = kind;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.count);
        dest.writeInt(this.kind);
        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
    }

    protected Alert(Parcel in) {
        this.id = in.readInt();
        this.count = in.readInt();
        this.kind = in.readInt();
        this.visible = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Alert> CREATOR = new Parcelable.Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel source) {
            return new Alert(source);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };
}
