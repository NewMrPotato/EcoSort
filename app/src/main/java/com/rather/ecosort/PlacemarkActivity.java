package com.rather.ecosort;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PlacemarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placemark);

        ImageView placemark_image_view = findViewById(R.id.placemark_image_view);
        TextView placemark_title_text_view = findViewById(R.id.placemark_title_text_view);
        TextView placemark_content_text_view = findViewById(R.id.placemark_content_text_view);

        placemark_image_view.setImageResource(getIntent().getIntExtra("placemark_image_view", 0));
        placemark_title_text_view.setText(getIntent().getStringExtra("placemark_title_text_view"));
        placemark_content_text_view.setText(getIntent().getStringExtra("placemark_content_text_view"));
    }
}
