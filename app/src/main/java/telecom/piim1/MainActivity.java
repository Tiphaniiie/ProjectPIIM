package telecom.piim1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import static org.bytedeco.javacpp.opencv_features2d.BFMatcher;
import static org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_nonfree.SIFT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Todo: carefully renamme the package and all that it is associated with
    private static final int CAMERA_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    String mCurrentPhotoPath;

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

    //The pic class simplifies the way to select the best picture
    private Pic[] picTab;
    private Pic finalPic;


    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    //cf. OnCreate, pb with permissions with gallery
    @TargetApi(Build.VERSION_CODES.M)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }
    //for the camera picture
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    //for the camera picture
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    //Intent for the camera picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }
    //both for the camera and gallery pictures
    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
    //Determine the picture with the smallest distance from the reference
    //could probably be optimized a bit better
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

    //Narrow matches to a few good ones
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

        //Get the average of the few good matches
        float result = 0;
        for (int i = 0; i < brandNewMatches.size(); i++){
            result += brandNewMatches.get(i, 0).distance();
        }
        return result / brandNewMatches.size();
    }
    //put the pictures from assets in the cache
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

        //permission problem with the gallery pictures
        //The permission is to be verified with the following test
        if (shouldAskPermissions()) {
            askPermissions();
        }
        imageView = (ImageView) findViewById(R.id.imageView);
        Button bCamera = (Button) findViewById(R.id.bCamera);
        bCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        Button bGallery = (Button) findViewById(R.id.bGallery);
        bGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        final AssetManager assetManager = getAssets();
        try {
            String[] filelist = assetManager.list("images");
            Log.i("filelist.length", String.valueOf(filelist.length));
            if (filelist == null) {
            } else {
                fileTab = new File[filelist.length];
                for (int i=0; i<filelist.length; i++) {
                    fileTab[i] = this.ToCache(this, "images"+"/"+filelist[i], filelist[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button bAnalyze = (Button) findViewById(R.id.bAnalyze);
        bAnalyze.setOnClickListener(this);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            setPic();
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            setPic();

        }
    }

    @Override
    public void onClick(View view) {

        this.filePath = mCurrentPhotoPath;
        Log.i("filepath", mCurrentPhotoPath);
        img = imread(filePath);
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
        //There are two files in the cache (android icon and another),
        //so the -2 removes them (they're always at the end)
        for (int i=0; i < fileTab.length-2; i++){
            picTab[i] = new Pic();
            picTab[i].setPicture(fileTab[i]);
            img2[i] = imread(fileTab[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
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

        //Passing only an URI is less costly
        Uri uri = Uri.fromFile(finalPic.getPicture());
        Intent analysisIntent = new Intent(MainActivity.this, ResultAnalysis.class);
        analysisIntent.setData(uri);
        MainActivity.this.startActivity(analysisIntent);
    }
}
