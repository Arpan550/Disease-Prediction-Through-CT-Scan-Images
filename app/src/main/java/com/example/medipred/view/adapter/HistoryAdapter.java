package com.example.medipred.view.adapter;

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

import com.example.medipred.R;
import com.example.medipred.model.ScanData;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ScanData> scanDataList;

    public void setScanDataList(List<ScanData> scanDataList) {
        this.scanDataList = scanDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanData scanData = scanDataList.get(position);
        holder.labelTextView.setText(scanData.getPredictedLabel());
        holder.confidenceTextView.setText("Confidence: " + scanData.getConfidence() + "%");
        holder.timestampTextView.setText("Date: " + scanData.getTimestamp().toDate().toString());

        // Handle image
        if (scanData.getImage() != null && !scanData.getImage().isEmpty()) {
            byte[] decodedString = Base64.decode(scanData.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        } else {
            holder.imageView.setImageResource(R.drawable.img); // Placeholder image
        }
    }

    @Override
    public int getItemCount() {
        return scanDataList == null ? 0 : scanDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelTextView;
        TextView confidenceTextView;
        TextView timestampTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.predictedLabel);
            confidenceTextView = itemView.findViewById(R.id.confidence);
            timestampTextView = itemView.findViewById(R.id.time_stamp);
            imageView = itemView.findViewById(R.id.imageView); // Initialize the ImageView
        }
    }
}
