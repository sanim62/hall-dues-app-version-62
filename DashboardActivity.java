package com.example.halldues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "HallDuesPrefs";
    private static final String KEY_USER_ID = "userId";

    private FirebaseManager firebaseManager;
    private FirebaseAuth firebaseAuth;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    private User currentUser;

    private RecyclerView rvPaymentHistory;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<PaymentRecord> paymentRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        appDatabase = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = getLoggedInUserId();
        }

        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        loadUserData(userId);
        setupRecyclerView();
    }

    private void loadUserData(String userId) {
        // 1. Try to load from local database first for instant UI.
        executorService.execute(() -> {
            currentUser = appDatabase.userDao().getUserById(userId);
            if (currentUser != null) {
                runOnUiThread(this::setupDashboardUI);
            }

            // 2. Fetch fresh data from Firebase in the background.
            firebaseManager.getUserById(userId, new FirebaseManager.OnUserOperationListener() {
                @Override
                public void onSuccess(User freshUser) {
                    currentUser = freshUser;
                    // Save fresh data to local DB.
                    executorService.execute(() -> appDatabase.userDao().insertUser(freshUser));
                    // Update UI with fresh data.
                    runOnUiThread(DashboardActivity.this::setupDashboardUI);
                    fetchAndDisplayMealData(true); // Fetch fresh meal data
                }

                @Override
                public void onFailure(String error) {
                    if (currentUser == null) { // Only show error if local load also failed
                        runOnUiThread(() -> {
                            Toast.makeText(DashboardActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            navigateToLogin();
                        });
                    }
                }
            });
        });

        fetchAndDisplayMealData(false); // Load meals from local cache first
    }

    private void setupDashboardUI() {
        if (currentUser == null) return;

        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(currentUser.getFullName() + " (" + currentUser.getDepartment() + ")");

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        Button btnManageMeals = findViewById(R.id.btnManageMeals);
        btnManageMeals.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealManagementActivity.class);
            intent.putExtra("userId", currentUser.getId());
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        rvPaymentHistory = findViewById(R.id.rvPaymentHistory);
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));

        paymentRecords = new ArrayList<>();
        paymentRecords.add(new PaymentRecord("Jan 2025", 1755, 0, 50, 10, 20, 1835));
        paymentRecords.add(new PaymentRecord("Dec 2024", 1600, 10, 50, 10, 20, 1690));

        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentRecords);
        rvPaymentHistory.setAdapter(paymentHistoryAdapter);
    }

    private void fetchAndDisplayMealData(boolean fetchFromRemote) {
        if (currentUser == null) return;

        String yearMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

        if (fetchFromRemote) {
            firebaseManager.getMealRecordsForMonth(currentUser.getId(), yearMonth, new FirebaseManager.OnMealListListener() {
                @Override
                public void onSuccess(List<MealRecord> records) {
                    executorService.execute(() -> appDatabase.mealRecordDao().insertAll(records));
                    runOnUiThread(() -> populateMealGrid(records));
                }

                @Override
                public void onFailure(String error) {
                    // Don't show error, as local data might be available.
                }
            });
        } else {
            executorService.execute(() -> {
                List<MealRecord> records = appDatabase.mealRecordDao().getRecordsForMonth(currentUser.getId(), yearMonth);
                runOnUiThread(() -> populateMealGrid(records));
            });
        }
    }

    private void populateMealGrid(List<MealRecord> records) {
        GridLayout gridMealStatus = findViewById(R.id.gridMealStatus);
        gridMealStatus.removeAllViews();

        Map<String, MealRecord> recordsMap = new HashMap<>();
        for (MealRecord record : records) {
            if (record != null && record.getDate() != null) {
                recordsMap.put(record.getDate(), record);
            }
        }

        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String date = dateFormat.format(cal.getTime());
            String status = recordsMap.containsKey(date) ? recordsMap.get(date).getStatus() : "MEAL_ON";

            TextView dayView = new TextView(this);
            dayView.setText(String.valueOf(day));
            dayView.setGravity(Gravity.CENTER);
            dayView.setPadding(12, 12, 12, 12);

            if ("MEAL_OFF".equals(status)) {
                dayView.setBackgroundColor(Color.parseColor("#FEE2E2"));
            } else if ("HALL_CLOSED".equals(status)) {
                dayView.setBackgroundColor(Color.parseColor("#E2E8F0"));
            } else { // MEAL_ON
                dayView.setBackgroundColor(Color.parseColor("#D1FAE5"));
            }
            gridMealStatus.addView(dayView);
        }
    }

    private void logout() {
        firebaseAuth.signOut();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
