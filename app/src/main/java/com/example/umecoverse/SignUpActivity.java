
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etSignUpName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSignUp.setOnClickListener(v -> registerUser());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim()
                .toLowerCase();
        String password = etPassword.getText().toString();
        String confirmPassword =
                etConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            etName.setError("Please enter your full name");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Please enter your UM email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Enforce UM student email
        if (!email.endsWith("@siswa.um.edu.my")) {
            etEmail.setError(
                    "Only @siswa.um.edu.my email is allowed"
            );
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must be at least 6 characters"
            );
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> profile = new HashMap<>();

                        profile.put("email", email);
                        profile.put("ecoPoints", 0);
                        profile.put("ecoScore", 0);
                        profile.put("level", "Beginner");
                        profile.put("streak", 0);
                        profile.put("carbonSaved", 0);
                        profile.put(
                                "createdAt",
                                FieldValue.serverTimestamp()
                        );

                        db.collection("users")
                                .document(uid)
                                .set(profile)
                                .addOnSuccessListener(unused -> {

                                    db.collection("users")
                                            .document(uid)
                                            .update("name", name)
                                            .addOnSuccessListener(result -> {

                                                Toast.makeText(
                                                        SignUpActivity.this,
                                                        "Account created successfully!",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                Intent intent = new Intent(
                                                        SignUpActivity.this,
                                                        HomeActivity.class
                                                );

                                                startActivity(intent);
                                                finish();

                                            })
                                            .addOnFailureListener(e -> {
                                                btnSignUp.setEnabled(true);
                                                Toast.makeText(
                                                        SignUpActivity.this,
                                                        "Name save failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    btnSignUp.setEnabled(true);
                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Profile save failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        btnSignUp.setEnabled(true);

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unknown error";

                        Toast.makeText(
                                SignUpActivity.this,
                                "Registration failed: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
