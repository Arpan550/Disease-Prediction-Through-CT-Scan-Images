package com.example.medipred;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<ScanData> scanDataList;

    public HistoryAdapter(List<ScanData> scanDataList) {
        this.scanDataList = scanDataList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_data, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScanData scanData = scanDataList.get(position);
        holder.predictedLabel.setText(scanData.getPredictedLabel());
        holder.confidence.setText(scanData.getConfidence());

        SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedDate = dateSdf.format(scanData.getTimestamp().toDate());
        String formattedTime = timeSdf.format(scanData.getTimestamp().toDate());
        holder.date.setText(formattedDate);
        holder.time.setText(formattedTime);

        byte[] decodedString = Base64.decode(scanData.getImage(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.imageView.setImageBitmap(decodedByte);
    }

    @Override
    public int getItemCount() {
        return scanDataList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView predictedLabel, confidence, date, time;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            predictedLabel = itemView.findViewById(R.id.predictedLabel);
            confidence = itemView.findViewById(R.id.confidence);
            date = itemView.findViewById(R.id.date);
            time= itemView.findViewById(R.id.time);
        }
    }
}
