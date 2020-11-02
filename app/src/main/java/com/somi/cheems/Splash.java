package com.somi.cheems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {

    // Duración en milisegundos que se mostrará el splash
    private final int DURACION_SPLASH = 1000;
    private String key = "dark";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if(DataSave.restorePrefData(key,getApplicationContext())){
            setTheme(R.style.darkTheme);
        }
        else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        TTS.init(getApplicationContext());
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable(){
            public void run(){
                if(DataSave.restorePrefData("inicio",getApplicationContext())){
                    obtenerinfo(auth);
                }else{
                    Intent intent = new Intent(Splash.this, intro1.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DURACION_SPLASH);
    }

    private void obtenerinfo(FirebaseAuth auth){
        if(auth.getCurrentUser()!=null){
            Intent main = new Intent(Splash.this, MainActivity.class);
            startActivity(main);
            finish();
        }else{
            Intent login = new Intent(Splash.this, login.class);
            startActivity(login);
            finish();
        }
    }
}