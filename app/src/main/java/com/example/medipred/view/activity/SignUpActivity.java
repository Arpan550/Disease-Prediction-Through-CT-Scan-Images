package com.example.medipred.view.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medipred.databinding.ActivitySignUpBinding;
import com.example.medipred.viewmodel.SignUpViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding signUpBinding;
    private SignUpViewModel viewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(signUpBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setCancelable(false);

        signUpBinding.signupBTN.setOnClickListener(v -> viewModel.registerUser(
                signUpBinding.nameET.getText().toString().trim(),
                signUpBinding.emailET.getText().toString().trim(),
                signUpBinding.passwordET.getText().toString().trim(),
                signUpBinding.phnoET.getText().toString().trim(),
                ((RadioButton)findViewById(signUpBinding.genderRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                signUpBinding.dobET.getText().toString().trim(),
                signUpBinding.countryCode.getSelectedCountryName()
        ));

        signUpBinding.alreadyHaveAcc.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));

        viewModel.getRegistrationStatus().observe(this, registrationStatus -> {
            if (registrationStatus.isSuccess()) {
                startActivity(new Intent(getApplicationContext(), SetDpActivity.class).putExtra("username", registrationStatus.getUsername()));
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, registrationStatus.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });
    }

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
