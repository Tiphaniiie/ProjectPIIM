package com.example.wannous.demo;

import java.io.File;

/**
 * Created by ASUS on 1/16/2017.
 */

public class Pic {

    private float score;
    private File picture;
    private float[] scoreTab;

    public File getPicture() {
        return picture;
    }

    public void setPicture(File picture) {
        this.picture = picture;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {

        this.score = score;
    }

    public float[] getScoreTab() {
        return scoreTab;
    }

    public void setScoreTab(float[] scoreTab) {
        this.scoreTab = scoreTab;
    }
}