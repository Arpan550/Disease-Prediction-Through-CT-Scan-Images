package com.example.medipred;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.medipred.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        progressDialog.show(); // Show progress dialog while fetching data

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    progressDialog.dismiss(); // Dismiss progress dialog once data fetching is complete

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
        String gender = (String) userProfile.get("gender");
        String dob = (String) userProfile.get("dob");
        String country = (String) userProfile.get("country");
        String photoUrl = (String) userProfile.get("photoUrl");

        profileBinding.headerName.setText("Name");
        profileBinding.nameTextView.setText(name);

        profileBinding.headerEmail.setText("Email");
        profileBinding.emailTextView.setText(email);

        profileBinding.headerPhone.setText("Phone Number");
        profileBinding.phoneTextView.setText(phone);

        profileBinding.headerGender.setText("Gender");
        profileBinding.genderTextView.setText(gender);

        profileBinding.headerDob.setText("Date of Birth");
        profileBinding.dobTextView.setText(dob);

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
        finish(); // Close the current activity to prevent returning to it with the back button
    }

    private void downloadReport() {
        // Implement the download report functionality
        Toast.makeText(this, "Download report feature coming soon", Toast.LENGTH_SHORT).show();
    }
}
