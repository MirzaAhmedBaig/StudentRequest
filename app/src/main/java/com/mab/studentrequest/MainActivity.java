package com.mab.studentrequest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private TextView email;
    private Button log_out, upload;
    private View loader;


    private int permsRequestCode = 200;
    private int requestCode = 201;
    private String[] perms = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private FirebaseAuth firebaseAuth;

    private StorageReference firebaseStorage;

    private StorageReference documentRef;
    private Uri imageUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setIds();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        email.setText(firebaseAuth.getCurrentUser().getEmail());
        getUserInfo();
    }

    private void setIds() {
        log_out = findViewById(R.id.log_out);
        upload = findViewById(R.id.upload);
        email = findViewById(R.id.email);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        documentRef = firebaseStorage.child("documents");
        loader = findViewById(R.id.loader);
    }

    private void setListeners() {
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finishAffinity();

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private boolean checkForPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, perms, permsRequestCode);
            return false;
        } else {
            return true;
        }

    }

    private void openCamera() {
        if (checkForPermission()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                String imageurl = getRealPathFromURI(getContentResolver(), imageUri);
                String imageDBName = firebaseAuth.getCurrentUser().getEmail();
                createDBImage(imageurl, imageDBName);
                Log.d(TAG, "Path : $imageurl");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    private void createDBImage(String path, String keyName) {
        loader.setVisibility(View.VISIBLE);
        Uri file = Uri.fromFile(new File(path));
        StorageReference riversRef = documentRef.child(keyName + "__" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                loader.setVisibility(View.GONE);
                showAlert("Uploading Done.");
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loader.setVisibility(View.GONE);
                showAlert("Uploading Failed.");
            }
        });

    }


    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Status")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .show();
    }


    private String getRealPathFromURI(ContentResolver contentResolver, Uri uri) {
        String res = null;
        String[] proj = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, proj, null, null, null);
        if (cursor == null)
            return null;
        else {
            if (cursor.moveToFirst()) {
                res = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
            cursor.close();
            return res;
        }
    }

    private void getUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User");
        ref.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot datas : dataSnapshot.getChildren()) {
                    //here datas contain user info
                    String keys = datas.getKey();
                    Log.d(TAG, "Key " + keys);
                    Log.d(TAG, "Key " + datas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled " + databaseError.getMessage());
            }
        });
    }
}
