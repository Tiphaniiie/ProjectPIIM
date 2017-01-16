package com.example.wannous.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
    private SIFT SiftDesc;
    private String filePath;
    private Pic imgFinal;
    private ImageView imgView;
    private File[] fileTab;
    private String[] fileNameTab;

    private static DMatchVectorVector refineMatches(DMatchVectorVector oldMatches) {
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
        return brandNewMatches;
    }

    private static Pic bestDistance(Pic[] scores){
        DMatchVectorVector finalScore = scores[0].getScore();
        Pic bestPic = new Pic();
       for (int i = 0; i < scores.length; i++){
           if (scores[i].getScore().get(i,0).distance() < finalScore.get(i,0).distance()){
               bestPic = scores[i];
           }
       }return bestPic;
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
            if (filelist == null) {
            } else {
                fileTab = new File[filelist.length];
                for (int i=0; i<filelist.length; i++) {
                    InputStream inputStream = assetManager.open("images"+"/"+filelist[i]);
                    //fileTab[i]=createFileFromInputStream(inputStream, filelist[i]);
                    fileTab[i] = this.ToCache(this, "images"+"/"+filelist[i], filelist[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imgView.setImageBitmap(bitmap);
        Button keypointsButton = (Button) findViewById(R.id.Keypoints);

        keypointsButton.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        imgFinal = new Pic();
        img = imread(this.filePath);
        SiftDesc = new SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);
        KeyPoint keypoints = new KeyPoint();
        SiftDesc.detect(img, keypoints);
        Mat descriptor = new Mat();
        Mat[] compImg = new Mat [fileTab.length];
        KeyPoint[] keyPoints = new KeyPoint[fileTab.length];
        Mat[] descriptors = new Mat[fileTab.length];
        Pic[] scores = new Pic[fileTab.length];
        for (int i=0; i< fileTab.length; i++) {
            compImg[i]= imread(fileTab[i].getPath(), CV_LOAD_IMAGE_GRAYSCALE);
            keyPoints[i] = new KeyPoint();
            SiftDesc.detect(compImg[i], keyPoints[i]);
            descriptors[i] = new Mat();
            SiftDesc.compute(compImg[i], keyPoints[i],descriptors[i]);
        }
        BFMatcher matcher = new BFMatcher( NORM_L2, false);
        DMatchVectorVector[] matches = new DMatchVectorVector[fileTab.length];
        for(int i=0; i<fileTab.length; i++){
            matches[i] = new DMatchVectorVector();
            matcher.knnMatch(descriptor, descriptors[i], matches[i], 2);
            scores[i].setPicture(fileTab[i]);
            scores[i].setScore(refineMatches(matches[i]));
        }
        imgFinal = bestDistance(scores);
        Bitmap bitmap = BitmapFactory.decodeFile(imgFinal.getFilePath());
        imgView.setImageBitmap(bitmap);
        //Toast.makeText(this, "Nb of detected keypoints:" + keypoints.capacity(), Toast.LENGTH_LONG).show();
    }
}
