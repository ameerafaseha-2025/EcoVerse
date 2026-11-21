

package com.example.umecoverse;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvProfileName, tvProfileEmail;
    private TextView tvProfileLevel, tvProfilePoints;
    private TextView tvProfileStreak, tvPersonalCarbon;
    private TextView tvCarbonGoal, tvCarbonPercentage;
    private TextView tvProfileBadges, tvProfileCollege;
    private TextView tvProfileCollegeRank;

    private ProgressBar progressCarbonGoal;

    private Spinner spinnerCollege;
    private Button btnSaveCollege;

    private String userId;
    private String userName = "UM Student";
    private boolean isLoadingCollege = false;

    private final List<String> collegeList = Arrays.asList(
            "Select Your College",
            "KK1",
            "KK2",
            "KK3",
            "KK4",
            "KK5",
            "KK6",
            "KK7",
            "KK8",
            "KK9",
            "KK10",
            "KK11",
            "KK12",
            "KK13"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        BottomNavHelper.setup(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connect XML components
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvProfilePoints = findViewById(R.id.tvProfilePoints);
        tvProfileStreak = findViewById(R.id.tvProfileStreak);
        tvPersonalCarbon = findViewById(R.id.tvPersonalCarbon);
        tvCarbonGoal = findViewById(R.id.tvCarbonGoal);
        tvCarbonPercentage = findViewById(R.id.tvCarbonPercentage);
        tvProfileBadges = findViewById(R.id.tvProfileBadges);
        tvProfileCollege = findViewById(R.id.tvProfileCollege);
        tvProfileCollegeRank = findViewById(R.id.tvProfileCollegeRank);

        progressCarbonGoal = findViewById(R.id.progressCarbonGoal);

        spinnerCollege = findViewById(R.id.spinnerCollege);
        btnSaveCollege = findViewById(R.id.btnSaveCollege);

        setupCollegeSpinner();

        btnSaveCollege.setOnClickListener(v -> saveCollege());

        loadUserProfile();
    }

    // Setup college dropdown
    private void setupCollegeSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                collegeList
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCollege.setAdapter(adapter);
    }

    // Load user profile from Firestore
    private void loadUserProfile() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        userId = user.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        displayUserProfile(document);

                        String savedCollege =
                                document.getString("college");

                        setSelectedCollege(savedCollege);

                    } else {

                        Toast.makeText(
                                this,
                                "User profile not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // Display profile information
    private void displayUserProfile(DocumentSnapshot document) {

        String email = document.getString("email");

        userName = getStringValue(
                document,
                "name",
                "UM Student"
        );

        tvProfileName.setText(userName);

        tvProfileEmail.setText(
                email != null ? email : "No email available"
        );

        tvProfileLevel.setText(
                "🌱 " + getStringValue(document, "level", "Beginner")
        );

        long points = getLongValue(document, "ecoPoints", 0);
        long streak = getLongValue(document, "streak", 0);

        tvProfilePoints.setText(points + " EP");
        tvProfileStreak.setText("🔥 " + streak + " Days");

        double carbonSaved = getDoubleValue(
                document,
                "carbonSaved",
                0
        );

        tvPersonalCarbon.setText(
                String.format(
                        java.util.Locale.US,
                        "%.1f kg CO₂",
                        carbonSaved
                )
        );

        // Carbon goal
        double carbonGoal = getDoubleValue(
                document,
                "carbonGoal",
                20
        );

        tvCarbonGoal.setText(
                "Monthly Goal: " + carbonGoal + " kg CO₂"
        );

        int percentage = carbonGoal > 0
                ? (int) Math.min(
                (carbonSaved / carbonGoal) * 100,
                100
        )
                : 0;

        progressCarbonGoal.setProgress(percentage);

        tvCarbonPercentage.setText(
                percentage + "% of monthly goal"
        );

        // Badges
        tvProfileBadges.setText(
                getStringValue(
                        document,
                        "badges",
                        "No achievements yet"
                )
        );

        // College
        String college = document.getString("college");

        tvProfileCollege.setText(
                college != null ? college : "Not set"
        );

        tvProfileCollegeRank.setText(
                "College ranking: " +
                        getStringValue(
                                document,
                                "collegeRank",
                                "Not available"
                        )
        );
    }

    // Set saved college in dropdown
    private void setSelectedCollege(String savedCollege) {

        if (savedCollege == null || savedCollege.trim().isEmpty()) {
            spinnerCollege.setSelection(0);
            return;
        }

        int position = collegeList.indexOf(savedCollege);

        if (position >= 0) {
            spinnerCollege.setSelection(position);
        } else {
            spinnerCollege.setSelection(0);
        }
    }

    // Save selected college to Firestore
    private void saveCollege() {

        if (userId == null) {
            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String selectedCollege =
                spinnerCollege.getSelectedItem().toString();

        if (selectedCollege.equals("Select Your College")) {

            Toast.makeText(
                    this,
                    "Please select your residential college",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSaveCollege.setEnabled(false);

        db.collection("users")
                .document(userId)
                .update("college", selectedCollege)
                .addOnSuccessListener(unused -> {

                    tvProfileCollege.setText(selectedCollege);

                    // Update leaderboard record too
                    Map<String, Object> leaderboardData =
                            new HashMap<>();

                    leaderboardData.put("name", userName);
                    leaderboardData.put("college", selectedCollege);

                    db.collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(document -> {

                                long points = getLongValue(
                                        document,
                                        "ecoPoints",
                                        0
                                );

                                leaderboardData.put(
                                        "ecoPoints",
                                        points
                                );

                                db.collection("leaderboard")
                                        .document(userId)
                                        .set(leaderboardData)
                                        .addOnSuccessListener(result -> {

                                            btnSaveCollege.setEnabled(true);

                                            Toast.makeText(
                                                    ProfileActivity.this,
                                                    "College saved successfully!",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        })
                                        .addOnFailureListener(e -> {

                                            btnSaveCollege.setEnabled(true);

                                            Toast.makeText(
                                                    ProfileActivity.this,
                                                    "College saved, but leaderboard update failed: "
                                                            + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            })
                            .addOnFailureListener(e -> {

                                btnSaveCollege.setEnabled(true);

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Could not refresh points: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    btnSaveCollege.setEnabled(true);

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to save college: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String getStringValue(
            DocumentSnapshot document,
            String field,
            String defaultValue
    ) {
        String value = document.getString(field);
        return value != null ? value : defaultValue;
    }

    private long getLongValue(
            DocumentSnapshot document,
            String field,
            long defaultValue
    ) {
        Long value = document.getLong(field);
        return value != null ? value : defaultValue;
    }

    private double getDoubleValue(
            DocumentSnapshot document,
            String field,
            double defaultValue
    ) {
        Number value = document.get(field) instanceof Number
                ? (Number) document.get(field)
                : null;

        return value != null ? value.doubleValue() : defaultValue;
    }
}
