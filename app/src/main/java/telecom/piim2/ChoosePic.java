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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChoosePic extends AppCompatActivity implements View.OnClickListener {
    //Var used in the selection of the picture to be analysed
    private static final int CAMERA_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    String mCurrentPhotoPath;

    //Var used in the server calls to get the files
    String urlRequest = "http://www-rech.telecom-lille.fr/nonfreesift/";
    List<Brand> brandsList = new ArrayList<>();
    RequestQueue queue;
    File vocab;


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

    //Save xml and yml files from server
    //Todo find a directory that will clean itself when app is restarted
    public static File writeToFile(String data, String fileName) {
        //Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStorageDirectory();
        //Make sure the path directory exists.
        if (!path.exists()) {
            //Make it, if it doesn't exit
            path.mkdirs();
        }
        final File file = new File(path, fileName);

        //Save stream
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    //Get the uri from a bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });
        //Start the server calls
        queue = Volley.newRequestQueue(this);

        //Call to get the index
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, urlRequest + "index.json", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        //Parsing of the json file
                        try {
                            //Get the vocabulary file name from the json
                            String vocabs = json.getString("vocabulary");
                            //Get the vocabulary file from server
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRequest + vocabs, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //Save file
                                    vocab = writeToFile(response, "vocabulary.yml");

                                }
                            },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    });
                            queue.add(stringRequest);
                            //Parse the brands part of the json file
                            JSONArray brands = json.getJSONArray("brands");
                            //Create as many instances of brands as needed
                            for (int i = 0; i < brands.length(); i++) {
                                //Get all objects per brand
                                JSONObject x = brands.getJSONObject(i);
                                //Construct the Brand objects
                                brandsList.add(new Brand(x.getString("brandname"), x.getString("url"), x.getString("classifier")));
                                JSONArray imgs = x.getJSONArray("images");
                                for (int j = 0; j < imgs.length(); j++) {
                                    String y = imgs.getString(j);
                                    brandsList.get(i).setImgNames(y);
                                }
                                brandsList.get(i).setClassifier(queue, urlRequest);
                                brandsList.get(i).setImage(queue, urlRequest);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        queue.add(jsonRequest);

        Button bRequest = (Button) findViewById(R.id.bRequest);
        bRequest.setOnClickListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Make sure the picture has the right dimensions
            setPic();
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
        }
    }

    @Override
    public void onClick(View view) {
        //Analyse the picture and find the brand that is the best match
        PicAnalysis analyse = new PicAnalysis(mCurrentPhotoPath, vocab, (ArrayList<Brand>) brandsList);
        String bestMatch;
        bestMatch = analyse.analysePic();

        //Retrieve brand that's the best match
        for (int i = 0; i < brandsList.size(); i++) {
            if (brandsList.get(i).getName() == bestMatch) {
                //Set the proper Uri from the picture of reference
                brandsList.get(i).setUri(getImageUri(this, brandsList.get(i).getImage()));
                //Send the url and uri of the right Brand to the next activity with the Intent
                Intent analysisIntent = new Intent(ChoosePic.this, ResultAnalysis.class);
                Bundle extras = new Bundle();
                extras.putString("URL", brandsList.get(i).getUrl());
                extras.putParcelable("URI", brandsList.get(i).getUri());
                analysisIntent.putExtras(extras);
                ChoosePic.this.startActivity(analysisIntent);
            }
        }
    }
}