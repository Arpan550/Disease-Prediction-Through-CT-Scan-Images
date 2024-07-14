package com.example.medipred;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medipred.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // if already logged in, going to the main activity
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        binding.forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_reset_password, null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextInputEditText resetMail = dialogView.findViewById(R.id.input_emailET);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(dialogView);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString().trim();

                        if (TextUtils.isEmpty(mail)) {
                            Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("Sending reset link...");
                        progressDialog.show();

                        auth.sendPasswordResetEmail(mail)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();

                                    if (task.isSuccessful()) {
                                        Snackbar.make(findViewById(android.R.id.content),
                                                "Reset link successfully sent to your email",
                                                Snackbar.LENGTH_SHORT).show();

                                        // Handle Firestore update if needed
                                        FirebaseAuth.getInstance().getCurrentUser();
                                        if (auth.getCurrentUser() != null) {
                                            String userId = auth.getCurrentUser().getUid();
                                            FirebaseFirestore.getInstance().collection("users")
                                                    .document(userId)
                                                    .update("passwordReset", true)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("LoginActivity", "Password reset flag updated successfully in Firestore");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("LoginActivity", "Error updating password reset flag in Firestore", e);
                                                    });
                                        }
                                    } else {
                                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
                                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "User does not exist. Please check the email address.",
                                                    Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    errorMessage,
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        // Set click listener for login button
        binding.loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Navigate to SignUpActivity if user doesn't have an account
        binding.dontHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = binding.emailLoginET.getText().toString().trim();
        String password = binding.passwordLoginET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailLoginET.setError("Email is required");
            binding.emailLoginET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLoginET.setError("Password is required");
            binding.passwordLoginET.requestFocus();
            return;
        }

        // Authenticate user with Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Login successful, navigate to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Login failed, display error message
                            Toast.makeText(LoginActivity.this, "Authentication failed: "
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
