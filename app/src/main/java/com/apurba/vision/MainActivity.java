package com.apurba.vision;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onEmojifyMeClicked(View view) {
        Intent intent = new Intent(this, EmojifyMeActivity.class);
        startActivity(intent);
    }
}