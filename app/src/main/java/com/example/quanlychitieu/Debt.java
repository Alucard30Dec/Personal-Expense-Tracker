package com.example.quanlychitieu;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Date;
import java.io.Serializable;

@Entity(tableName = "debts_table")
@TypeConverters(Converters.class) // Reuse date converter
public class Debt implements Serializable{

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    private String name; // Tên khoản vay/nợ (VD: Vay tiền học, Cho bạn A mượn)

    private double amount; // Số tiền

    private boolean isLending; // true = Cho vay (mình là chủ nợ), false = Đi vay (mình là con nợ)

    private Date dueDate; // Ngày đáo hạn (nếu có)

    private boolean isPaid; // Đã trả/thu hồi xong chưa?

    public Debt (@NonNull String name, double amount, boolean isLending, Date dueDate, boolean isPaid) {
        this.name = name;
        this.amount = amount;
        this.isLending = isLending;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    // --- Getters ---
    public int getId() { return id; }
    @NonNull public String getName() { return name; }
    public double getAmount() { return amount; }
    public boolean isLending() { return isLending; }
    public Date getDueDate() { return dueDate; }
    public boolean isPaid() { return isPaid; }

    // --- Setters (Cần cho việc cập nhật) ---
    public void setPaid(boolean paid) { isPaid = paid; }
    // Có thể thêm các setter khác nếu cần sửa thông tin
}