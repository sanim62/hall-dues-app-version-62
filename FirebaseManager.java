package com.example.halldues;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseManager {

    private static FirebaseManager instance;
    private final DatabaseReference usersRef;
    private final DatabaseReference mealsRef;
    private final FirebaseAuth firebaseAuth;

    private FirebaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = database.getReference("users");
        mealsRef = database.getReference("meals");
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public interface OnUserOperationListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnMealListListener {
        void onSuccess(List<MealRecord> records);
        void onFailure(String error);
    }

    public interface OnMealOperationListener {
        void onSuccess(MealRecord record);
        void onFailure(String error);
    }

    // ================= REGISTER USER =================
    public void registerUser(User user, String password, OnUserOperationListener listener) {
        String email = user.getRollNumber() + "@halldues.com";

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        user.setId(userId); // IMPORTANT: The User object's ID is now the Firebase UID.

                        usersRef.child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> listener.onSuccess(user))
                                .addOnFailureListener(e -> listener.onFailure("Failed to save user data: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Registration failed: " + e.getMessage()));
    }

    // ================= LOGIN USER =================
    public void loginUser(String rollNumber, String password, OnUserOperationListener listener) {
        String email = rollNumber + "@halldues.com";

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        getUserById(firebaseUser.getUid(), listener);
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Login failed: " + e.getMessage()));
    }

    // ================= GET USER BY ID =================
    public void getUserById(String userId, OnUserOperationListener listener) {
        // NOTE: The 'userId' parameter MUST be the Firebase Authentication UID.
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    user.setId(snapshot.getKey()); // Ensure the user object has the UID.
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User data not found in database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Database error: " + error.getMessage());
            }
        });
    }

    // ================= MEAL DATA =================
    public void saveMealRecord(MealRecord record, OnMealOperationListener listener) {
         String id = record.getUserId() + "_" + record.getDate();
        record.setId(id);

        mealsRef.child(id).setValue(record)
                .addOnSuccessListener(v -> listener.onSuccess(record))
                .addOnFailureListener(e -> listener.onFailure("Save failed: " + e.getMessage()));
    }

    public void getMealRecordsForMonth(String userId, String yearMonth, OnMealListListener listener) {

        Query query = mealsRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MealRecord> list = new ArrayList<>();
                if (!snapshot.exists()) {
                    if (listener != null)
                    listener.onSuccess(list); // Return an empty list
                    return;
                }
                for (DataSnapshot s : snapshot.getChildren()) {
                    MealRecord r = s.getValue(MealRecord.class);
                    if (r != null && r.getDate().startsWith(yearMonth)) {
                        list.add(r);
                    }
                }
                listener.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Database error: " + error.getMessage());
            }
        });
    }
}
