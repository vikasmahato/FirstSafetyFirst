package com.products.safetyfirst.models;

/**
 * Created by profileconnect on 24/04/17.
 */

public class Statics_model {

    Integer point;
    String texts;

    public Statics_model(String texts) {
        this.texts = texts;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }
}
