package com.somi.cheems;

import android.content.Context;
import android.widget.Toast;

public class ErrorHandler {

    private Context context;

    /**
     * Needs the Context in which the Error msg should be printed
     * @param context
     */
    public ErrorHandler(Context context){
        this.context=context;

    }

    /**
     * Prints an Error msg to screen
     * @param msg
     */
    public void printError(String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

    }

}
