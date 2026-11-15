package com.rather.ecosort;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SighUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigh_up);
        findViewById(R.id.registration_button).setOnClickListener(view ->
                Toast.makeText(this, R.string.profile_unavailable, Toast.LENGTH_SHORT).show());
        findViewById(R.id.move_to_login_text_view).setOnClickListener(view -> finish());
    }
}
