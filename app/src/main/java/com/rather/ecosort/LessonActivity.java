package com.rather.ecosort;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LessonActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE = "lesson_content_image_view";
    public static final String EXTRA_TEXT = "lesson_content_text_view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        CardView backButton = findViewById(R.id.lesson_back_button);
        backButton.setOnClickListener(view -> finish());

        ImageView lesson_content_image_view = findViewById(R.id.lesson_content_image_view);
        TextView lesson_content_text_view = findViewById(R.id.lesson_content_text_view);

        lesson_content_image_view.setImageResource(getIntent().getIntExtra(EXTRA_IMAGE, 0));
        lesson_content_text_view.setText(getIntent().getStringExtra(EXTRA_TEXT));
    }
}
