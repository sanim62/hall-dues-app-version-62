package com.example.halldues;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class HallDuesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Firebase database persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
