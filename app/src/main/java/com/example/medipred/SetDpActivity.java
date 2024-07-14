package com.example.medipred;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.medipred.databinding.ActivitySetDpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class SetDpActivity extends AppCompatActivity {

    private ActivitySetDpBinding setDpBinding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDpBinding = ActivitySetDpBinding.inflate(getLayoutInflater());
        setContentView(setDpBinding.getRoot());

        String name = getIntent().getStringExtra("username");
        setDpBinding.textViewHelloUser.setText("Hello, " + name + " !");

        // Initialize FirebaseAuth, FirebaseFirestore, and StorageReference
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUser = auth.getCurrentUser();

        // Progress dialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        // Upload profile picture to Firebase Storage
        setDpBinding.addProfileImg.setOnClickListener(v -> chooseImageSource());

        // Continue button click listener
        setDpBinding.buttonNext.setOnClickListener(v -> {
            startActivity(new Intent(SetDpActivity.this, MainActivity.class));
            finish();
        });
    }

    private void chooseImageSource() {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_choose_image, null);
        TextView textGallery = dialogView.findViewById(R.id.textGallery);
        TextView textTakePhoto = dialogView.findViewById(R.id.textTakePhoto);
        TextView textCancel = dialogView.findViewById(R.id.textCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        textGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            dialog.dismiss();
        });

        textTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            dialog.dismiss();
        });

        textCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Set dialog position and animations
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_GALLERY:
                    if (data != null) {
                        imageUri = data.getData();
                        setImageAndUpload(imageUri);
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        imageUri = getImageUri(bitmap);
                        setImageAndUpload(imageUri);
                    }
                    break;
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void setImageAndUpload(Uri uri) {
        Glide.with(this)
                .load(uri)
                .into(setDpBinding.addProfileImg);
        uploadImageToFirebaseStorage(uri);
    }

    private void uploadImageToFirebaseStorage(Uri uri) {
        progressDialog.show();
        StorageReference filePath = storageRef.child("profileImages").child(currentUser.getUid());
        filePath.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    Toast.makeText(SetDpActivity.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                    filePath.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String photoUrl = downloadUri.toString();
                        // Update Firestore document with the new photoUrl
                        updateUserPhotoUrl(photoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SetDpActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserPhotoUrl(String photoUrl) {
        // Update Firestore document with the new photoUrl
        firestore.collection("users").document(currentUser.getUid())
                .update("photoUrl", photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SetDpActivity.this, "Photo URL updated successfully", Toast.LENGTH_SHORT).show();
                    // Optionally perform any other actions upon successful update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SetDpActivity.this, "Error updating photo URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
}
