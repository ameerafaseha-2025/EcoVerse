
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your UM email");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user == null) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Unable to retrieve user information",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            String uid = user.getUid();

                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(document -> {

                                        if (document.exists()) {
                                            openHomePage();

                                        } else {

                                            Map<String, Object> profile =
                                                    new HashMap<>();

                                            profile.put("email", user.getEmail());
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
                                                    .addOnSuccessListener(unused ->
                                                            openHomePage()
                                                    )
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(
                                                                    MainActivity.this,
                                                                    "Profile save failed: "
                                                                            + e.getMessage(),
                                                                    Toast.LENGTH_LONG
                                                            ).show()
                                                    );
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    "Database error: "
                                                            + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );

                        } else {

                            String errorMessage =
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Unknown error";

                            Toast.makeText(
                                    MainActivity.this,
                                    "Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }

    private void openHomePage() {

        Toast.makeText(
                MainActivity.this,
                "Login successful!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                MainActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
        finish();
    }
}
