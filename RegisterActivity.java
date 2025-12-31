package com.example.halldues;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etFullName, etDepartment, etRoll, etPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseManager = FirebaseManager.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etDepartment = findViewById(R.id.etDepartment);
        etRoll = findViewById(R.id.etRoll);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> handleRegistration());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() || department.isEmpty() || roll.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        User newUser = new User(fullName, department, roll, ""); // Password is not stored in the database directly

        firebaseManager.registerUser(newUser, password, new FirebaseManager.OnUserOperationListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("rollNumber", roll);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                });
            }
        });
    }
}
