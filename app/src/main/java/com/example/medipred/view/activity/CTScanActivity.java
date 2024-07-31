package com.example.medipred.view.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medipred.R;
import com.example.medipred.databinding.ActivityCtscanBinding;
import com.example.medipred.ml.Model;
import com.example.medipred.viewmodel.ScanDataViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CTScanActivity extends AppCompatActivity {

    private static final int CAMERA_REQ_CODE = 100;
    private static final int GALLERY_REQ_CODE = 200;
    private static final int IMAGE_SIZE = 224;

    private ActivityCtscanBinding binding;
    private ScanDataViewModel scanDataViewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCtscanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        scanDataViewModel = new ViewModelProvider(this).get(ScanDataViewModel.class);

        initViews();
        setListeners();
        hideResults();

        binding.btnBack.setOnClickListener(view -> finish());

        scanDataViewModel.getIsSaving().observe(this, isSaving -> {
            if (isSaving) {
                progressDialog = new ProgressDialog(CTScanActivity.this);
                progressDialog.setMessage("Saving scan data...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            } else if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    private void initViews() {
        binding.btnChooseImage.setOnClickListener(view -> showImageSelectionDialog());
    }

    private void setListeners() {
        binding.btnSubmit.setOnClickListener(view -> {
            if (binding.imgPreview.getDrawable() != null) {
                Bitmap img = ((BitmapDrawable) binding.imgPreview.getDrawable()).getBitmap();
                processImage(img);
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideResults() {
        binding.btnChooseImage.setVisibility(View.VISIBLE);
        binding.btnBack.setVisibility(View.GONE);
        binding.btnSubmit.setVisibility(View.GONE);
        binding.classifier.setVisibility(View.GONE);
        binding.result.setVisibility(View.GONE);
    }

    private void showImageSelectionDialog() {
        Dialog dialog = new Dialog(CTScanActivity.this);
        dialog.setContentView(R.layout.select_img_dialog_layout);
        dialog.show();
        Button btnCamera = dialog.findViewById(R.id.Camera_Btn);
        Button btnGallery = dialog.findViewById(R.id.Gallery_Btn);

        btnCamera.setOnClickListener(view -> {
            Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(iCamera, CAMERA_REQ_CODE);
            dialog.dismiss();
        });

        btnGallery.setOnClickListener(view -> {
            Intent iGallery = new Intent(Intent.ACTION_PICK);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(iGallery, GALLERY_REQ_CODE);
            dialog.dismiss();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQ_CODE) {
                Bitmap img = (Bitmap) data.getExtras().get("data");
                processImage(img);
            } else if (requestCode == GALLERY_REQ_CODE) {
                if (data != null && data.getData() != null) {
                    try {
                        Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        processImage(img);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image from gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void processImage(Bitmap img) {
        int dimension = Math.min(img.getWidth(), img.getHeight());
        img = ThumbnailUtils.extractThumbnail(img, dimension, dimension);
        binding.imgPreview.setImageBitmap(img);

        binding.btnChooseImage.setVisibility(View.GONE);
        binding.btnBack.setVisibility(View.VISIBLE);
        binding.btnSubmit.setVisibility(View.VISIBLE);
        binding.classifier.setVisibility(View.VISIBLE);
        binding.result.setVisibility(View.VISIBLE);

        img = Bitmap.createScaledBitmap(img, IMAGE_SIZE, IMAGE_SIZE, false);
        classifyImage(img);
    }

    private void classifyImage(Bitmap img) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValue = new int[IMAGE_SIZE * IMAGE_SIZE];
            img.getPixels(intValue, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

            int pixel = 0;
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    int val = intValue[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++) {
                if (confidence[i] > maxConfidence) {
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }
            String[] classes = {"adenocarcinoma_left.lower.lobe_T2_N0_M0_Ib", "large.cell.carcinoma_left.hilum_T2_N2_M0_IIIa", "normal", "squamous.cell.carcinoma_left.hilum_T1_N2_M0_IIIa"};
            String predictedLabel = classes[maxPos];
            String confidenceStr = String.valueOf(maxConfidence);

            binding.result.setText(predictedLabel + "\n\nConfidence: " + confidenceStr);

            // Save to Firestore via ViewModel
            binding.btnSubmit.setOnClickListener(view -> {
                scanDataViewModel.saveScanData(img, predictedLabel, confidenceStr);
            });

            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

