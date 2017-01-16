package com.example.wannous.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import static org.bytedeco.javacpp.opencv_features2d.BFMatcher;
import static org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_nonfree.SIFT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    // SIFT keypoint features
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;


    public Mat img;
    public Mat[] img2;
    private SIFT SiftDesc;
    private File[] fileTab;
    private String filePath;
    private Pic[] picTab;
    private Pic finalPic;

    private static Pic bestPic(Pic[] picTab){
        Pic finalPic = new Pic();
        finalPic.setScore(picTab[0].getScore());
        for (int i = 0; i < picTab.length; i++){
            if (picTab[i].getScore() < finalPic.getScore()){
                finalPic.setScore(picTab[i].getScore());
                finalPic.setPicture(picTab[i].getPicture());
            }
        }
        return finalPic;
    }
    private static float refineMatches(DMatchVectorVector oldMatches) {
        double RoD = 0.6;
        DMatchVectorVector newMatches = new DMatchVectorVector();
        int sz = 0;
        newMatches.resize(oldMatches.size());
        double maxDist = 0.0, minDist = 1e100;
        for (int i = 0; i < oldMatches.size(); i++) {
            newMatches.resize(i, 1);
            if (oldMatches.get(i, 0).distance() < RoD
                    * oldMatches.get(i, 1).distance()) {
                newMatches.put(sz, 0, oldMatches.get(i, 0));
                sz++;
                double distance = oldMatches.get(i, 0).distance();
                if (distance < minDist)
                    minDist = distance;
                if (distance > maxDist)
                    maxDist = distance;
            }
        }
        newMatches.resize(sz);
        sz = 0;
        DMatchVectorVector brandNewMatches = new DMatchVectorVector();
        brandNewMatches.resize(newMatches.size());
        for (int i = 0; i < newMatches.size(); i++) {
            if (newMatches.get(i, 0).distance() <= 3 * minDist) {
                brandNewMatches.resize(sz, 1);
                brandNewMatches.put(sz, 0, newMatches.get(i, 0));
                sz++;
            }
        }
        brandNewMatches.resize(sz);
        float result = 0;
        for (int i = 0; i < brandNewMatches.size(); i++){
            result += brandNewMatches.get(i, 0).distance();
        }

        return result / brandNewMatches.size();
    }

    public static File ToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String refFile = "Pepsi_10.jpg";
        this.filePath = this.ToCache(this, "images" + "/" + refFile, refFile).getPath();
        final AssetManager assetManager = getAssets();
        try {
            String[] filelist = assetManager.list("images");
            Log.i("filelist.length", String.valueOf(filelist.length));
            if (filelist == null) {
            } else {
                fileTab = new File[filelist.length];
                for (int i=0; i<filelist.length; i++) {
                    //InputStream inputStream = assetManager.open("images"+"/"+filelist[i]);
                    fileTab[i] = this.ToCache(this, "images"+"/"+filelist[i], filelist[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imageView.setImageBitmap(bitmap);
        Button bAnalyze = (Button) findViewById(R.id.bAnalyze);
        bAnalyze.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        img = imread(this.filePath);
        img2 = new Mat[fileTab.length-2];
        picTab = new Pic[fileTab.length-2];
        finalPic = new Pic();
        SiftDesc = new SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);
        Mat descriptor = new Mat();
        Mat descriptor2 = new Mat();
        KeyPoint keypoints = new KeyPoint();
        KeyPoint keypoints2 = new KeyPoint();
        SiftDesc.detect(img, keypoints);
        SiftDesc.compute(img, keypoints, descriptor);
        BFMatcher matcher = new BFMatcher( NORM_L2, false);
        DMatchVectorVector[] matches = new DMatchVectorVector[fileTab.length];
        for (int i=0; i < fileTab.length-2; i++){
            picTab[i] = new Pic();
            picTab[i].setPicture(fileTab[i]);
            img2[i] = imread(fileTab[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            Log.i("filetab.length", String.valueOf(fileTab.length));
            SiftDesc.detect(img2[i], keypoints2);
            Log.i("après second detect", fileTab[i].getName());
            SiftDesc.compute(img2[i], keypoints2, descriptor2);
            matches[i] = new DMatchVectorVector();
            matcher.knnMatch(descriptor, descriptor2, matches[i], 2);
            picTab[i].setScore(refineMatches(matches[i]));
            Log.i("score", String.valueOf(picTab[i].getScore()));
        }
        finalPic = bestPic(picTab);
        Log.i("final picture", finalPic.getPicture().getName());
        Toast.makeText(this, "Nb of detected keypoints:" + keypoints.capacity(), Toast.LENGTH_LONG).show();
    }
}
