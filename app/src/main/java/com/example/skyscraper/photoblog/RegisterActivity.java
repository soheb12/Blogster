package com.example.skyscraper.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmail;
    private EditText regPass;
    private EditText regConfirmPass;
    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regConfirmPass = findViewById(R.id.regPassConfirm);
        regBtn = findViewById(R.id.regBtn);
        regLoginBtn = findViewById(R.id.regLoginBtn);
        mProgressBar = findViewById(R.id.progressBar);

        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTo(LoginActivity.class);
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = regEmail.getText().toString();
                String pass = regPass.getText().toString();
                String confirmPass = regConfirmPass.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass))
                {
                    if(pass.equals(confirmPass))
                    {
                        mProgressBar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful())
                                {
                                    sendTo(SetupActiviity.class);
                                }
                                else
                                {
                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
                                }
                            }

                        });//create user

                        mProgressBar.setVisibility(View.INVISIBLE);
                    }else
                    {
                        Toast.makeText(RegisterActivity.this,"Passwords Dont Match",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null)
        {
            sendTo(MainActivity.class);
        }
    }

    private void sendTo(Class c) {
        Intent intent = new Intent(RegisterActivity.this , c );
        startActivity(intent);
    }

    public void hideKeyboard(View view)
    {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
