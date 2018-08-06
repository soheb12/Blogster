package com.example.skyscraper.photoblog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 128 ;
    private ImageView newPostImg;
    private EditText newPostDesc;
    private Button postBlogBtn;
    private Toolbar newPostToolbar;
    private ProgressBar newPostProgressBar;

    private Uri postImageUri = null;
    private String userId;
    private Bitmap compressedImageFile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Photo Blog");
        getSupportActionBar().setHomeButtonEnabled(true);

        newPostImg = findViewById(R.id.newPostImg);
        newPostDesc = findViewById(R.id.newPostDesc);
        postBlogBtn = findViewById(R.id.postBlogBtn);
        newPostProgressBar = findViewById(R.id.newPostProgressBar);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = mAuth.getCurrentUser().getUid();

        newPostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        postBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null )
                {
                    newPostProgressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            if(task.isSuccessful())
                            {


                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask thumbUploadTask = storageReference.child("post_images/thumbs").child(randomName + "jpg")
                                        .putBytes(thumbData);

                                thumbUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                        String thumbDownloadUri = taskSnapshot.getDownloadUrl().toString();

                                        Map<String,Object> postMap = new HashMap<>();

                                        postMap.put("image_uri" , downloadUri);
                                        postMap.put("thumb_uri" , thumbDownloadUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id",userId);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());


                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(NewPostActivity.this,"Uploaded to Firestore",Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(mainIntent);
                                                }
                                                else
                                                {
                                                    String errMsg = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this,"(Firestore Upload) : " + errMsg,Toast.LENGTH_SHORT).show();
                                                    newPostProgressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });

                                    }
                                });





                            }else
                            {
                                String errMsg = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this,"(Firestore Upload) : " + errMsg,Toast.LENGTH_SHORT).show();
                                newPostProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
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

                postImageUri = result.getUri();
                newPostImg.setImageURI(postImageUri);

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
