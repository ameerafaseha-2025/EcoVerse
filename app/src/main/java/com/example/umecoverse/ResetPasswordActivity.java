package com.example.umecoverse;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etResetEmail;
    private Button btnSendResetLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etResetEmail = findViewById(R.id.etResetEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);

        btnSendResetLink.setOnClickListener(v -> {

            String email = etResetEmail.getText()
                    .toString().trim();

            // Check empty email
            if (email.isEmpty()) {
                etResetEmail.setError("Please enter your email");
                etResetEmail.requestFocus();
                return;
            }

            // Check valid email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etResetEmail.setError("Enter a valid email address");
                etResetEmail.requestFocus();
                return;
            }

            // Firebase integration will be handled by Deja
            Toast.makeText(
                    this,
                    "Email validated. Password reset integration pending.",
                    Toast.LENGTH_LONG
            ).show();
        });
    }
}
