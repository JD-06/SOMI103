package com.somi.cheems;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class FloatWidgetService extends Service {
    private String key = "dark";
    private WindowManager mWindowManager;
    private View mFloatingWidget;
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;
    private static final String TAG = FloatWidgetService.class.getSimpleName();
    private String uuid = UUID.randomUUID().toString();
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private SessionsClient sessionsClient;
    private SessionName session;
    private KarenActions karenActions;

    public FloatWidgetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(DataSave.restorePrefData(key,getApplicationContext())){
            setTheme(R.style.darkTheme);
        }
        else {
            setTheme(R.style.AppTheme);
        }
        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.bubble_layout,null);
        karenActions = new KarenActions();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

      /*  ImageView closeButtonCollapsed = (ImageView) mFloatingWidget.findViewById(R.id.floating_image);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

           *//*     Intent intent = new Intent(BROADCAST_ACTION);
                sendBroadcast(intent);

                stopSelf();*//*
            }
        });*/

        mFloatingWidget.findViewById(R.id.bubbleasis).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        return false;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                        if (clickDuration < MAX_CLICK_DURATION) {

                            mFloatingWidget.setEnabled(false);
                            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, getString(R.string.str_karenlenguaje));
                            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
                            speech.startListening(recognizerIntent);

                           // stopSelf();

                        }

                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return false;
                }
                return false;
            }

        });
        SpeechToText();
        initV2Chatbot();
    }


    @Override
    public void onDestroy() {

        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
        super.onDestroy();
    }
    private void SpeechToText() {
        speech = SpeechRecognizer.createSpeechRecognizer(FloatWidgetService.this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(FloatWidgetService.this));
        //Reconocimiento de voz
        speech.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                mFloatingWidget.setEnabled(true);
            }

            @Override
            public void onError(int error) {
                TTS.speak(getString(R.string.str_karenerror));
                mFloatingWidget.setEnabled(true);
                Log.e("Asis",getErrorText(error));
            }

            @Override
            public void onResults(Bundle results) {
                mFloatingWidget.setEnabled(true);
                ArrayList<String> matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Toast.makeText(FloatWidgetService.this, matches.get(0), Toast.LENGTH_SHORT).show();
                QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(matches.get(0)).setLanguageCode(getString(R.string.str_karenlenguaje))).build();
                new RequestJavaAsis(FloatWidgetService.this, session, sessionsClient, queryInput).execute();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

    }

    private void initV2Chatbot() {
        try {
            InputStream stream = getResources().openRawResource(R.raw.somikarenkey);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = ((ServiceAccountCredentials)credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            session = SessionName.of(projectId, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void callbackV2(DetectIntentResponse response) {
        if (response != null) {
            karenActions.actions(response.getQueryResult().getAction(), response,FloatWidgetService.this);
        } else {
            Log.d(TAG, "Bot Reply: Null");
            Toast.makeText(FloatWidgetService.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}