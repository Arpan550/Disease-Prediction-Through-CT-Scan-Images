package com.example.medipred;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medipred.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.CardCtScan.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CTScanActivity.class);
            startActivity(intent);
        });
        binding.CardProfile.setOnClickListener(view -> {
            Intent intent= new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        binding.CardHistory.setOnClickListener(view->{
            Intent intent=new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        binding.CardChat.setOnClickListener(view->{
            Intent intent=new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }
}
