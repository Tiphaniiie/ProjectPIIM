package telecom.piim2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ResultAnalysis extends AppCompatActivity {
    //todo put the variable in a xml file and link them to Pic 
    String webSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_analysis);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        webSite = extras.getString("URL");
        Uri result = extras.getParcelable("URI");
        imageView2.setImageURI(result);
        Log.i("image : ", result.toString());
        Button bWebsite = (Button) findViewById(R.id.bWebsite);
        bWebsite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webSite));
                startActivity(webIntent);
            }
        });
    }
}
