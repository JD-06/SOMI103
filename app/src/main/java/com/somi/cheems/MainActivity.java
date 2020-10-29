package com.somi.cheems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
private String key = "dark";
private BackgroundService gpsService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Condicion para setear el tema de la app, oscura o clara, segun las preferencias del usuario
        if(DataSave.restorePrefData(key,getApplicationContext())){
            setTheme(R.style.darkTheme);
        }
        else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Metodo de bottomnavigation(La barra de abajo de la interfaz)
        TTS.init(getApplicationContext());
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new asis()).commit();
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_asis:
                        selectedFragment = new asis();
                        TTS.speak(getString(R.string.str_asis));
                        break;
                    case R.id.nav_menu:
                        selectedFragment = new menu();
                        TTS.speak(getString(R.string.str_menu));
                        break;
                    case R.id.nav_guia:
                        selectedFragment = new guia();
                        TTS.speak(getString(R.string.str_guia));
                        break;
                    case R.id.nav_conf:
                        selectedFragment = new conf();
                        TTS.speak(getString(R.string.str_conf));
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };



}