package com.example.halldues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "HallDuesPrefs";
    private static final String KEY_USER_ID = "userId";

    private FirebaseManager firebaseManager;
    private FirebaseAuth firebaseAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

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
    }

    private void loadUserData(String userId) {
        firebaseManager.getUserById(userId, new FirebaseManager.OnUserOperationListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    currentUser = user;
                    setupDashboardUI();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this, "Error loading user data: " + error, Toast.LENGTH_LONG).show();
                    navigateToLogin();
                });
            }
        });
    }

    private void setupDashboardUI() {
        if (currentUser == null) return;

        // 1. Populate User Details
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(currentUser.getFullName() + " (" + currentUser.getDepartment() + ")");

        // 2. Setup Buttons
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        // Note: You need to add this Button to your activity_dashboard.xml layout file.
        Button btnManageMeals = findViewById(R.id.btnManageMeals);
        btnManageMeals.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealManagementActivity.class);
            intent.putExtra("userId", currentUser.getId());
            startActivity(intent);
        });

        // 3. Display Data
        displayPaymentHistory();
        fetchAndDisplayMealData();
    }

    private void fetchAndDisplayMealData() {
        String yearMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

        firebaseManager.getMealRecordsForMonth(currentUser.getId(), yearMonth, new FirebaseManager.OnMealListListener() {
            @Override
            public void onSuccess(List<MealRecord> records) {
                runOnUiThread(() -> populateMealGrid(records));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Could not load meal data.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void populateMealGrid(List<MealRecord> records) {
        GridLayout gridMealStatus = findViewById(R.id.gridMealStatus);
        gridMealStatus.removeAllViews();

        Map<String, MealRecord> recordsMap = new HashMap<>();
        for (MealRecord r : records) {
            recordsMap.put(r.getDate(), r);
        }

        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String date = dateFormat.format(cal.getTime());
            String status = recordsMap.containsKey(date) ? recordsMap.get(date).getStatus() : "MEAL_ON";

            TextView dayView = makeText(String.valueOf(day));
            dayView.setGravity(Gravity.CENTER);
            dayView.setPadding(12, 12, 12, 12);

            if ("MEAL_OFF".equals(status)) {
                dayView.setBackgroundColor(Color.parseColor("#FEE2E2"));
            } else if ("HALL_CLOSED".equals(status)) {
                dayView.setBackgroundColor(Color.parseColor("#E2E8F0"));
            } else {
                dayView.setBackgroundColor(Color.parseColor("#D1FAE5"));
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            dayView.setLayoutParams(params);

            gridMealStatus.addView(dayView);
        }
    }


    private void displayPaymentHistory() {
        TableLayout tablePayments = findViewById(R.id.tablePayments);
        addTableHeader(tablePayments);

        PaymentRecord[] records = {
                new PaymentRecord("Jan 2025", 1755, 0, 50, 10, 20, 1835),
                new PaymentRecord("Dec 2024", 1600, 10, 50, 10, 20, 1690)
        };

        for (PaymentRecord r : records) {
            TableRow row = new TableRow(this);
            row.addView(makeText(r.month));
            row.addView(makeText(String.valueOf((int)r.messing)));
            row.addView(makeText(r.fine > 0 ? String.valueOf((int)r.fine) : "-"));
            row.addView(makeText(String.valueOf((int)(r.generator + r.water + r.misc))));
            row.addView(makeText(String.valueOf((int)r.total)));
            tablePayments.addView(row);
        }
    }

    private void addTableHeader(TableLayout table) {
        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#0284C7"));

        String[] headers = {"Month", "Messing", "Fine", "Others", "Total"};
        for (String h : headers) {
            TextView tv = makeText(h);
            tv.setTextColor(Color.WHITE);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(16, 12, 16, 12);
            header.addView(tv);
        }
        table.addView(header);
    }

    private TextView makeText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 12, 12, 12);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#1E293B"));
        return tv;
    }

    private void logout() {
        firebaseAuth.signOut(); // Sign out from Firebase

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply(); // Clear saved session

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
