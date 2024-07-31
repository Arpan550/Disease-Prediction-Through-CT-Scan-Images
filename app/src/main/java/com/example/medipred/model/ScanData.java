package com.example.medipred.model;

import com.google.firebase.Timestamp;

public class ScanData {
    private String image;
    private String predictedLabel;
    private String confidence;
    private Timestamp timestamp;

    // Empty constructor for Firestore serialization
    public ScanData() {}

    public ScanData(String image, String predictedLabel, String confidence, Timestamp timestamp) {
        this.image = image;
        this.predictedLabel = predictedLabel;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(String predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
