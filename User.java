package com.example.halldues;

public class User {

    private String id; // Firebase UID
    private String fullName;
    private String department;
    private String rollNumber;

    // Required for Firebase
    public User() {}

    public User(String fullName, String department, String rollNumber, String id) {
        this.fullName = fullName;
        this.department = department;
        this.rollNumber = rollNumber;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
