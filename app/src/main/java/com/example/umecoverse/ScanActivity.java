
package com.example.umecoverse;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView tvImageStatus;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePicker;

    private FirebaseAuth auth;
    private EcoRewardManager rewardManager;

    private WasteClassifier classifier;
    private Button btnScanVerify;

    private static final float MIN_CONFIDENCE = 0.70f;

    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        BottomNavHelper.setup(this);

        auth = FirebaseAuth.getInstance();

        imgPreview = findViewById(R.id.imgPreview);
        tvImageStatus = findViewById(R.id.tvImageStatus);

        Button btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnScanVerify = findViewById(R.id.btnScanVerify);

        // Initialize Firestore reward manager
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            rewardManager = new EcoRewardManager(userId);
        }

        // Load AI model
        try {
            classifier = new WasteClassifier(this);
        } catch (IOException e) {
            Toast.makeText(
                    this,
                    "Failed to load AI model: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }

        // Image picker
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
                runScan();
            }
        });
    }

    // ---------- AI SCAN ----------

    private void runScan() {

        if (isScanning) {
            return;
        }

        if (classifier == null) {
            Toast.makeText(
                    this,
                    "AI model not loaded",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Bitmap bitmap = loadBitmap(selectedImageUri);

        if (bitmap == null) {
            Toast.makeText(
                    this,
                    "Could not read the image",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        isScanning = true;
        btnScanVerify.setEnabled(false);

        try {
            WasteClassifier.Result result = classifier.classify(bitmap);

            if (result.confidence < MIN_CONFIDENCE) {
                showErrorPopup("confidence");
            } else {
                calculateScanPoints(result.label, result.confidence);
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Scan failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            finishScanning();
        } finally {
            bitmap.recycle();
        }
    }

    private Bitmap loadBitmap(Uri uri) {

        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;

            try (InputStream in =
                         getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            int sample = 1;

            while (bounds.outWidth / sample > 1024
                    || bounds.outHeight / sample > 1024) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;

            try (InputStream in =
                         getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(in, null, opts);
            }

        } catch (IOException e) {
            return null;
        }
    }

    // ---------- POINTS & FIRESTORE ----------

    private void calculateScanPoints(String material, float confidence) {

        if (auth.getCurrentUser() == null || rewardManager == null) {
            Toast.makeText(
                    this,
                    "Please log in first.",
                    Toast.LENGTH_SHORT
            ).show();

            finishScanning();
            return;
        }

        // Only these materials are eligible for rewards
        if (!isRecyclableMaterial(material)) {

            Toast.makeText(
                    this,
                    material + " detected. This material is not eligible for points.",
                    Toast.LENGTH_LONG
            ).show();

            finishScanning();
            return;
        }

        // Award points and update streak through Firestore transaction
        rewardManager.awardSuccessfulScan()
                .addOnSuccessListener(reward -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    showSuccessPopup(
                            material,
                            confidence,
                            reward.streak,
                            reward.pointsEarned,
                            reward.multiplierApplied
                    );

                    finishScanning();
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Failed to save reward: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    finishScanning();
                });
    }

    private boolean isRecyclableMaterial(String material) {

        if (material == null) {
            return false;
        }

        String normalized = material.trim().toLowerCase(Locale.ROOT);

        return normalized.equals("plastic")
                || normalized.equals("paper")
                || normalized.equals("glass")
                || normalized.equals("aluminium")
                || normalized.equals("aluminum");
    }

    private void finishScanning() {
        isScanning = false;
        btnScanVerify.setEnabled(true);
    }

    // ---------- SUCCESS POPUP ----------

    private void showSuccessPopup(
            String material,
            float confidence,
            int currentStreak,
            int pointsEarned,
            boolean multiplierActive) {

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

        tvDetectedMaterial.setText(material);

        tvConfidence.setText(
                "Confidence: " + Math.round(confidence * 100) + "%"
        );

        tvPointsEarned.setText("+" + pointsEarned + " EP");

        if (multiplierActive) {
            tvMultiplier.setText(
                    "🔥 " + currentStreak
                            + "-day streak bonus: 1.5× active!"
            );
        } else {
            tvMultiplier.setText(
                    "🔥 " + currentStreak
                            + "-day streak. Reach 5 days for 1.5×!"
            );
        }

        tvEcoFact.setText(getEcoFact(material));

        btnContinue.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        resizeDialog(dialog);
    }

    // ---------- ERROR POPUP ----------

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
            subtitle.setText("We couldn't get a clear view of your item.");
            message.setText(
                    "Make sure your item is well-lit and in focus."
            );
            retry.setText("Retake Photo");

        } else if (type.equals("duplicate")) {

            title.setText("Duplicate Submission");
            subtitle.setText("This image may have been submitted before.");
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

        dialog.setOnDismissListener(dialogInterface -> finishScanning());

        dialog.show();
        resizeDialog(dialog);
    }

    // ---------- DIALOG SIZE ----------

    private void resizeDialog(Dialog dialog) {

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.90
                    ),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // ---------- ECO FACTS ----------

    private String getEcoFact(String material) {

        switch (material.toLowerCase(Locale.ROOT)) {

            case "plastic":
                return "Recycling plastic helps reduce waste and supports a circular economy.";

            case "aluminium":
            case "aluminum":
                return "Recycling aluminium saves up to 95% of the energy needed to make it new.";

            case "glass":
                return "Glass can be recycled endlessly without losing quality or purity.";

            case "paper":
                return "Recycling one tonne of paper can save about 17 trees.";

            default:
                return "Every item you recycle helps protect our planet.";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (classifier != null) {
            classifier.close();
        }
    }
}
