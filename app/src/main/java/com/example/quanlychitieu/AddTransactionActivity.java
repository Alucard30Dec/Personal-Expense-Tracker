package com.example.quanlychitieu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText amountEditText, noteEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private TextView dateTextView;
    private Calendar selectedDateCalendar;
    private LinearLayout suggestionLayout;
    private Button suggestion1, suggestion2, suggestion3;
    private Transaction transactionToEdit = null; // Biến để lưu giao dịch cần sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm Chi tiêu");
        }

        // --- Ánh xạ các View ---
        amountEditText = findViewById(R.id.amountEditText);
        noteEditText = findViewById(R.id.noteEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        dateTextView = findViewById(R.id.dateTextView);
        suggestionLayout = findViewById(R.id.suggestionLayout);
        suggestion1 = findViewById(R.id.suggestion1);
        suggestion2 = findViewById(R.id.suggestion2);
        suggestion3 = findViewById(R.id.suggestion3);

        // --- Cài đặt các chức năng ---
        setupDatePicker();
        setupCategorySpinner();
        setupAmountSuggestions();
        saveButton.setOnClickListener(v -> saveTransaction());
        // KIỂM TRA XEM CÓ DỮ LIỆU SỬA ĐƯỢC GỬI SANG KHÔNG
        if (getIntent().hasExtra("EDIT_TRANSACTION_DATA")) {
            transactionToEdit = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION_DATA");
            getSupportActionBar().setTitle("Sửa Chi tiêu");
            saveButton.setText("Cập nhật");
            prefillData();
        }
    }
    private void prefillData() {
        if (transactionToEdit == null) return;
        amountEditText.setText(String.valueOf((long)transactionToEdit.getAmount()));
        noteEditText.setText(transactionToEdit.getNote());

        // Đặt ngày đã lưu
        selectedDateCalendar.setTime(transactionToEdit.getDate());
        updateDateInView();

        // Chọn đúng hạng mục trong Spinner
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(transactionToEdit.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }
    }

    // ✅ BẠN ĐANG THIẾU CÁC HÀM DƯỚI ĐÂY

    private void setupCategorySpinner() {
        String[] categories = {"Ăn uống", "Mua sắm", "Di chuyển", "Hóa đơn", "Giải trí", "Sức khỏe"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupDatePicker() {
        selectedDateCalendar = Calendar.getInstance();
        updateDateInView();
        dateTextView.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupAmountSuggestions() {
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateSuggestions(s.toString());
            }
        });
        View.OnClickListener suggestionClickListener = v -> {
            Button b = (Button) v;
            String suggestedValue = b.getText().toString().replaceAll("[.,]", "");
            amountEditText.setText(suggestedValue);
            amountEditText.setSelection(suggestedValue.length());
        };
        suggestion1.setOnClickListener(suggestionClickListener);
        suggestion2.setOnClickListener(suggestionClickListener);
        suggestion3.setOnClickListener(suggestionClickListener);
    }

    private void updateSuggestions(String text) {
        if (text.isEmpty()) {
            suggestionLayout.setVisibility(View.GONE);
            return;
        }
        try {
            long number = Long.parseLong(text);
            if (number > 0) {
                suggestion1.setText(formatNumber(number * 100));
                suggestion2.setText(formatNumber(number * 1000));
                suggestion3.setText(formatNumber(number * 10000));
                suggestionLayout.setVisibility(View.VISIBLE);
            } else {
                suggestionLayout.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            suggestionLayout.setVisibility(View.GONE);
        }
    }

    private String formatNumber(long number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };
        new DatePickerDialog(this, dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dateTextView.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    private void saveTransaction() {
        // Lấy dữ liệu từ các ô nhập (giữ nguyên)
        String amountStr = amountEditText.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = categorySpinner.getSelectedItem().toString();
        String note = noteEditText.getText().toString();

        // KIỂM TRA XEM ĐANG Ở CHẾ ĐỘ SỬA HAY THÊM MỚI
        if (transactionToEdit != null) {
            // --- ĐANG Ở CHẾ ĐỘ SỬA ---
            // Tạo một đối tượng Transaction mới với các thông tin đã được cập nhật
            Transaction editedTransaction = new Transaction(
                    category,
                    note,
                    amount,
                    transactionToEdit.isExpense(), // Giữ nguyên đây là chi tiêu hay thu nhập
                    selectedDateCalendar.getTime()
            );
            // QUAN TRỌNG: Giữ lại ID cũ để cập nhật đúng đối tượng trong database
            editedTransaction.id = transactionToEdit.id;

            // Gửi toàn bộ đối tượng đã sửa về cho TransactionDetailActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("EDITED_TRANSACTION_DATA", editedTransaction);
            setResult(Activity.RESULT_OK, resultIntent);

        } else {
            // --- ĐANG Ở CHẾ ĐỘ THÊM MỚI (Code cũ của bạn) ---
            // Gửi các thông tin riêng lẻ về cho MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("EXTRA_AMOUNT", amount);
            resultIntent.putExtra("EXTRA_CATEGORY", category);
            resultIntent.putExtra("EXTRA_NOTE", note);
            resultIntent.putExtra("EXTRA_DATE", selectedDateCalendar.getTimeInMillis());
            setResult(Activity.RESULT_OK, resultIntent);
        }

        // Đóng màn hình sau khi đã hoàn tất
        finish();
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