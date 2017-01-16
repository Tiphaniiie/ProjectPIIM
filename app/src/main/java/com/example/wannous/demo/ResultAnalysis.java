package com.example.wannous.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ResultAnalysis extends AppCompatActivity {
    String webSite = "http://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_analysis);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        Uri uri = getIntent().getData();
        imageView2.setImageURI(uri);

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
