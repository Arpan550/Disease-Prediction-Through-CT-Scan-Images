package com.example.medipred.repository;

import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.medipred.model.ScanData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScanDataRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public ScanDataRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void saveScanData(Bitmap img, String predictedLabel, String confidence, OnCompleteListener<Void> onCompleteListener) {
        // Convert Bitmap to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();
        String imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);

        // Create a timestamp
        Timestamp timestamp = Timestamp.now();

        // Format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd:HH:mm:ss", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date());

        // Prepare data
        Map<String, Object> scanData = new HashMap<>();
        scanData.put("image", imageBase64);
        scanData.put("predictedLabel", predictedLabel);
        scanData.put("confidence", confidence);
        scanData.put("timestamp", timestamp);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Save to Firestore
            db.collection("ct_scans")
                    .document(userId)
                    .collection("scans")
                    .document(formattedTimestamp)
                    .set(scanData)
                    .addOnCompleteListener(onCompleteListener);
        } else {
            // Handle user not authenticated
            TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
            taskCompletionSource.setException(new Exception("User not authenticated"));
            onCompleteListener.onComplete(taskCompletionSource.getTask());
        }
    }

    public Task<List<ScanData>> fetchScanData() {
        TaskCompletionSource<List<ScanData>> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("ct_scans")
                    .document(userId)
                    .collection("scans")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<ScanData> scanDataList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ScanData scanData = document.toObject(ScanData.class);
                                scanDataList.add(scanData);
                            }
                            taskCompletionSource.setResult(scanDataList);
                        } else {
                            taskCompletionSource.setException(task.getException());
                        }
                    });
        } else {
            taskCompletionSource.setException(new Exception("User not authenticated"));
        }
        return taskCompletionSource.getTask();
    }


}
