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

public class TransactionDetailActivity extends AppCompatActivity {
    private TextView categoryTextView, amountTextView, dateTextView, noteTextView;
    private Button editButton, deleteButton;
    private Transaction currentTransaction;

    private static final int EDIT_REQUEST_CODE = 101;
    public static final int RESULT_DELETED = 201;
    public static final int RESULT_EDITED = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

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
        if (currentTransaction == null) return;

        categoryTextView.setText(currentTransaction.getCategory());
        noteTextView.setText("Ghi chú: " + currentTransaction.getNote());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateTextView.setText("Ngày: " + sdf.format(currentTransaction.getDate()));

        if (currentTransaction.isExpense()) {
            amountTextView.setText(String.format("-%,.0f đ", currentTransaction.getAmount()));
            amountTextView.setTextColor(Color.RED);
        } else {
            amountTextView.setText(String.format("+%,.0f đ", currentTransaction.getAmount()));
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
}