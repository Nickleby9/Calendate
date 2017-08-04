package com.calendate.calendate.models;

import android.support.design.widget.FloatingActionButton;

public class AlertItem {

    FloatingActionButton fabRemove;

    int count;
    String kind;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
