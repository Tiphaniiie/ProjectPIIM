package com.example.wannous.demo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class ResultAnalysis extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_analysis);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        Uri uri = getIntent().getData();
        imageView2.setImageURI(uri);
    }
}
