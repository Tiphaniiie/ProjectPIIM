package telecom.piim2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.FileNotFoundException;

/**
 * Created by ASUS on 1/19/2017.
 */

public class ServerInteraction {

    public static void main(String[] args) throws FileNotFoundException {
        String url = "http://www-rech.telecom-lille.fr/nonfreesift/";
        //RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //	Display	the	first	500	characters	of	the	response	string.
               // mTextView.setText("Response	is:	" + response.substring(0, 500));
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //mTextView.setText("That	didn't	work!");
                    }
                });
        //queue.add(stringRequest);
    }
}

