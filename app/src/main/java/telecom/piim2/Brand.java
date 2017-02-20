package telecom.piim2;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public Brand(String name, String url, String fileName) {
        this.url = url;
        this.name = name;
        this.fileName = fileName;
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

    public void setClassifier(RequestQueue queue, String urlRequest) {
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, urlRequest+this.getXmlPath(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                classifier = Requests.writeToFile(response, fileName);
                Log.i("classifier : ", classifier.getName());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(stringRequest2);
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getImgNames() {
        return imgNames;
    }

    public void setImgNames(String imgName) {
        this.imgNames.add(imgName);
    }
}
