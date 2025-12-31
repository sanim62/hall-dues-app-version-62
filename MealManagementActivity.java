package com.example.halldues;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealManagementActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private User currentUser;

    private Calendar currentCalendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat monthYearFormat;

    private TextView tvStudentName, tvRollNo, tvDepartment;
    private TextView tvMealsOn, tvMealsOff, tvHallClosed;
    private TextView tvMonthYear;
    private GridLayout gridCalendar;
    private Button btnPrev, btnNext, btnUpdateChanges;

    private Map<String, MealRecord> mealRecordsMap;
    private Map<String, CardView> dateCardMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_management);

        firebaseManager = FirebaseManager.getInstance();
        currentCalendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        mealRecordsMap = new HashMap<>();
        dateCardMap = new HashMap<>();

        initializeViews();
        loadUserData();
    }

    private void initializeViews() {
        tvStudentName = findViewById(R.id.tvStudentName);
        tvRollNo = findViewById(R.id.tvRollNo);
        tvDepartment = findViewById(R.id.tvDepartment);

        tvMealsOn = findViewById(R.id.tvMealsOn);
        tvMealsOff = findViewById(R.id.tvMealsOff);
        tvHallClosed = findViewById(R.id.tvHallClosed);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        gridCalendar = findViewById(R.id.gridCalendar);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnUpdateChanges = findViewById(R.id.btnUpdateChanges);

        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnUpdateChanges.setEnabled(false);

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadMealData();
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadMealData();
        });

        btnUpdateChanges.setOnClickListener(v -> saveAllChanges());
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = firebaseUser.getUid();

        firebaseManager.getUserById(userId, new FirebaseManager.OnUserOperationListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    currentUser = user;

                    tvStudentName.setText(user.getFullName());
                    tvRollNo.setText(user.getRollNumber());
                    tvDepartment.setText(user.getDepartment());

                    btnPrev.setEnabled(true);
                    btnNext.setEnabled(true);
                    btnUpdateChanges.setEnabled(true);

                    loadMealData();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MealManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void loadMealData() {
        if (currentUser == null) return;

        String yearMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(currentCalendar.getTime());

        firebaseManager.getMealRecordsForMonth(
                currentUser.getId(), // Always use the Firebase UID
                yearMonth,
                new FirebaseManager.OnMealListListener() {
                    @Override
                    public void onSuccess(List<MealRecord> records) {
                        runOnUiThread(() -> {
                            mealRecordsMap.clear();
                            for (MealRecord r : records) {
                                mealRecordsMap.put(r.getDate(), r);
                            }
                            buildCalendar();
                            updateStats();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(MealManagementActivity.this, "Failed to load meals: " + error, Toast.LENGTH_LONG).show();
                            buildCalendar();
                            updateStats();
                        });
                    }
                }
        );
    }

    private void buildCalendar() {
        gridCalendar.removeAllViews();
        dateCardMap.clear();

        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String d : days) {
            TextView header = new TextView(this);
            header.setText(d);
            header.setGravity(Gravity.CENTER);
            header.setTextColor(Color.parseColor("#64748B"));
            gridCalendar.addView(header);
        }

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDay; i++) {
            gridCalendar.addView(new View(this));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String date = dateFormat.format(cal.getTime());

            CardView card = createDayCard(day, date);
            dateCardMap.put(date, card);
            gridCalendar.addView(card);
        }
    }

    private CardView createDayCard(int day, String date) {
        CardView card = new CardView(this);
        card.setRadius(8);
        card.setCardElevation(2);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        TextView tvDay = new TextView(this);
        tvDay.setText(String.valueOf(day));
        tvDay.setTextSize(18);

        TextView tvStatus = new TextView(this);
        tvStatus.setTextSize(11);

        MealRecord record = mealRecordsMap.get(date);
        String status = (record != null) ? record.getStatus() : "MEAL_ON";

        updateCardAppearance(card, tvStatus, status);

        card.setOnClickListener(v -> {
            String currentStatus = "MEAL_ON";
            if (mealRecordsMap.containsKey(date)) {
                currentStatus = mealRecordsMap.get(date).getStatus();
            }

            String newStatus = getNextStatus(currentStatus);

            // CORRECT: Always use the Firebase UID (currentUser.getId()) when creating a new record.
            MealRecord newRecord = new MealRecord(currentUser.getId(), date, newStatus);
            mealRecordsMap.put(date, newRecord);

            updateCardAppearance(card, tvStatus, newStatus);
            updateStats();
        });

        layout.addView(tvDay);
        layout.addView(tvStatus);
        card.addView(layout);

        return card;
    }

    private String getNextStatus(String s) {
        switch (s) {
            case "MEAL_ON": return "MEAL_OFF";
            case "MEAL_OFF": return "HALL_CLOSED";
            default: return "MEAL_ON";
        }
    }

    private void updateCardAppearance(CardView card, TextView tv, String s) {
        // ... (Appearance code is correct, no changes needed)
    }

    private void updateStats() {
        // ... (Stats calculation is correct, no changes needed)
    }

    private void saveAllChanges() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnUpdateChanges.setEnabled(false);
        btnUpdateChanges.setText("SAVING...");

        if (mealRecordsMap.isEmpty()) {
            Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show();
            btnUpdateChanges.setEnabled(true);
            btnUpdateChanges.setText("UPDATE CHANGES");
            return;
        }

        final int totalRecords = mealRecordsMap.values().size();
        final int[] successCount = {0};
        final int[] errorCount = {0};

        for (MealRecord record : mealRecordsMap.values()) {
            firebaseManager.saveMealRecord(record, new FirebaseManager.OnMealOperationListener() {
                @Override
                public void onSuccess(MealRecord savedRecord) {
                    handleSaveCallback(true);
                }

                @Override
                public void onFailure(String error) {
                    handleSaveCallback(false);
                }

                private void handleSaveCallback(boolean success) {
                    if (success) successCount[0]++; else errorCount[0]++;

                    if (successCount[0] + errorCount[0] == totalRecords) {
                        runOnUiThread(() -> {
                            if (errorCount[0] > 0) {
                                Toast.makeText(MealManagementActivity.this, "Saved with " + errorCount[0] + " errors.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MealManagementActivity.this, "All changes saved successfully!", Toast.LENGTH_SHORT).show();
                            }
                            btnUpdateChanges.setEnabled(true);
                            btnUpdateChanges.setText("UPDATE CHANGES");
                        });
                    }
                }
            });
        }
    }
}
