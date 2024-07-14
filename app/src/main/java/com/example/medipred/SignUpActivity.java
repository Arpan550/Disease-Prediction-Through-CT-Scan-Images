package com.example.medipred;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medipred.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding signUpBinding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(signUpBinding.getRoot());

        // Initialize FirebaseAuth instance and progress dialog
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        signUpBinding.signupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        signUpBinding.alreadyHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    private void registerUser() {
        final String username = signUpBinding.nameET.getText().toString().trim();
        final String email = signUpBinding.emailET.getText().toString().trim();
        String password = signUpBinding.passwordET.getText().toString().trim();
        final String phone = signUpBinding.phnoET.getText().toString().trim();
        // Get selected gender
        int selectedGenderId = signUpBinding.genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString();
        final String dob = signUpBinding.dobET.getText().toString().trim();
        final String country = signUpBinding.countryCode.getSelectedCountryName();

        if (TextUtils.isEmpty(username)) {
            signUpBinding.nameET.setError("Username is required");
            signUpBinding.nameET.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            signUpBinding.emailET.setError("Email is required");
            signUpBinding.emailET.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            signUpBinding.passwordET.setError("Password is required");
            signUpBinding.passwordET.requestFocus();
            return;
        }
        if (password.length() < 6) {
            signUpBinding.passwordET.setError("Password must be at least 6 characters long");
            signUpBinding.passwordET.requestFocus();
            return;
        }

        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                        // Store additional user data in Firestore
                        String userId = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("gender", gender);
                        user.put("dob", dob);
                        user.put("country", country);

                        firestore.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("SignUpActivity", "User profile is created for " + userId);
                                    startActivity(new Intent(getApplicationContext(), SetDpActivity.class)
                                            .putExtra("username", username));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this, "Failed to create user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to show DatePickerDialog
    public void showDatePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date to the EditText
                        signUpBinding.dobET.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

}