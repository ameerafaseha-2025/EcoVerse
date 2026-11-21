
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HomeActivity extends AppCompatActivity {

    private TextView tvEcoPoints;
    private TextView tvStreak;
    private TextView tvEcoScore;
    private TextView tvCarbonSaved;

    private ProgressBar progressStreak;
    private ProgressBar progressEcoScore;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connect TextViews
        tvEcoPoints = findViewById(R.id.tvEcoPoints);
        tvStreak = findViewById(R.id.tvStreak);
        tvEcoScore = findViewById(R.id.tvEcoScore);
        tvCarbonSaved = findViewById(R.id.tvCarbonSaved);

        // Connect ProgressBars
        progressStreak = findViewById(R.id.progressStreak);
        progressEcoScore = findViewById(R.id.progressEcoScore);

        // Start Eco Scan
        findViewById(R.id.btnStartScan).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );

        // College Battle
        findViewById(R.id.btnCollegeBattle).setOnClickListener(v ->
                startActivity(new Intent(this, CollegeBattleActivity.class))
        );

        // Bottom Navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });

        findViewById(R.id.navRanks).setOnClickListener(v ->
                startActivity(new Intent(this, CollegeBattleActivity.class))
        );

        findViewById(R.id.navScan).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );

        findViewById(R.id.navRewards).setOnClickListener(v ->
                startActivity(new Intent(this, RewardsActivity.class))
        );

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        // Knowledge Hub
        findViewById(R.id.btnKnowledge).setOnClickListener(v ->
                startActivity(new Intent(this, KnowledgeActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserData();
    }

    private void loadUserData() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "Please log in first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        userListener = db.collection("users")
                .document(userId)
                .addSnapshotListener((document, error) -> {

                    if (error != null) {
                        Toast.makeText(this,
                                "Failed to sync: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (document == null || !document.exists()) {
                        return;
                    }

                    // ECO POINTS
                    Long points = document.getLong("ecoPoints");
                    int ecoPoints = points != null ? points.intValue() : 0;

                    tvEcoPoints.setText(ecoPoints + " EP");

                    // RECYCLING STREAK
                    // Same streak field used by ProfileActivity
                    Long streak = document.getLong("streak");
                    int currentStreak = streak != null ? streak.intValue() : 0;

                    tvStreak.setText(currentStreak + " Days");

                    progressStreak.setProgress(
                            Math.min(currentStreak, 5)
                    );

                    // ECO SCORE
                    Long score = document.getLong("ecoScore");
                    int ecoScore = score != null ? score.intValue() : 0;

                    tvEcoScore.setText(ecoScore + "/100");

                    progressEcoScore.setProgress(
                            Math.min(ecoScore, 100)
                    );

                    // CARBON SAVED
                    Object carbonValue = document.get("carbonSaved");

                    double carbonSaved = 0;

                    if (carbonValue instanceof Number) {
                        carbonSaved = ((Number) carbonValue).doubleValue();
                    }

                    tvCarbonSaved.setText(
                            String.format(
                                    java.util.Locale.getDefault(),
                                    "%.2f kg CO₂",
                                    carbonSaved
                            )
                    );
                });
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
