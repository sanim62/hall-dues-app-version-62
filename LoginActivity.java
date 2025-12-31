package com.example.halldues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "HallDuesPrefs";
    private static final String KEY_USER_ID = "userId";

    private EditText etLoginId, etPassword;
    private Button btnLogin;
    private TextView tvRegisterNavigate;
    private FirebaseManager firebaseManager;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in.
        if (isUserLoggedIn()) {
            navigateToDashboard(getLoggedInUserId());
            return; // Skip the rest of the setup.
        }

        setContentView(R.layout.activity_login);

        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        etLoginId = findViewById(R.id.etLoginId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNavigate = findViewById(R.id.tvRegisterNavigate);

        // Handle pre-filled credentials from registration.
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("rollNumber")) {
            etLoginId.setText(intent.getStringExtra("rollNumber"));
        }

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegisterNavigate.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String loginId = etLoginId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (loginId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        firebaseManager.loginUser(loginId, password, new FirebaseManager.OnUserOperationListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Welcome, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                    saveLoginSession(user.getId());
                    navigateToDashboard(user.getId());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                });
            }
        });
    }

    private void saveLoginSession(String userId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    private boolean isUserLoggedIn() {
        // A user is logged in if their ID is saved and a Firebase user is active.
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null) != null && firebaseAuth.getCurrentUser() != null;
    }

    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    private void navigateToDashboard(String userId) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish(); // Prevent the user from returning to the login screen.
    }
}
