package com.somi.cheems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import static android.Manifest.permission.RECORD_AUDIO;

public class intro1 extends AppCompatActivity {
    final DataSave dataSave = new DataSave();
    LottieAnimationView ltprincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_intro1);
            TextView tvprincipal = findViewById(R.id.txtprincipal);
            ltprincipal = findViewById(R.id.ltprincipal);
            CardView btnprincipal = findViewById(R.id.btnprincipal);
            ltprincipal.setAnimation(R.raw.network);
            ltprincipal.playAnimation();
            ltprincipal.loop(true);
            TTS.speak(getString(R.string.str_intro1));
            btnprincipal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            permisos();
                }
            });


    }

    //Metodo para la peticion de los permisos correspondientes al usuario
    private static int PETICION = 101;
    public void permisos(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            TTS.speak(getString(R.string.str_intro2));
            ltprincipal.setAnimation(R.raw.dev);
            ltprincipal.playAnimation();
            ltprincipal.loop(true);
            ActivityCompat.requestPermissions(intro1.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PETICION);
        } else {
            TTS.speak(getString(R.string.str_intro2));
            ltprincipal.setAnimation(R.raw.check);
            ltprincipal.playAnimation();
            ltprincipal.loop(true);
            showAlertDialogButtonClicked();
        }
    }

    public void showAlertDialogButtonClicked() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.terms, null);
        builder.setView(customLayout);
        final RadioButton chkterms = customLayout.findViewById(R.id.chkterms);

        // create and show the alert dialog
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        chkterms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    dialog.dismiss();
                    RemoteModelManager modelManager = RemoteModelManager.getInstance();
                    TranslateRemoteModel Model;
                    switch (getString(R.string.str_karen)){
                        case "es":
                            Model = new TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build();
                            DownloadConditions conditions = new DownloadConditions.Builder()
                                    .requireWifi()
                                    .build();
                            modelManager.download(Model, conditions)
                                    .addOnSuccessListener( v -> {
                                        Intent intent = new Intent(intro1.this, login.class);
                                        dataSave.savePrefsData(true,"inicio",getApplicationContext());
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener( e -> {
                                        // Model couldn’t be downloaded or other internal error.
                                        // ...
                                    });
                        case "en":
                            Intent intent = new Intent(intro1.this, login.class);
                            dataSave.savePrefsData(true,"inicio",getApplicationContext());
                            startActivity(intent);
                            finish();
                    }


                }
            }
        });
    }

}



