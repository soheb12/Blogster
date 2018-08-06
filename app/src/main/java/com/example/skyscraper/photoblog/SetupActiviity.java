package com.example.skyscraper.photoblog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActiviity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri = null;
    private EditText setupName;
    private Button setupBtn;
    private ProgressBar mProgressBar;

    private String userId;
    private boolean isChanged = false;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_activiity);

        android.support.v7.widget.Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = firebaseAuth.getCurrentUser().getUid();

        setupImage = findViewById(R.id.setupProfile);
        setupName = findViewById(R.id.setupName);
        setupBtn = findViewById(R.id.setupBtn);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String username = task.getResult().getString("name");
                        String imageUri = task.getResult().getString("image");

                        mainImageUri = Uri.parse(imageUri);

                        setupName.setText(username);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.mipmap.default_image);

                        Glide.with(SetupActiviity.this).setDefaultRequestOptions(placeHolderRequest).load(imageUri).into(setupImage);

                        Toast.makeText(SetupActiviity.this, "Data found" ,Toast.LENGTH_SHORT).show();

                    }else
                    {
                        Toast.makeText(SetupActiviity.this, "Data Not found" ,Toast.LENGTH_SHORT).show();

                    }
                }else{
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(SetupActiviity.this, errorMsg ,Toast.LENGTH_SHORT).show();
                }

                mProgressBar.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = setupName.getText().toString();


                if (!TextUtils.isEmpty(username) && mainImageUri != null) {

                    mProgressBar.setVisibility(View.VISIBLE);

                    if (isChanged) {


                        userId = firebaseAuth.getCurrentUser().getUid();

                        StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, username);

                                } else {
                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(SetupActiviity.this, errorMsg, Toast.LENGTH_LONG).show();

                                }
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        storeFirestore(null, username);
                    }


                }//textutils
                else
                {
                    if (TextUtils.isEmpty(username))
                        Toast.makeText(SetupActiviity.this, "UserName Can't Be Empty", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SetupActiviity.this, "Image Not Selected", Toast.LENGTH_SHORT).show();

                    mProgressBar.setVisibility(View.INVISIBLE);


                }

            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //after marshmello we need permission to access storage_write
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(SetupActiviity.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(SetupActiviity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
                        Toast.makeText(SetupActiviity.this,"Permission Denied",Toast.LENGTH_SHORT).show();

                    }else
                    {
                        Toast.makeText(SetupActiviity.this,"Permission Allowed",Toast.LENGTH_SHORT).show();
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActiviity.this);

                    }
                }else
                {
                    Toast.makeText(SetupActiviity.this,"Permission Allowed",Toast.LENGTH_SHORT).show();
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SetupActiviity.this);
                }

            }
        });
    }


    public void storeFirestore(Task<UploadTask.TaskSnapshot> task , String username)
    {

        Uri downloadUri = null;
        if(task == null)
        {
            downloadUri = mainImageUri;
        }
        else
        {
            downloadUri = task.getResult().getDownloadUrl();
        }


        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",username);
        userMap.put("image",downloadUri.toString());

        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(SetupActiviity.this,"Profile Updated",Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActiviity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                }else
                {
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(SetupActiviity.this, errorMsg ,Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void hideKeyboard(View view)
    {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
