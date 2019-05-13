package com.r0_f0.SistemaGeolocalizador;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intento =new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intento);
                finish();
            }
        },3000);
    }//Cierra OnCreate
}//Cierra la clase MainActivity
