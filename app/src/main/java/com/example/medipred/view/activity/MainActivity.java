package com.example.medipred.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medipred.databinding.ActivityMainBinding;
import com.example.medipred.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.CardCtScan.setOnClickListener(view -> viewModel.onCtScanClicked());
        binding.CardProfile.setOnClickListener(view -> viewModel.onProfileClicked());
        binding.CardHistory.setOnClickListener(view -> viewModel.onHistoryClicked());
        binding.CardChat.setOnClickListener(view -> viewModel.onChatClicked());

        viewModel.getNavigationEvent().observe(this, navigationEvent -> {
            if (navigationEvent != null) {
                startActivity(new Intent(MainActivity.this, navigationEvent));
            }
        });
    }
}
