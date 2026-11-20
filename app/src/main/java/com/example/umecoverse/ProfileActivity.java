
package com.example.umecoverse;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        loadUserProfile();
    }

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

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {
                        displayUserProfile(document);
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

    private void displayUserProfile(DocumentSnapshot document) {

        String email = document.getString("email");

        tvProfileName.setText(
                getStringValue(document, "name", "UM Student")
        );

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
                String.format(java.util.Locale.US, "%.1f kg CO₂", carbonSaved)
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
                ? (int) Math.min((carbonSaved / carbonGoal) * 100, 100)
                : 0;

        progressCarbonGoal.setProgress(percentage);

        tvCarbonPercentage.setText(
                percentage + "% of monthly goal"
        );

        // Optional profile information
        tvProfileBadges.setText(
                getStringValue(
                        document,
                        "badges",
                        "No achievements yet"
                )
        );

        tvProfileCollege.setText(
                getStringValue(document, "college", "Not set")
        );

        tvProfileCollegeRank.setText(
                "College ranking: " +
                        getStringValue(document, "collegeRank", "Not available")
        );
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

