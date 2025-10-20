package com.example.quanlychitieu;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "transactions_table") // Đánh dấu đây là một bảng
public class Transaction implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String category;
    private String note;
    private double amount;
    private boolean isExpense;
    private Date date;

    public Transaction(String category, String note, double amount, boolean isExpense, Date date) {
        this.category = category;
        this.note = note;
        this.amount = amount;
        this.isExpense = isExpense;
        this.date = date;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public boolean isExpense() { return isExpense; }
    public Date getDate() { return date; }
}