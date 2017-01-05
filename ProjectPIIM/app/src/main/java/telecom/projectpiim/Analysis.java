package telecom.projectpiim;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class Analysis extends AppCompatActivity {
    ImageView imgResult;
    Button bWeb;
    String webSite = "http://www.google.com";
    String uri;
    ByteArrayOutputStream os = new ByteArrayOutputStream();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        imgResult = (ImageView) findViewById(R.id.imgResult);
        bWeb = (Button) findViewById(R.id.bWeb);
        bWeb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webSite));
                startActivity(webIntent);
            }
        });
        Bundle extras = getIntent().getExtras();
        Bitmap bmp = (Bitmap) extras.getParcelable("Bitmap");
        //image.setImage(bmp);

        imgResult.setImageBitmap(bmp);

    }

}
