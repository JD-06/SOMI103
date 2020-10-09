package com.somi.cheems;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;

import java.util.Set;

import static android.speech.tts.Voice.LATENCY_NORMAL;
import static android.speech.tts.Voice.QUALITY_VERY_HIGH;

public class TTS {
    private static TextToSpeech textToSpeech;

    //Metodo el cual se instancia en la primera activity
    public static void init(final Context context) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                }
            });
        }
    }
    //Metodo para activar el TTS y diga los que se le pase mediante un string
    public static void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}