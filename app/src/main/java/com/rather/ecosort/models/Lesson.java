package com.rather.ecosort.models;

import android.text.Spanned;

public class Lesson {

    private int preview, main_image;
    private String title;
    private String text;


    public Lesson(int  image, String title, String text, int main_image) {
        this.preview = image;
        this.title = title;
        this.text = text;
        this.main_image = main_image;
    }

    public int getPreview() {
        return preview;
    }

    public void setPreview(int preview) {
        this.preview = preview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMain_image() {
        return main_image;
    }

    public void setMain_image(int main_image) {
        this.main_image = main_image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
