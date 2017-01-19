package telecom.piim2;

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

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_READ;
import static org.bytedeco.javacpp.opencv_core.CvFileStorage;
import static org.bytedeco.javacpp.opencv_core.CvMat;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.cvAttrList;
import static org.bytedeco.javacpp.opencv_core.cvOpenFileStorage;
import static org.bytedeco.javacpp.opencv_core.cvReadByName;
import static org.bytedeco.javacpp.opencv_core.cvReleaseFileStorage;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.imread;

public class ChoosePic extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    String mCurrentPhotoPath;
    File vocab;
    private File[] fileTab;
    private File[] classifierTab;

    Mat response_hist = new Mat();
    KeyPoint keypoints = new KeyPoint();
    Mat inputDescriptors = new Mat();
    MatVector imagesVec;

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
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
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
        setContentView(R.layout.activity_choose_pic);
        //permission problem with the gallery pictures
        //The permission is to be verified with the following test
        if (shouldAskPermissions()) {
            askPermissions();
        }
        imageView = (ImageView) findViewById(R.id.imageView);
        Button bCamera = (Button) findViewById(R.id.bCamera);
        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        Button bGallery = (Button) findViewById(R.id.bGallery);
        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        Button bRequest = (Button) findViewById(R.id.bRequest);
        bRequest.setOnClickListener(this);

        //get files into cache
        final AssetManager assetManager = getAssets();

        try {
            String[] filelist = assetManager.list("Data_BOW");
            String[] filelistTest = assetManager.list("Data_BOW"+"/"+ "TestImage");
            String[] filelistClassifier = assetManager.list("Data_BOW"+"/"+"classifiers");

            if ((filelist == null) || (filelistTest == null) || (filelistClassifier == null)) {
            } else {
                vocab = this.ToCache(this, "Data_BOW"+"/"+"vocabulary.yml", "vocabulaire");
                fileTab = new File[filelistTest.length];
                for (int i = 0; i < filelistTest.length; i++) {
                    fileTab[i] = this.ToCache(this, "Data_BOW"+"/"+ "TestImage" +"/"+ filelistTest[i], filelistTest[i]);
                    Log.i("ICIIIIII", fileTab[i].getAbsolutePath());
                }
                classifierTab = new File[filelistClassifier.length];
                for (int i = 0; i<filelistClassifier.length; i++){
                    classifierTab[i] = this.ToCache(this, "Data_BOW"+"/"+ "classifiers" +"/"+ filelistClassifier[i], filelistClassifier[i]);
                    Log.i("ICIIIIII", classifierTab[i].getAbsolutePath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //getClassifier part
        final Mat vocabulary;
        Loader.load(opencv_core.class);
        CvFileStorage storage = cvOpenFileStorage(vocab.getAbsolutePath(), null, CV_STORAGE_READ);
        Pointer p = cvReadByName(storage, null, "vocabulary", cvAttrList());
        CvMat cvMat = new CvMat(p);
        vocabulary = new Mat(cvMat);
        Log.i("HERE", "vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        cvReleaseFileStorage(storage);
        //create SIFT feature point extracter
        final SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);
        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);
        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        Log.i("HEEEEERE", "vocab is set");
        int classNumber = 3;
        String[] class_names;
        class_names = new String[classNumber];

        class_names[0] = "Coca";
        class_names[1] = "Pepsi";
        class_names[2] = "Sprite";
        final CvSVM[] classifiers;
        classifiers = new CvSVM[classNumber];
        for (int i = 0; i < classNumber; i++) {
            Log.i("HEREAGAINNN", "Ok. Creating class name from " + class_names[i]);
            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM();
            classifiers[i].load(classifierTab[i].getAbsolutePath());
        }

        imagesVec = new MatVector(fileTab.length);
        for (int i=0; i < fileTab.length-2; i++){
            Log.i("HEEEEEEEEEEEEEERE", "path:" + fileTab[i].getName());
            Mat imageTest = imread(fileTab[i].getAbsolutePath(), 1);
            detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
            bowide.compute(imageTest, keypoints, response_hist);
            // Finding best match
            float minf = Float.MAX_VALUE;
            String bestMatch = null;
            long timePrediction = System.currentTimeMillis();
            // loop for all classes
            for (int j = 0; j < classNumber; j++) {
                // classifier prediction based on reconstructed histogram
                float res = classifiers[j].predict(response_hist, true);
                //System.out.println(class_names[i] + " is " + res);
                if (res < minf) {
                    minf = res;
                    bestMatch = class_names[j];
                }
            }
            timePrediction = System.currentTimeMillis() - timePrediction;
            Log.i("ICIIIIII", fileTab[i].getName() + "  predicted as " + bestMatch + " in " + timePrediction + " ms");
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            setPic();
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            setPic();

        }
    }

    @Override
    public void onClick(View view) {

    }
}