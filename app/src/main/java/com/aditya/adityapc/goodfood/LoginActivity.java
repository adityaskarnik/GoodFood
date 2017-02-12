package com.aditya.adityapc.goodfood;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by Aditya PC on 2/12/2017.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText loginEmail, loginPassword, signupEmail, signupPassword, signupPhoneNumber;
    Button signUpButton, loginButton, skipButton, signupSubmitButton, loginSubmitButton;
    LinearLayout loginLayout, signUpLayout, selectLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);
        signupEmail = (EditText) findViewById(R.id.signup_password);
        signupPassword = (EditText) findViewById(R.id.signup_password);
        signupPhoneNumber = (EditText) findViewById(R.id.signup_phoneNumber);

        signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(this);
        loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(this);
        skipButton = (Button) findViewById(R.id.button_skip);
        skipButton.setOnClickListener(this);
        signupSubmitButton = (Button) findViewById(R.id.signup_submit);
        signupSubmitButton.setOnClickListener(this);
        loginSubmitButton = (Button) findViewById(R.id.login_submit);
        loginSubmitButton.setOnClickListener(this);

        loginLayout = (LinearLayout) findViewById(R.id.login);
        signUpLayout = (LinearLayout) findViewById(R.id.signup);
        selectLayout = (LinearLayout) findViewById(R.id.selectLoginSignup);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sign_up :
                signUpLayout.setVisibility(View.VISIBLE);
            case R.id.button_login :
                loginLayout.setVisibility(View.VISIBLE);
            case R.id.button_skip :
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            case R.id.login_submit :
                loginEmail.getText().toString();
                loginPassword.getText().toString();
            case R.id.signup_submit :
                signupEmail.getText().toString();
                signupPassword.getText().toString();
                signupPhoneNumber.getText().toString();
        }
    }
}
