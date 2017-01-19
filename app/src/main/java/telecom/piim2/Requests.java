package telecom.piim2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Requests extends AppCompatActivity implements View.OnClickListener {
    TextView textView;
    String url = "http://www-rech.telecom-lille.fr/nonfreesift/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        textView = (TextView) findViewById(R.id.textView);
        Button bServer = (Button) findViewById(R.id.bServer);
        bServer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //	Display	the	first	500	characters	of	the	response	string.
                 textView.setText("Response	is:	" + response.substring(0, 500));
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("That	didn't	work!");
                    }
                });
        queue.add(stringRequest);
    }
}
