package com.mab.studentrequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton, registerButton;
    private EditText email, password;
    private View loader;
    private FirebaseAuth firebaseAuth;
    private String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setIds();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null)
            gotoMainActivity();
    }

    private void setIds() {
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loader = findViewById(R.id.loader);
        firebaseAuth = FirebaseAuth.getInstance();
    }


    private void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (performValidation()) {
                    loginUser();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }


    private boolean performValidation() {
        if (!isEmailValid(email.getText().toString())) {
            email.requestFocus();
            email.setError("Enter valid email");
            return false;
        }
        if (password.getText().toString().isEmpty() || password.getText().toString().length() < 6) {
            password.requestFocus();
            password.setError("Enter valid password min 6 char");
            return false;
        }
        return true;
    }


    private void gotoMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }


    private void loginUser() {
        loader.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loader.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            gotoMainActivity();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean isEmailValid(String email) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2}+[a-z]*");
        return emailPattern.matcher(email).matches() && !email.trim().isEmpty();
    }

}
