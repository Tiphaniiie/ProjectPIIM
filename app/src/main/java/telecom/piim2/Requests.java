package telecom.piim2;

import android.os.Environment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Requests {
    private RequestQueue queue;
    private String urlRequest;
    private File vocab;
    private ArrayList<Brand> brandsList;

    public Requests(RequestQueue queue, String urlRequest, File vocab, ArrayList<Brand> brandsList) {
        this.queue = queue;
        this.urlRequest = urlRequest;
        this.brandsList = brandsList;
        this.vocab = vocab;
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

    public void sendRequests(){

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
                                    //Make sure the vocab is set in ChoosePic
                                    ChoosePic.vocab = vocab;
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
    }
}
