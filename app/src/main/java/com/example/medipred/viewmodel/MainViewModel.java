package com.example.medipred.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medipred.view.activity.CTScanActivity;
import com.example.medipred.view.activity.ChatActivity;
import com.example.medipred.view.activity.HistoryActivity;
import com.example.medipred.view.activity.ProfileActivity;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Class<?>> navigationEvent = new MutableLiveData<>();

    public LiveData<Class<?>> getNavigationEvent() {
        return navigationEvent;
    }

    public void onCtScanClicked() {
        navigationEvent.setValue(CTScanActivity.class);
    }

    public void onProfileClicked() {
        navigationEvent.setValue(ProfileActivity.class);
    }

    public void onHistoryClicked() {
        navigationEvent.setValue(HistoryActivity.class);
    }

    public void onChatClicked() {
        navigationEvent.setValue(ChatActivity.class);
    }
}

