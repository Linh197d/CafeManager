package com.app.shopfee.model;

import java.io.Serializable;

public class Rating implements Serializable {

    private String review;
    private float rate;

    public Rating() {}

    public Rating(String review, float rate) {
        this.review = review;
        this.rate = rate;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
