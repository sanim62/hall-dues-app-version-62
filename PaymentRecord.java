package com.example.halldues;

public class PaymentRecord {
    public String month;
    public double messing;
    public double fine;
    public double generator;
    public double water;
    public double misc;
    public double total;

    public PaymentRecord(String month, double messing, double fine, double generator, double water, double misc, double total) {
        this.month = month;
        this.messing = messing;
        this.fine = fine;
        this.generator = generator;
        this.water = water;
        this.misc = misc;
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public double getTotal() {
        return total;
    }
}
