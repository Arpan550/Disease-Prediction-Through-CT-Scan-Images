package com.example.medipred.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medipred.model.ScanData;
import com.example.medipred.repository.ScanDataRepository;

import java.util.List;

public class ScanDataViewModel extends ViewModel {

    private final ScanDataRepository repository;
    private final MutableLiveData<List<ScanData>> scanDataList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ScanDataViewModel() {
        repository = new ScanDataRepository();
    }

    public LiveData<List<ScanData>> getScanDataList() {
        return scanDataList;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void saveScanData(Bitmap img, String predictedLabel, String confidence) {
        isSaving.setValue(true);
        repository.saveScanData(img, predictedLabel, confidence, task -> {
            if (task.isSuccessful()) {
                isSaving.setValue(false);
            } else {
                errorMessage.setValue("Error saving data: " + task.getException().getMessage());
                isSaving.setValue(false);
            }
        });
    }

    public void fetchScanData() {
        repository.fetchScanData().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ScanData> data = task.getResult();
                scanDataList.setValue(data);
            } else {
                errorMessage.setValue("Error getting documents: " + task.getException().getMessage());
            }
        });
    }
}
