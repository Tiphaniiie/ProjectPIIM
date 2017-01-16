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
        String refFile2 = "Coca_7.jpg";
        this.filePath = this.ToCache(this, "images" + "/" + refFile, refFile).getPath();
        final AssetManager assetManager = getAssets();
        try {
            String[] filelist = assetManager.list("images");
            if (filelist == null) {
            } else {
                fileTab = new File[filelist.length];
                for (int i=0; i<filelist.length; i++) {
                    InputStream inputStream = assetManager.open("images"+"/"+filelist[i]);
                    fileTab[i] = this.ToCache(this, "images"+"/"+filelist[i], filelist[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imageView.setImageBitmap(bitmap);
        Button keypointsButton = (Button) findViewById(R.id.Keypoints);
        keypointsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        img = imread(this.filePath);
        img2 = new Mat[fileTab.length];
        SiftDesc = new SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);
        Mat descriptor = new Mat();
        Mat descriptor2 = new Mat();
        KeyPoint keypoints = new KeyPoint();
        KeyPoint keypoints2 = new KeyPoint();
        SiftDesc.detect(img, keypoints);
        SiftDesc.compute(img, keypoints, descriptor);
        for (int i=0; i< fileTab.length; i++){
            img2[i] = imread(fileTab[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            Log.i("avant second detect", String.valueOf(i));
            SiftDesc.detect(img2[i], keypoints2);
            Log.i("après second detect", String.valueOf(i));
            SiftDesc.compute(img2[i], keypoints2, descriptor2);
        }
        Toast.makeText(this, "Nb of detected keypoints:" + keypoints.capacity(), Toast.LENGTH_LONG).show();
    }
}
