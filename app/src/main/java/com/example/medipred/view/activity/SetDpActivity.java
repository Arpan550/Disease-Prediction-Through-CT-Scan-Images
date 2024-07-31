package com.example.medipred.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.medipred.R;
import com.example.medipred.databinding.ActivitySetDpBinding;
import com.example.medipred.viewmodel.SetDpViewModel;

public class SetDpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private ActivitySetDpBinding setDpBinding;
    private SetDpViewModel viewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private int currentRequestCode;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDpBinding = ActivitySetDpBinding.inflate(getLayoutInflater());
        setContentView(setDpBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(SetDpViewModel.class);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        String name = getIntent().getStringExtra("username");
        setDpBinding.textViewHelloUser.setText("Hello, " + name + " !");

        setDpBinding.addProfileImg.setOnClickListener(v -> showImageSourceDialog());

        setDpBinding.buttonNext.setOnClickListener(v -> {
            startActivity(new Intent(SetDpActivity.this, MainActivity.class));
            finish();
        });

        viewModel.getUploadStatus().observe(this, uploadStatus -> {
            if (uploadStatus.isSuccess()) {
                // Success: Dismiss ProgressDialog and update UI
                progressDialog.dismiss();
                Toast.makeText(SetDpActivity.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                Glide.with(this)
                        .load(uploadStatus.getPhotoUrl()) // Load the updated image
                        .into(setDpBinding.addProfileImg);
            } else {
                // Failure: Dismiss ProgressDialog and show error
                progressDialog.dismiss();
                Toast.makeText(SetDpActivity.this, "Failed: " + uploadStatus.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    viewModel.handleActivityResult(currentRequestCode, result.getResultCode(), data);
                }
            }
        });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentRequestCode = CAMERA_REQUEST;
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            imagePickerLauncher.launch(cameraIntent);
                            break;
                        case 1:
                            currentRequestCode = PICK_IMAGE_REQUEST;
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            imagePickerLauncher.launch(galleryIntent);
                            break;
                    }
                })
                .show();
    }

    private void setImageAndUpload(Uri uri) {
        Glide.with(this)
                .load(uri)
                .into(setDpBinding.addProfileImg);

        // Show ProgressDialog before starting the upload
        progressDialog.show();

        viewModel.uploadImageToFirebaseStorage(uri);
    }
}
