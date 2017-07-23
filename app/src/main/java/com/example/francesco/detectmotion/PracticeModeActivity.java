package com.example.francesco.detectmotion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PracticeModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_mode);

        Intent i = getIntent();
        String mode = i.getStringExtra("game_mode");
        Toast.makeText(getApplicationContext(), "Modalità: "+ mode, Toast.LENGTH_LONG).show();


    }
}
