
package com.example.umecoverse;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ScanActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView tvImageStatus;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        imgPreview = findViewById(R.id.imgPreview);
        tvImageStatus = findViewById(R.id.tvImageStatus);

        Button btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        Button btnScanVerify = findViewById(R.id.btnScanVerify);

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgPreview.setImageURI(uri);
                        tvImageStatus.setText("Photo selected");
                    }
                }
        );

        btnChoosePhoto.setOnClickListener(v ->
                imagePicker.launch("image/*")
        );

        btnScanVerify.setOnClickListener(v -> {

            if (selectedImageUri == null) {
                Toast.makeText(
                        this,
                        "Please choose a photo first",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                showDemoResultSelector();
            }
        });
    }

    private void showDemoResultSelector() {

        String[] results = {
                "Successful Scan",
                "Blurry Image",
                "Duplicate Submission",
                "Low Confidence"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Demo: Choose Scan Result")
                .setItems(results, (dialog, which) -> {

                    if (which == 0) {
                        showSuccessPopup();
                    } else if (which == 1) {
                        showErrorPopup("blurry");
                    } else if (which == 2) {
                        showErrorPopup("duplicate");
                    } else {
                        showErrorPopup("confidence");
                    }

                })
                .show();
    }

    private void showSuccessPopup() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scan_success);

        TextView tvDetectedMaterial =
                dialog.findViewById(R.id.tvDetectedMaterial);
        TextView tvConfidence =
                dialog.findViewById(R.id.tvConfidence);
        TextView tvPointsEarned =
                dialog.findViewById(R.id.tvPointsEarned);
        TextView tvMultiplier =
                dialog.findViewById(R.id.tvMultiplier);
        TextView tvEcoFact =
                dialog.findViewById(R.id.tvEcoFact);

        Button btnContinue =
                dialog.findViewById(R.id.btnScanContinue);

        // Sample data for frontend demo only
        tvDetectedMaterial.setText("Plastic Bottle");
        tvConfidence.setText("Confidence: 96%");
        tvPointsEarned.setText("+50 EP");
        tvMultiplier.setText("Streak bonus: Not active");

        tvEcoFact.setText(
                "Recycling plastic helps reduce waste " +
                        "and supports a circular economy."
        );

        btnContinue.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources()
                            .getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void showErrorPopup(String type) {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scan_error);

        TextView title = dialog.findViewById(R.id.tvErrorTitle);
        TextView subtitle = dialog.findViewById(R.id.tvErrorSubtitle);
        TextView message = dialog.findViewById(R.id.tvErrorMessage);
        Button retry = dialog.findViewById(R.id.btnRetryScan);

        if (type.equals("blurry")) {

            title.setText("Blurry Image");
            subtitle.setText(
                    "We couldn't get a clear view of your item."
            );
            message.setText(
                    "Make sure your item is well-lit and in focus."
            );
            retry.setText("Retake Photo");

        } else if (type.equals("duplicate")) {

            title.setText("Duplicate Submission");
            subtitle.setText(
                    "This image may have been submitted before."
            );
            message.setText(
                    "Please upload a new photo of a different eco action."
            );
            retry.setText("Choose Another Photo");

        } else {

            title.setText("Low Confidence");
            subtitle.setText(
                    "We couldn't confidently identify the material."
            );
            message.setText(
                    "Try another photo with better lighting."
            );
            retry.setText("Try Again");
        }

        retry.setOnClickListener(v -> {
            dialog.dismiss();
            imagePicker.launch("image/*");
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources()
                            .getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
