package com.example.medipred;

import com.google.firebase.Timestamp;

public class ScanData {
    private String image;
    private String predictedLabel;
    private String confidence;
    private Timestamp timestamp;

    public ScanData() {
        // Empty constructor needed for Firestore serialization
    }

    public ScanData(String image, String predictedLabel, String confidence, Timestamp timestamp) {
        this.image = image;
        this.predictedLabel = predictedLabel;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public String getConfidence() {
        return confidence;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
