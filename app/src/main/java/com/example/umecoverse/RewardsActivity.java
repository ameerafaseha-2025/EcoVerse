
package com.example.umecoverse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class RewardsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvRewardPoints, tvRewardStreak;
    private Button btnRedeemCafe, btnRedeemFoodcourt, btnJoinSprint;

    private ListenerRegistration userListener;

    private String userId;
    private boolean isRedeeming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        userId = auth.getCurrentUser().getUid();

        tvRewardPoints = findViewById(R.id.tvRewardPoints);
        tvRewardStreak = findViewById(R.id.tvRewardStreak);

        btnRedeemCafe = findViewById(R.id.btnRedeemCafe);
        btnRedeemFoodcourt = findViewById(R.id.btnRedeemFoodcourt);
        btnJoinSprint = findViewById(R.id.btnJoinSprint);

        loadUserRewards();

        btnRedeemCafe.setOnClickListener(v ->
                showRedeemDialog("RM5 Cafe Voucher", 500));

        btnRedeemFoodcourt.setOnClickListener(v ->
                showRedeemDialog("RM10 Foodcourt Voucher", 1000));

        btnJoinSprint.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eco-Sprint")
                    .setMessage("Join the Plastic-Free Campus Sprint?\n\nComplete 5 recycling actions to earn a badge.")
                    .setPositiveButton("Join", (dialog, which) ->
                            Toast.makeText(this,
                                    "Eco-Sprint feature will be connected next!",
                                    Toast.LENGTH_SHORT).show())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadUserRewards() {

        DocumentReference userRef = db.collection("users")
                .document(userId);

        userListener = userRef.addSnapshotListener((document, error) -> {

            if (error != null) {
                Toast.makeText(this,
                        "Unable to load rewards.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (document != null && document.exists()) {

                Long points = document.getLong("ecoPoints");
                Long streak = document.getLong("streak");

                int currentPoints = points != null ? points.intValue() : 0;
                int currentStreak = streak != null ? streak.intValue() : 0;

                tvRewardPoints.setText(currentPoints + " EP");

                tvRewardStreak.setText(
                        "🔥 " + currentStreak +
                                "-day streak • Reach 5 days for 1.5× points!"
                );
            }
        });
    }

    private void showRedeemDialog(String voucherName, int requiredPoints) {

        new AlertDialog.Builder(this)
                .setTitle("Redeem Voucher")
                .setMessage("Redeem " + voucherName +
                        " for " + requiredPoints + " Eco Points?")
                .setPositiveButton("Redeem", (dialog, which) ->
                        redeemVoucher(voucherName, requiredPoints))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redeemVoucher(String voucherName, int requiredPoints) {

        if (isRedeeming) return;

        isRedeeming = true;
        btnRedeemCafe.setEnabled(false);
        btnRedeemFoodcourt.setEnabled(false);

        DocumentReference userRef = db.collection("users")
                .document(userId);

        DocumentReference redemptionRef = db.collection("redemptions")
                .document();

        db.runTransaction(transaction -> {

            DocumentSnapshot userDocument = transaction.get(userRef);

            if (!userDocument.exists()) {
                throw new IllegalStateException("User profile not found.");
            }

            Long pointsValue = userDocument.getLong("ecoPoints");

            int currentPoints = pointsValue != null
                    ? pointsValue.intValue() : 0;

            if (currentPoints < requiredPoints) {
                throw new IllegalStateException("NOT_ENOUGH_POINTS");
            }

            int remainingPoints = currentPoints - requiredPoints;

            Map<String, Object> redemption = new HashMap<>();
            redemption.put("userId", userId);
            redemption.put("voucherName", voucherName);
            redemption.put("pointsUsed", requiredPoints);
            redemption.put("status", "Pending");
            redemption.put("redeemedAt", FieldValue.serverTimestamp());

            transaction.update(userRef, "ecoPoints", remainingPoints);
            transaction.set(redemptionRef, redemption);

            return remainingPoints;

        }).addOnSuccessListener(remainingPoints -> {

            isRedeeming = false;
            enableRedeemButtons();

            new AlertDialog.Builder(this)
                    .setTitle("Redemption Successful!")
                    .setMessage(
                            voucherName +
                                    " has been redeemed successfully!\n\n" +
                                    "Remaining Points: " + remainingPoints + " EP\n\n" +
                                    "Status: Pending"
                    )
                    .setPositiveButton("OK", null)
                    .show();

        }).addOnFailureListener(error -> {

            isRedeeming = false;
            enableRedeemButtons();

            if ("NOT_ENOUGH_POINTS".equals(error.getMessage())) {

                new AlertDialog.Builder(this)
                        .setTitle("Insufficient Eco Points")
                        .setMessage("You do not have enough Eco Points to redeem this voucher.")
                        .setPositiveButton("OK", null)
                        .show();

            } else {

                Toast.makeText(this,
                        "Redemption failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enableRedeemButtons() {
        btnRedeemCafe.setEnabled(true);
        btnRedeemFoodcourt.setEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }
}
