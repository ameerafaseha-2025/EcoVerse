
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class CollegeBattleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_college_battle);

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // View College Map
        findViewById(R.id.btnViewCollegeMap).setOnClickListener(v -> {
            startActivity(new Intent(
                    CollegeBattleActivity.this,
                    CollegeMapActivity.class
            ));
        });

        // Create Eco Challenge
        findViewById(R.id.btnCreateChallenge).setOnClickListener(v -> {
            startActivity(new Intent(
                    CollegeBattleActivity.this,
                    CreateChallengeActivity.class
            ));
        });
    }
}
