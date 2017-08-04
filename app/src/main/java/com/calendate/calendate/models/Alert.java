package com.calendate.calendate.models;

public class Alert {

    int count;
    int kind;

    public Alert() {
    }

    public Alert(int count, int kind) {
        this.count = count;
        this.kind = kind;
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
}
