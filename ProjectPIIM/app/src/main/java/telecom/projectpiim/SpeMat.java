package telecom.projectpiim;

import android.graphics.Bitmap;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

import static org.bytedeco.javacpp.opencv_highgui.imread;

/**
 * Created by ASUS on 11/23/2016.
 */

public class SpeMat {
    private String uri;
    private Bitmap pic;
    private String web;
    private double distance;
    private Mat image;
    private File file;

    public SpeMat(Bitmap pic, String uri, File file) {
        this.pic = pic;
        this.uri =  uri.substring(uri.lastIndexOf('/') + 1);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getUri() {
        return uri;
    }

    public Mat getImage() {
        //image = new Mat(this.pic.getHeight(),this.pic.getWidth(), CV_8UC1);
        image = new Mat(imread(this.uri, 1));
        return image;
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
