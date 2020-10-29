package com.somi.cheems;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dmax.dialog.SpotsDialog;

public class IdentificadorImagenes extends AppCompatActivity {
    private AlertDialog waitingDialog;
    private PreviewView mPreviewView;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private CardView btncapturar;
    private String mPhotoPath;
    private VisualRecognition vrClient;
    private ErrorHandler errorHandler;
    private InternetCheck internetCheck;
    private String key = "dark";

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DataSave.restorePrefData(key, getApplicationContext())) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        TTS.init(getApplicationContext());
        setContentView(R.layout.activity_identificador_imagenes);
        waitingDialog = new SpotsDialog.Builder().setMessage(getString(R.string.str_dialogprocess))
                .setContext(this)
                .setCancelable(false)
                .build();
        btncapturar = findViewById(R.id.btncapturar);
        mPreviewView = findViewById(R.id.view_finder);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
            IamOptions options = new IamOptions.Builder()
                    .apiKey(getString(R.string.api_key))
                    .build();
            vrClient = new VisualRecognition("2018-07-01", options);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();
        builder.setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetResolution(new Size(1080, 1920))
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);


        btncapturar.setOnClickListener(v -> {

            waitingDialog.show();
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    MediaScannerConnection.scanFile(IdentificadorImagenes.this,
                            new String[]{file.toString()}, null,
                            (path, uri) -> {

                                try {
                                    mPhotoPath = path;
                                    errorHandler = new ErrorHandler(getApplicationContext());
                                    internetCheck = new InternetCheck();
                                    InputImage image = InputImage.fromFilePath(getApplicationContext(), uri);
                                    runOnUiThread(() -> Toast.makeText(IdentificadorImagenes.this, "Picture saved: " + mPhotoPath,
                                            Toast.LENGTH_LONG).show());
                                    AsyncTask.execute(() -> {
                                        if (internetCheck.checkInternetConnection(getApplicationContext())) {
                                            try {
                                                //setup classifier options
                                                ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                                                        .imagesFile(file)
                                                        .threshold((float) 0.7)
                                                        .acceptLanguage(getString(R.string.str_karen))
                                                        .owners(Collections.singletonList("me"))
                                                        .classifierIds(Collections.singletonList("default"))
                                                        .build();

                                                final ClassifiedImages output = vrClient.classify(classifyOptions).execute();

                                                //Some debug prints
                                                Log.i("CLASS", output.getImages().get(0).getClassifiers().get(0).getClasses().get(0).getClassName());
                                                Log.i("SCORE", output.getImages().get(0).getClassifiers().get(0).getClasses().get(0).getScore().toString());


                                                List<ClassResult> classes = output.getImages().get(0).getClassifiers().get(0).getClasses();

                                                //iterate over all results and save into String
                                                final StringBuffer buffer = new StringBuffer();
                                                for (ClassResult result : classes) {

                                                    String name = result.getClassName();
                                                    String score = Float.toString(result.getScore() * 100);
                                                    Log.i("Class", result.getClassName());
                                                    Log.i("Score", result.getScore().toString());
                                                    buffer.append(name).append(". ");


                                                }
                                                // Print results in results field
                                                runOnUiThread(() -> {
                                                    TextRecognizer recognizer = TextRecognition.getClient();
                                                    Task<Text> result =
                                                            recognizer.process(image)
                                                                    .addOnSuccessListener(visionText -> {
                                                                        if (visionText.getText().equals("")) {
                                                                            TTS.speak(getString(R.string.str_encontre) + buffer.toString());
                                                                        } else
                                                                            TTS.speak(getString(R.string.str_encontre) + ", " + buffer.toString() + ", " + getString(R.string.str_textofound) + ", " + visionText.getText());
                                                                    })
                                                                    .addOnFailureListener(
                                                                            e -> {
                                                                                Log.e("Error", e.getMessage());
                                                                            });
                                                });
                                                waitingDialog.dismiss();

                                            } catch (FileNotFoundException e) {
                                                errorHandler.printError("File not found. Please try again or restart the App.");
                                                e.printStackTrace();
                                                waitingDialog.dismiss();
                                            } catch (IndexOutOfBoundsException e) {
                                                errorHandler.printError("Something went wrong. No Classes found . Please try again or restart the App.");
                                                e.printStackTrace();
                                                waitingDialog.dismiss();
                                            } catch (Exception e) {
                                                errorHandler.printError("Something went wrong.Please try again or restart the App.");
                                                e.printStackTrace();
                                                waitingDialog.dismiss();
                                            }
                                        } else {
                                            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
                                            labeler.process(image).addOnSuccessListener(labels -> {
                                                final StringBuffer buffer = new StringBuffer();
                                                for (ImageLabel label : labels) {
                                                    buffer.append(label.getText()).append(". ");
                                                }
                                                TextRecognizer recognizer = TextRecognition.getClient();
                                                Task<Text> result =
                                                        recognizer.process(image)
                                                                .addOnSuccessListener(visionText -> {
                                                                    traducir(buffer.toString(), visionText.getText());
                                                                })
                                                                .addOnFailureListener(
                                                                        e -> {
                                                                            // Task failed with an exception
                                                                            // ...
                                                                        });
                                            }).addOnFailureListener(e -> {
                                                waitingDialog.dismiss();
                                                e.printStackTrace();
                                            });

                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }


                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                    Log.e("error", error.getMessage());
                    waitingDialog.dismiss();
                }
            });
        });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name);
        return app_folder_path;
    }




    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void traducir(String text, String ocr){
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.SPANISH)
                        .build();
        final Translator englishSpanishTranslator =
                Translation.getClient(options);

        englishSpanishTranslator.translate(text)
                .addOnSuccessListener(s -> {
                    if(ocr.equals("")){
                        TTS.speak(getString(R.string.str_encontre)+ s);
                    }else TTS.speak(getString(R.string.str_encontre)+ ", " + s + ", "+  getString(R.string.str_textofound) + ", " + ocr);
                })
                .addOnFailureListener(
                        (OnFailureListener) e -> TTS.speak("Error"));
        waitingDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}