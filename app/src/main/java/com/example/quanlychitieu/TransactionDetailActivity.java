package com.example.quanlychitieu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.widget.ImageView;
import android.view.View;      // ✅ THÊM DÒNG NÀY
import android.widget.Toast;    // ✅ THÊM DÒNG NÀY

public class TransactionDetailActivity extends AppCompatActivity {
    private TextView categoryTextView, amountTextView, dateTextView, noteTextView;
    private Button editButton, deleteButton;
    private Transaction currentTransaction;
    private ImageView categoryIconImageView; // ✅ Thêm biến này

    private static final int EDIT_REQUEST_CODE = 101;
    public static final int RESULT_DELETED = 201;
    public static final int RESULT_EDITED = 202;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        categoryIconImageView = findViewById(R.id.detailCategoryIcon); // ✅ Ánh xạ icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Nhận dữ liệu từ MainActivity
        currentTransaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");

        // Ánh xạ View
        categoryTextView = findViewById(R.id.detailCategoryTextView);
        amountTextView = findViewById(R.id.detailAmountTextView);
        dateTextView = findViewById(R.id.detailDateTextView);
        noteTextView = findViewById(R.id.detailNoteTextView);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Hiển thị thông tin
        populateData();

        deleteButton.setOnClickListener(v -> confirmDelete());
        editButton.setOnClickListener(v -> openEditScreen());
    }

    private void populateData() {
        if (currentTransaction == null) {
            // Hiển thị thông báo lỗi hoặc đóng activity nếu không có dữ liệu
            Toast.makeText(this, "Lỗi: Không có dữ liệu giao dịch.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ BƯỚC 1: LẤY VÀ HIỂN THỊ ICON
        // Đảm bảo bạn đã ánh xạ categoryIconImageView trong onCreate
        categoryIconImageView.setImageResource(getIconForCategory(currentTransaction.getCategory()));

        // Hiển thị tên hạng mục
        categoryTextView.setText(currentTransaction.getCategory());

        // ✅ BƯỚC 2: CHỈ HIỂN THỊ DỮ LIỆU (Bỏ label thừa)
        // Hiển thị ngày (đã bỏ "Ngày: ")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateTextView.setText(sdf.format(currentTransaction.getDate()));

        // Hiển thị ghi chú (đã bỏ "Ghi chú: ")
        // Thêm kiểm tra null hoặc trống cho ghi chú
        String note = currentTransaction.getNote();
        if (note != null && !note.trim().isEmpty()) {
            noteTextView.setText(note);
            noteTextView.setVisibility(View.VISIBLE); // Hiện nếu có ghi chú
            // Tìm TextView label tương ứng và hiện nó lên (nếu bạn ẩn nó ban đầu)
            TextView noteLabel = findViewById(R.id.detailNoteLabelTextView);
            if(noteLabel != null) noteLabel.setVisibility(View.VISIBLE);
        } else {
            noteTextView.setVisibility(View.GONE); // Ẩn nếu không có ghi chú
            // Tìm TextView label tương ứng và ẩn nó đi
            TextView noteLabel = findViewById(R.id.detailNoteLabelTextView);
            if(noteLabel != null) noteLabel.setVisibility(View.GONE);
        }


        // Hiển thị số tiền (giữ nguyên)
        if (currentTransaction.isExpense()) {
            amountTextView.setText(String.format(Locale.getDefault(), "-%,.0f đ", currentTransaction.getAmount()));
            amountTextView.setTextColor(Color.RED);
        } else {
            amountTextView.setText(String.format(Locale.getDefault(), "+%,.0f đ", currentTransaction.getAmount()));
            amountTextView.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    private void openEditScreen() {
        // Mở màn hình Add/Edit tương ứng và gửi dữ liệu sang
        Intent intent;
        if (currentTransaction.isExpense()) {
            intent = new Intent(this, AddTransactionActivity.class);
        } else {
            intent = new Intent(this, AddIncomeActivity.class);
        }
        intent.putExtra("EDIT_TRANSACTION_DATA", currentTransaction);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("TRANSACTION_TO_DELETE", currentTransaction);
                    setResult(RESULT_DELETED, resultIntent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Nhận dữ liệu đã sửa về và gửi ngược lại MainActivity
            Transaction editedTransaction = (Transaction) data.getSerializableExtra("EDITED_TRANSACTION_DATA");
            if (editedTransaction != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("EDITED_TRANSACTION_DATA", editedTransaction);
                setResult(RESULT_EDITED, resultIntent);
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private int getIconForCategory(String category) {
        switch (category) {
            case "Ăn uống": return R.drawable.ic_eating;
            case "Mua sắm": return R.drawable.ic_shopping;
            case "Di chuyển": return R.drawable.ic_transport;
            case "Hóa đơn": return R.drawable.ic_bill;
            case "Giải trí": return R.drawable.ic_entertainment;
            case "Sức khỏe": return R.drawable.ic_health;
            case "Lương": case "Thưởng": case "Bán hàng": case "Quà tặng": case "Đầu tư":
                return R.drawable.ic_wallet;
            default: return R.drawable.ic_expense; // Icon mặc định
        }
    }
}