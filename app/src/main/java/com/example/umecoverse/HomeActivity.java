
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // Open AI Scan from Home
        findViewById(R.id.btnStartScan).setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    ScanActivity.class
            );
            startActivity(intent);
        });

        // Open College Battle from Home
        findViewById(R.id.btnCollegeBattle).setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    CollegeBattleActivity.class
            );
            startActivity(intent);
        });

        // Bottom navigation: Home
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });

        // Bottom navigation: Ranks
        findViewById(R.id.navRanks).setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    CollegeBattleActivity.class
            ));
        });

        // Bottom navigation: Scan
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    ScanActivity.class
            ));
        });
        findViewById(R.id.navRewards).setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    RewardsActivity.class
            ));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            ));
        });
        findViewById(R.id.btnKnowledge).setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    KnowledgeActivity.class
            ));
        });
    }
}
