package telecom.projectpiim;

import android.graphics.Bitmap;

import org.bytedeco.javacpp.opencv_core.Mat;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;

/**
 * Created by ASUS on 11/23/2016.
 */

public class SpeMat {
    private String uri;
    private Bitmap pic;
    private String web;
    private double distance;
    private Mat image;

    public SpeMat(Bitmap pic, String uri) {
        this.pic = pic;
        this.uri =  uri.substring(uri.lastIndexOf('/') + 1);
    }

    public Mat getImage() {
        return image = new Mat(this.pic.getHeight(),this.pic.getWidth(), CV_8UC1);
    }


    public void setImage(Mat image) {
        this.image = image;
    }

    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap image) {
        this.pic = pic;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
