package com.example.medipred.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpViewModel extends ViewModel {

    private final MutableLiveData<RegistrationStatus> registrationStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false); // Add this line
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public SignUpViewModel() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<RegistrationStatus> getRegistrationStatus() {
        return registrationStatus;
    }

    public LiveData<Boolean> getIsLoading() { // Add this method
        return isLoading;
    }

    public void registerUser(String username, String email, String password, String phone, String gender, String dob, String country) {
        if (validateInput(username, email, password)) {
            isLoading.setValue(true); // Set loading to true
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("gender", gender);
                            user.put("dob", dob);
                            user.put("country", country);

                            firestore.collection("users").document(userId).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        isLoading.setValue(false); // Set loading to false
                                        registrationStatus.setValue(new RegistrationStatus(true, username, "Account created successfully"));
                                    })
                                    .addOnFailureListener(e -> {
                                        isLoading.setValue(false); // Set loading to false
                                        registrationStatus.setValue(new RegistrationStatus(false, "", "Failed to create user profile: " + e.getMessage()));
                                    });
                        } else {
                            isLoading.setValue(false); // Set loading to false
                            registrationStatus.setValue(new RegistrationStatus(false, "", "Error: " + task.getException().getMessage()));
                        }
                    });
        }
    }

    private boolean validateInput(String username, String email, String password) {
        // Add validation logic
        return true;
    }

    public static class RegistrationStatus {
        private final boolean success;
        private final String username;
        private final String message;

        public RegistrationStatus(boolean success, String username, String message) {
            this.success = success;
            this.username = username;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getUsername() {
            return username;
        }

        public String getMessage() {
            return message;
        }
    }
}
