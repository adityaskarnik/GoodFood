package com.aditya.goodfood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

/**
 * Created by Aditya PC on 2/12/2017.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText loginEmail, loginPassword, signupEmail, signupPassword/*, signupPhoneNumber*/;
    Button signUpButton, loginButton, /*skipButton,*/ signupSubmitButton, loginSubmitButton, loginCancelButton, signupCancelButton;
    LinearLayout loginLayout, signUpLayout, selectLayout;
    private FirebaseAuth auth;
    SpotsDialog progressDialog;
    static final AppMsg.Style error = AppMsg.STYLE_ALERT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        progressDialog = new SpotsDialog(LoginActivity.this, "Please wait..");
        progressDialog.setCancelable(false);
        loginEmail = (EditText) findViewById(R.id.login_email);
        loginEmail.setTypeface(Typeface.SERIF);
        loginPassword = (EditText) findViewById(R.id.login_password);
        loginPassword.setTypeface(Typeface.SERIF);
        signupEmail = (EditText) findViewById(R.id.signup_email);
        signupEmail.setTypeface(Typeface.SERIF);
        signupPassword = (EditText) findViewById(R.id.signup_password);
        signupPassword.setTypeface(Typeface.SERIF);
        //signupPhoneNumber = (EditText) findViewById(R.id.signup_phoneNumber);

        signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(this);
        loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(this);
        //skipButton = (Button) findViewById(R.id.button_skip);
        //skipButton.setOnClickListener(this);
        signupSubmitButton = (Button) findViewById(R.id.signup_submit);
        signupSubmitButton.setOnClickListener(this);
        loginSubmitButton = (Button) findViewById(R.id.login_submit);
        loginSubmitButton.setOnClickListener(this);
        loginCancelButton = (Button) findViewById(R.id.login_cancel);
        loginCancelButton.setOnClickListener(this);
        signupCancelButton = (Button) findViewById(R.id.signup_cancel);
        signupCancelButton.setOnClickListener(this);

        loginLayout = (LinearLayout) findViewById(R.id.login);
        signUpLayout = (LinearLayout) findViewById(R.id.signup);
        selectLayout = (LinearLayout) findViewById(R.id.selectLoginSignup);

    }

    @Override
    public void onClick(View view) {
        final String email, password;
        switch (view.getId()) {
            case R.id.button_sign_up :
                signUpLayout.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.GONE);
                break;
            case R.id.button_login :
                loginLayout.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.GONE);
                break;
            /*case R.id.button_skip :
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
                //finish();
                break;*/
            case R.id.login_submit :
                if (isNetworkAvailable()) {
                    progressDialog.show();
                    email = loginEmail.getText().toString();
                    password = loginPassword.getText().toString();
                    if (email.equals("")) {
                        signupEmail.setError("Email address cannot be blank");
                    }
                    if (!isValidEmail(email)) {
                        signupEmail.setError("Invalid email address");
                    } else if (password.length() < 6) {
                        loginPassword.setError("Minimum 6 characters");
                    } else
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        //progressBar.setVisibility(View.GONE);
                                        if (!task.isSuccessful()) {
                                            // there was an error
                                            progressDialog.dismiss();
                                            if (password.length() < 6) {
                                                loginPassword.setError("Minimum 6 characters");
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                } else {
                    AppMsg appMsg = AppMsg.makeText(LoginActivity.this, "Network not available", error);
                    appMsg.show();
                }
                break;
            case R.id.signup_submit :
                if (isNetworkAvailable()) {
                    progressDialog.show();
                    email = signupEmail.getText().toString();
                    password = signupPassword.getText().toString();
                    //String contact = signupPhoneNumber.getText().toString();
                    if (email.equals("")) {
                        signupEmail.setError("Email address cannot be blank");
                    }
                    if (!isValidEmail(email)) {
                        signupEmail.setError("Invalid email address");
                    } else if (password.length() < 6) {
                        loginPassword.setError("Minimum 6 characters");
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Toast.makeText(LoginActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        //progressBar.setVisibility(View.GONE);
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, "Registration failed " + task.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            progressDialog.dismiss();
                                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                            finish();
                                        }
                                    }
                                });
                    }
                } else {
                    AppMsg appMsg = AppMsg.makeText(LoginActivity.this, "Network not available", error);
                    appMsg.show();
                }
                break;
            case R.id.login_cancel :
                loginLayout.setVisibility(View.GONE);
                selectLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.signup_cancel :
                signUpLayout.setVisibility(View.GONE);
                selectLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
