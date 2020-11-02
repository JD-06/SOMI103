package com.somi.cheems;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class DataSave {

    //Metodo para guardar datos de tipo boolean mediante PrefData
    public void savePrefsData(Boolean tf, String nm, Context context) {
        SharedPreferences pref = context.getSharedPreferences(nm,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(nm,tf);
        editor.commit();
    }
    //Metodo para recuperar los datos
    public static boolean restorePrefData(String nm, Context context) {
        SharedPreferences pref = context.getSharedPreferences(nm,MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean(nm,false);
        return  isIntroActivityOpnendBefore;
    }
    public void savePrefsDatastr(String tf, String nm, Context context) {
        SharedPreferences pref = context.getSharedPreferences(nm,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(nm,tf);
        editor.commit();
    }
    //Metodo para recuperar los datos
    public static String restorePrefDatastr(String nm, Context context) {
        SharedPreferences pref = context.getSharedPreferences(nm,MODE_PRIVATE);
        String isIntroActivityOpnendBefore = pref.getString(nm,"");
        return  isIntroActivityOpnendBefore;
    }
}
