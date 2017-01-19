package telecom.piim2;

import android.graphics.Bitmap;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;
import static android.graphics.Bitmap.Config.RGB_565;
import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class Requests extends AppCompatActivity implements View.OnClickListener {
    TextView textView;
    ImageView imageView2;
    String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";
    String url3 = "http://www-rech.telecom-lille.fr/nonfreesift/train-images/Coca_12.jpg";
    String url2 = "http://www-rech.telecom-lille.fr/nonfreesift/vocabulary.yml";

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
        RequestQueue queue = Volley.newRequestQueue(this);
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
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject json) {
                                //traitement du fichier json
                                try {
                                    String array = json.getJSONArray("brands").toString();
                                    textView.setText(array);
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
                ImageRequest imageRequest = new ImageRequest(url3, new Listener<Bitmap>() {
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
