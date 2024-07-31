package com.example.medipred.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SetDpViewModel extends AndroidViewModel {

    private final MutableLiveData<UploadStatus> uploadStatus = new MutableLiveData<>();
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final StorageReference storageRef;
    private Uri imageUri;
    private final Application application;

    public SetDpViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public LiveData<UploadStatus> getUploadStatus() {
        return uploadStatus;
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                setImageAndUpload(imageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                imageUri = getImageUriFromBitmap(bitmap);
                setImageAndUpload(imageUri);
            }
        }
    }

    private void setImageAndUpload(Uri uri) {
        uploadImageToFirebaseStorage(uri);
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(application.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public void uploadImageToFirebaseStorage(Uri uri) {
        if (uri != null) {
            StorageReference filePath = storageRef.child("profileImages/" + auth.getCurrentUser().getUid() + ".jpg");
            filePath.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        String photoUrl = downloadUrl.toString();
                        updateUserPhotoUrl(photoUrl);
                    }))
                    .addOnFailureListener(e -> uploadStatus.setValue(new UploadStatus(false, "Upload failed: " + e.getMessage(), null)));
        }
    }

    private void updateUserPhotoUrl(String photoUrl) {
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", photoUrl);

        firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> uploadStatus.setValue(new UploadStatus(true, "Profile picture updated successfully", photoUrl)))
                .addOnFailureListener(e -> uploadStatus.setValue(new UploadStatus(false, "Failed to update profile photo: " + e.getMessage(), null)));
    }

    public static class UploadStatus {
        private final boolean success;
        private final String message;
        private final String photoUrl; // Add photoUrl

        public UploadStatus(boolean success, String message, String photoUrl) {
            this.success = success;
            this.message = message;
            this.photoUrl = photoUrl;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
}
