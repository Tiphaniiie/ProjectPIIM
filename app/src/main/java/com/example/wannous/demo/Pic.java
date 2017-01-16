package com.example.wannous.demo;

import java.io.File;

import static org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;

/**
 * Created by ASUS on 1/16/2017.
 */

public class Pic {

    private DMatchVectorVector score;
    private File picture;

    public File getPicture() {
        return picture;
    }

    public void setPicture(File picture) {
        this.picture = picture;
    }

    public DMatchVectorVector getScore() {
        return score;
    }

    public void setScore(DMatchVectorVector score) {
        this.score = score;
    }
}
