package com.example.medipred.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.medipred.databinding.ActivityProfileBinding;
import com.example.medipred.model.ScanData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding profileBinding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching profile...");
        progressDialog.setCancelable(false);

        // Fetch user profile data
        fetchUserProfile();

        profileBinding.signoutBTN.setOnClickListener(view -> signOut());
        profileBinding.downloadReportBTN.setOnClickListener(view -> downloadReport());
    }

    private void fetchUserProfile() {
        progressDialog.show();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Map<String, Object> userProfile = document.getData();
                            if (userProfile != null) {
                                updateUI(userProfile);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateUI(Map<String, Object> userProfile) {
        String name = (String) userProfile.get("username");
        String email = (String) userProfile.get("email");
        String phone = (String) userProfile.get("phone");
        String country = (String) userProfile.get("country");
        String photoUrl = (String) userProfile.get("photoUrl");

        profileBinding.headerName.setText("Name");
        profileBinding.nameTextView.setText(name);

        profileBinding.headerEmail.setText("Email");
        profileBinding.emailTextView.setText(email);

        profileBinding.headerPhone.setText("Phone Number");
        profileBinding.phoneTextView.setText(phone);


        profileBinding.headerCountry.setText("Country");
        profileBinding.countryTextView.setText(country);

        // Load the profile picture using Glide
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).into(profileBinding.profileImageView);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

    private void downloadReport() {
        progressDialog.setMessage("Generating report...");
        progressDialog.show();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetching user profile data
            db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDocument = task.getResult();
                        if (userDocument != null && userDocument.exists()) {
                            Map<String, Object> userProfile = userDocument.getData();

                            // Fetching CT scan data
                            db.collection("ct_scans").document(userId).collection("scans").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> scanDataTask) {
                                    if (scanDataTask.isSuccessful()) {
                                        List<ScanData> scanDataList = scanDataTask.getResult().toObjects(ScanData.class);

                                        // Generating the PDF report
                                        generatePDF(userProfile, scanDataList);

                                        progressDialog.dismiss();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed to fetch CT scan data", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ProfileActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void generatePDF(Map<String, Object> userProfile, List<ScanData> scanDataList) {
        // Creating a new PdfDocument instance
        PdfDocument pdfDocument = new PdfDocument();

        // Creating a page info with page size and margins
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        // Starting a page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Get canvas of the page
        android.graphics.Canvas canvas = page.getCanvas();

        // Creating a paint object
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Bold paint settings
        android.graphics.Paint.FontMetrics metrics = paint.getFontMetrics();
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

        // Initialize y position
        int y = 40;

        // Writing profile report heading and user details
        canvas.drawText("Profile Report", 40, y, paint);
        y += 20;


        // Reseting paint for regular text
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(12);

        // Writing user details to the PDF
        canvas.drawText("Name: " + userProfile.get("username"), 40, y, paint);
        y += 20;
        canvas.drawText("Email: " + userProfile.get("email"), 40, y, paint);
        y += 20;
        canvas.drawText("Phone: " + userProfile.get("phone"), 40, y, paint);
        y += 20;
        canvas.drawText("Gender: " + userProfile.get("gender"), 40, y, paint);
        y += 20;
        canvas.drawText("Date of Birth: " + userProfile.get("dob"), 40, y, paint);
        y += 20;
        canvas.drawText("Country: " + userProfile.get("country"), 40, y, paint);
        y += 40;

        // Bold paint settings for CT scan heading
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

        // Write CT scan results to the PDF
        canvas.drawText("CT Scan Results", 40, y, paint);
        y += 20;

        // Reseting paint for regular text
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(12);

        // Writing each scan result
        for (ScanData scanData : scanDataList) {
            canvas.drawText("Date: " + scanData.getTimestamp().toDate(), 40, y, paint);
            y += 20;
            canvas.drawText("Predicted Label: " + scanData.getPredictedLabel(), 40, y, paint);
            y += 20;
            canvas.drawText("Confidence: " + scanData.getConfidence(), 40, y, paint);
            y += 40;
        }

        // Finishing the page
        pdfDocument.finishPage(page);

        // Writing the document content to a file
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Profile_Report.pdf");
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(ProfileActivity.this, "PDF report generated successfully", Toast.LENGTH_SHORT).show();
            openPdfFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ProfileActivity.this, "Failed to generate PDF report", Toast.LENGTH_SHORT).show();
        }

        // Closing the document
        pdfDocument.close();
    }



    private void openPdfFile(File file) {
        // Opening the generated PDF file using an Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(ProfileActivity.this,
                getApplicationContext().getPackageName() + ".provider", file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

}
