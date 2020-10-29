package com.somi.cheems;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.material.switchmaterial.SwitchMaterial;

import static android.content.Context.MODE_PRIVATE;


public class conf extends Fragment {

    SwitchMaterial swtmode;
    private String dark="dark";

    public conf() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conf, container, false);
        final DataSave dataSave = new DataSave();
        swtmode = view.findViewById(R.id.swtmode);
            swtmode.setChecked(dataSave.restorePrefData(dark,getContext()));
        swtmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    dataSave.savePrefsData(true,dark, getContext());
                    restart();
                }else{
                    dataSave.savePrefsData(false,dark, getContext());
                    restart();
                }
            }
        });
        return view;
    }
    public void restart(){
        Intent i = new Intent(getContext(),MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }



}