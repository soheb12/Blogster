package com.example.skyscraper.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPasswordText;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        loginEmailText = findViewById(R.id.regEmail);
        loginPasswordText = findViewById(R.id.regPass);
        loginBtn = findViewById(R.id.regBtn);
        loginRegBtn = findViewById(R.id.loginRegBtn);
        mProgressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);

                String email = loginEmailText.getText().toString();
                String password = loginPasswordText.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    mProgressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                goToMain();
                            }
                            else
                            {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                            }

                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });//sign in with email & password
                }
            }
        });//login button clicked code

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
                finish();
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null)//as user is already logged in send him back to main page
        {
            Toast.makeText(LoginActivity.this," hereLogin " + user.getUid(),Toast.LENGTH_SHORT).show();

            goToMain();
        }
    }

    private void goToMain() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void hideKeyboard(View view)
    {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
