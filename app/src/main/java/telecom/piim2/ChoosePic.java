package telecom.piim2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChoosePic extends AppCompatActivity implements View.OnClickListener {

    //Var used in the selection of the picture to be analysed
    private static final int CAMERA_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    private ProgressBar pBar;
    String mCurrentPhotoPath;
    Button bCamera;
    Button bGallery;
    Button bRequest;
    Button bCrop;

    //Var for crop
    Uri uriPic;

    //var to get to next activity
    Bundle extras = new Bundle();
    String bestMatch;

    //Var used in the server calls to get the files
    String urlRequest = "http://www-rech.telecom-lille.fr/nonfreesift/";
    List<Brand> brandsList = new ArrayList<>();
    RequestQueue queue;
    static File vocab;
    Requests callSvr;


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


    //Get the uri from a bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pic);
        bCamera = (Button) findViewById(R.id.bCamera);
        bGallery = (Button) findViewById(R.id.bGallery);
        pBar = (ProgressBar) findViewById(R.id.pBar);
        bRequest = (Button) findViewById(R.id.bRequest);
        bCrop = (Button) findViewById(R.id.bCrop);
        pBar.setVisibility(View.GONE);
        //permission problem with the gallery pictures
        //The permission is to be verified with the following test
        if (shouldAskPermissions()) {
            askPermissions();
        }
        imageView = (ImageView) findViewById(R.id.imageView);

        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });
        //Start the server calls
        queue = Volley.newRequestQueue(this);
        callSvr = new Requests(queue, urlRequest, vocab, (ArrayList<Brand>) brandsList);
        callSvr.sendRequests();

        bRequest.setVisibility(View.GONE);
        bRequest.setOnClickListener(this);

        bCrop.setVisibility(View.GONE);
        bCrop.setOnClickListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Make sure the picture has the right dimensions
            setPic();
            bCrop.setVisibility(View.VISIBLE);
            bRequest.setVisibility(View.VISIBLE);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            //Get the URI from the gallery picture chosen
            Uri selectedImage = data.getData();
            String[] filePathColumn = {Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();

            //Make sure the picture has the right dimensions
            setPic();
            bCrop.setVisibility(View.VISIBLE);
            bRequest.setVisibility(View.VISIBLE);
        }
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //Button to analyse picture
            case R.id.bRequest:
                //Thread created in order to show the progression widget while the analysis is running
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Analyse the picture and find the brand that is the best match
                        PicAnalysis analyse = new PicAnalysis(mCurrentPhotoPath, vocab, (ArrayList<Brand>) brandsList);
                        bestMatch = analyse.analysePic();
                        final Context context = getApplicationContext();
                        //Retrieve brand that's the best match
                        for (int i = 0; i < brandsList.size(); i++) {
                            if (brandsList.get(i).getName() == bestMatch) {
                                //Set the proper Uri from the picture of reference
                                brandsList.get(i).setUri(getImageUri(context, brandsList.get(i).getImage()));
                                //Send the url and uri of the right Brand to the next activity with the Intent
                                extras.putString("URL", brandsList.get(i).getUrl());
                                extras.putParcelable("URI", brandsList.get(i).getUri());
                            }
                        }
                        Intent analysisIntent = new Intent(ChoosePic.this, ResultAnalysis.class);
                        analysisIntent.putExtras(extras);
                        ChoosePic.this.startActivity(analysisIntent);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.setVisibility(View.GONE);
                        }
                    });
                    }
                });
                pBar.setVisibility(View.VISIBLE);
                thread.start();
                break;

            //Button to crop picture
            case R.id.bCrop:
                imageView.setImageDrawable(null);
                uriPic = Uri.parse("file:///"+mCurrentPhotoPath);
                Log.d("Crop-URI", uriPic.toString());
                beginCrop(uriPic);
                break;
        }
    }
}