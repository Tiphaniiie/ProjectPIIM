package telecom.piim2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;
import static android.graphics.Bitmap.Config.RGB_565;
import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

// DEBUG CLASS FOR SERVER CALLS. NOT USED IN ACTUAL APP ANYMORE
public class Requests extends AppCompatActivity implements View.OnClickListener {
    static TextView textView;
    ImageView imageView2;
    String index = "index.json";
    String url2 = "http://www-rech.telecom-lille.fr/nonfreesift/vocabulary.yml";
    String urlRequest = "http://www-rech.telecom-lille.fr/nonfreesift/";
    List<Brand> brandsList = new ArrayList<>();

    public static File writeToFile(String data, String fileName)
    {
        // Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStorageDirectory();
        //final File path = filepath;
        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }
        final File file = new File(path, fileName);
        // Save your stream, don't forget to flush() it before closing it.

        try
        {
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
        //textView.setText(file.getAbsolutePath());
        return file;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        textView = (TextView) findViewById(R.id.textView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        Button bString = (Button) findViewById(R.id.bString);
        bString.setOnClickListener(this);
        Button bJson = (Button) findViewById(R.id.bJson);
        bJson.setOnClickListener(this);
        Button bPic = (Button) findViewById(R.id.bPic);
        bPic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       final RequestQueue queue = Volley.newRequestQueue(this);
        switch (v.getId()) {

            case R.id.bString:
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url2, new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //	Display	the	first	500	characters	of	the	response	string.

                        textView.setText("Response	is:	" + response.substring(0, 500));
                    }
                },
                        new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                textView.setText("That	didn't	work!");
                            }
                        });
                queue.add(stringRequest);
                break;

            case R.id.bJson:
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, urlRequest+index, null,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject json) {
                                //traitement du fichier json
                                try {
                                    String vocab = json.getString("vocabulary");
                                    StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRequest+vocab, new Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            //	Display	the	first	500	characters	of	the	response	string.
                                            writeToFile(response, "vocabulary.yml");

                                        }
                                    },
                                            new ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    textView.setText("That	didn't	work!");
                                                }
                                            });
                                    queue.add(stringRequest);
                                    JSONArray brands = json.getJSONArray("brands");
                                    for (int i = 0; i<brands.length(); i++){
                                        JSONObject x = brands.getJSONObject(i);
                                        brandsList.add(new Brand(x.getString("brandname"), x.getString("url"),x.getString("classifier")));
                                        JSONArray imgs = x.getJSONArray("images");
                                        for (int j = 0; j<imgs.length(); j++){
                                            String y = imgs.getString(j);
                                            brandsList.get(i).setImgNames(y);
                                            Log.i("listes : ", i+" "+brandsList.get(i).getImgNames().get(j).toString());
                                        }
                                        brandsList.get(i).setClassifier(queue, urlRequest);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                textView.setText("That	didn't	work!");
                            }
                        });
                queue.add(jsonRequest);
                break;

            case R.id.bPic:
                ImageRequest imageRequest = new ImageRequest(urlRequest+"train-images/"+brandsList.get(0).getImgNames().get(0), new Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Log.i("TEEEEEST",bitmap.toString());
                        imageView2.setImageBitmap(bitmap);
                    }
                },
                        maxWidth, maxHeight, null, RGB_565, new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.i("HERE", "load	error");
                    }
                });
                queue.add(imageRequest);
                break;
        }


    }
}
