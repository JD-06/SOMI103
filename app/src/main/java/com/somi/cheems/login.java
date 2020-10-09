package com.somi.cheems;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class login extends AppCompatActivity {


    private FirebaseAuth auth;
    private static final int GOOGLE_SIGN = 123;
    protected GoogleSignInClient googleSignInClient;
    private CallbackManager mCallbackManager;
    private static final String TAG = "login";
    private FirebaseUser prevUser, currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);
        setContentView(R.layout.activity_login);
        TextView btnlogin = findViewById(R.id.btnlogin);
        TextView btnsingin = findViewById(R.id.btnsingin);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogLogin();
            }
        });
        btnsingin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogRegister();
            }
        });
    }

    public void showAlertDialogLogin() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.login, null);
        builder.setView(customLayout);
        final EditText etemail = customLayout.findViewById(R.id.etemail);
        final EditText etpass = customLayout.findViewById(R.id.etpassword);
        CardView btnlog = customLayout.findViewById(R.id.btnlog);


        // create and show the alert dialog
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etemail.getText().toString(), password = etpass.getText().toString();
                if(!email.equals("")&&!password.equals("")){
                    loginUserEmail(email,password,dialog);
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.str_checkdata), Toast.LENGTH_SHORT).show();
                    TTS.speak(getString(R.string.str_checkdata));
                }
             
            }
        });
    }

    private void showAlertDialogRegister() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.singin, null);
        builder.setView(customLayout);
        final EditText etemailreg = customLayout.findViewById(R.id.etemailreg);
        final EditText etpassreg = customLayout.findViewById(R.id.etpasswordreg);
        final EditText etname = customLayout.findViewById(R.id.etname);
        final EditText etpassregconfirm = customLayout.findViewById(R.id.etpasswordregconfirm);
        CardView btnreg = customLayout.findViewById(R.id.btnreg);
        CardView btnreggoogle = customLayout.findViewById(R.id.btnreggoogle);


        // create and show the alert dialog
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etname.getText().toString(), email = etemailreg.getText().toString(), password = etpassreg.getText().toString(), passconfirm = etpassregconfirm.getText().toString();
                if(!name.isEmpty()&&!email.isEmpty()&&!password.isEmpty()&&!passconfirm.isEmpty()){
                    if(password.length() >= 6){
                        if(password.equals(passconfirm)){
                            registerUserEmail(email,password, name,dialog);
                        }else{
                            TTS.speak(getString(R.string.str_errorpass));
                            Toast.makeText(login.this, getString(R.string.str_errorpass), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        TTS.speak(getString(R.string.str_errorcaracteres));
                        Toast.makeText(login.this, getString(R.string.str_errorcaracteres), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    TTS.speak(getString(R.string.str_errordatos));
                    Toast.makeText(login.this, getString(R.string.str_errordatos), Toast.LENGTH_SHORT).show();
                }


            }
        });

        btnreggoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });


    }

    //Metodo para el login sando un correo
    private void loginUserEmail(String email, String password, final Dialog dialog){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent main = new Intent(login.this, MainActivity.class);
                    dialog.dismiss();
                    startActivity(main);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.str_checkdata), Toast.LENGTH_SHORT).show();
                    TTS.speak(getString(R.string.str_checkdata));
                }
            }
        });
    }
    //metodo para registrarlo atraves de correo
    private void registerUserEmail(final String email, String pass, final String name, final Dialog dialog){
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dialog.dismiss();
                    Intent main = new Intent(login.this, MainActivity.class);
                    startActivity(main);
                    finish();

                }else{
                    TTS.speak(getString(R.string.str_errorreg));
                    Toast.makeText(login.this, getString(R.string.str_errorreg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void SignInGoogle(){
        Intent SignIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(SignIntent,GOOGLE_SIGN);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account != null ) firebaseAuthWithGoogle(account);

            }catch (ApiException e){


                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent i = new Intent(login.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(login.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);


        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(login.this, "Success:" + user.getUid(),
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(login.this,MainActivity.class);
                            startActivity(i);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(login.this, "beep", Toast.LENGTH_SHORT).show();
                            if (task.getException().getMessage().equals(getString(R.string.user_exists))) {
                                linkWithExistingUser(credential);
                            } else {
                                Toast.makeText(login.this, "Authentication failed." + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }


                    }
                });
    }
    private void linkWithExistingUser(AuthCredential credential) {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        prevUser.linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(login.this, "Successfully Linked.",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(login.this,MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}