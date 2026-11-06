
package com.example.umecoverse;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RewardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.btnRedeemCafe).setOnClickListener(v -> {
            showRedeemDialog("RM5 Cafe Voucher", 500);
        });

        findViewById(R.id.btnRedeemFoodcourt).setOnClickListener(v -> {
            showRedeemDialog("RM10 Foodcourt Voucher", 1000);
        });

        findViewById(R.id.btnJoinSprint).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eco-Sprint")
                    .setMessage("Join the Plastic-Free Campus Sprint?\n\nComplete 5 recycling actions to earn a badge.")
                    .setPositiveButton("Join", (dialog, which) -> {
                        Toast.makeText(this,
                                "Joined Eco-Sprint! (Demo)",
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showRedeemDialog(String voucherName, int points) {
        new AlertDialog.Builder(this)
                .setTitle("Redeem Voucher")
                .setMessage("Redeem " + voucherName +
                        " for " + points + " Eco Points?")
                .setPositiveButton("Redeem", (dialog, which) -> {
                    Toast.makeText(this,
                            "Redemption simulated (Demo)",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
