package telecom.piim2;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;
import static android.graphics.Bitmap.Config.RGB_565;

/**
 * Created by ASUS on 1/19/2017.
 */

public class Brand {
    private String url;
    private String name;
    private String xmlPath;
    private String fileName;
    private File classifier;
    private Bitmap image;
    private ArrayList<String> imgNames;
    private Uri uri;

    //Constructor
    public Brand(String name, String url, String fileName) {
        this.url = url;
        this.name = name;
        this.fileName = fileName;
        //Todo eventually put classifiers as a variable
        this.xmlPath = "classifiers/"+fileName;
        this.imgNames = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }


    public String getXmlPath() {
        return xmlPath;
    }

    public File getClassifier() {

        return classifier;
    }

    //For each brand, get the classifier (xml file)
    public void setClassifier(RequestQueue queue, String urlRequest) {
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, urlRequest+this.getXmlPath(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                classifier = Requests.writeToFile(response, fileName);
                Log.d("Classifier : ", classifier.getName());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Classifier", "load	error");
                    }
                });
        queue.add(stringRequest2);

    }

    public Bitmap getImage() {
        return image;
    }

    //Get the picture of reference for each brand
    public void setImage(RequestQueue queue, String urlRequest) {
        //Todo eventually get train-images in a variable
        ImageRequest imageRequest = new ImageRequest(urlRequest+"train-images/"+this.getImgNames().get(0), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                Log.d("Picture",bitmap.toString());
                image = bitmap;
            }
        },
                maxWidth, maxHeight, null, RGB_565, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("Picture", "load	error");
            }
        });
        queue.add(imageRequest);
    }

    public List<String> getImgNames() {
        return imgNames;
    }

    // Add new picture names to the list
    public void setImgNames(String imgName) {
        this.imgNames.add(imgName);
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
