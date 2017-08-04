package com.calendate.calendate.models;

public class Button {

    String btnId;
    String imageUrl;

    public Button() {
    }

    public Button(String btnId, String imageUrl) {
        this.btnId = btnId;
        this.imageUrl = imageUrl;
    }

    public String getBtnId() {
        return btnId;
    }

    public void setBtnId(String btnId) {
        this.btnId = btnId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
