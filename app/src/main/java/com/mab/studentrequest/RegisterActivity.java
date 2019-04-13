package com.mab.studentrequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private Button loginButton, registerButton;
    private EditText email, password, name, number, aadhaar;
    private Spinner gender;
    private View loader;
    private FirebaseAuth firebaseAuth;
    private String TAG = RegisterActivity.class.getSimpleName();

    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setIds();
        setListeners();
    }

    private void setIds() {
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        aadhaar = findViewById(R.id.aadhar);
        loader = findViewById(R.id.loader);
        gender = findViewById(R.id.gender);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);

    }


    private void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));


            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (performValidation()) {
                    registerUser();
                }
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


    private void registerUser() {
        loader.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loader.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            addUsersDetails();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    private void addUsersDetails() {
        String key = firebaseDatabase.child("User").push().getKey();
        UserModel user = new UserModel();
        user.setEmail(firebaseAuth.getCurrentUser().getEmail());
        user.setName(name.getText().toString());
        user.setAadhaar(aadhaar.getText().toString());
        user.setGender(gender.getSelectedItem().toString());
        user.setNumber(number.getText().toString());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/User/" + key, user);
        firebaseDatabase.updateChildren(childUpdates);
        gotoMainActivity();
    }

    private boolean isEmailValid(String email) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2}+[a-z]*");
        return emailPattern.matcher(email).matches() && !email.trim().isEmpty();
    }
}
