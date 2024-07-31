package com.example.medipred.view.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medipred.R;
import com.example.medipred.model.ScanData;
import com.example.medipred.view.adapter.HistoryAdapter;
import com.example.medipred.viewmodel.ScanDataViewModel;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private ScanDataViewModel scanDataViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new HistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        scanDataViewModel = new ViewModelProvider(this).get(ScanDataViewModel.class);

        scanDataViewModel.getScanDataList().observe(this, scanDataList -> {
            if (scanDataList != null) {
                historyAdapter.setScanDataList(scanDataList);
                historyAdapter.notifyDataSetChanged();
            }
        });

        scanDataViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(HistoryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch scan data when activity is created
        scanDataViewModel.fetchScanData();
    }
}
