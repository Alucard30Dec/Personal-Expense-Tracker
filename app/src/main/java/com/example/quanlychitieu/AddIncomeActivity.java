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

public class AddIncomeActivity extends AppCompatActivity {

    // --- Biến giao diện ---
    private EditText amountEditText, noteEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private TextView dateTextView;
    private LinearLayout suggestionLayout;
    private Button suggestion1, suggestion2, suggestion3;

    // --- Biến dữ liệu ---
    private Calendar selectedDateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm Thu nhập");
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

        // --- Cài đặt chức năng ---
        setupDatePicker();
        setupCategorySpinner();
        setupAmountSuggestions();

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    /**
     * Cài đặt chức năng chọn ngày tháng.
     */
    private void setupDatePicker() {
        selectedDateCalendar = Calendar.getInstance();
        updateDateInView();
        dateTextView.setOnClickListener(v -> showDatePickerDialog());
    }

    /**
     * Cài đặt danh sách các loại thu nhập cho Spinner.
     */
    private void setupCategorySpinner() {
        String[] categories = {"Lương", "Thưởng", "Bán hàng", "Quà tặng", "Đầu tư", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * Cài đặt chức năng gợi ý số tiền.
     */
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

    /**
     * Hiển thị các gợi ý số tiền dựa trên input của người dùng.
     */
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

    /**
     * Định dạng số thành chuỗi có dấu phẩy/chấm.
     */
    private String formatNumber(long number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    /**
     * Hiển thị hộp thoại lịch để chọn ngày.
     */
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

    /**
     * Cập nhật ngày tháng đã chọn lên TextView.
     */
    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dateTextView.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    /**
     * Kiểm tra dữ liệu, lưu và gửi kết quả về MainActivity.
     */
    private void saveTransaction() {
        String amountStr = amountEditText.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = categorySpinner.getSelectedItem().toString();
        String note = noteEditText.getText().toString();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("EXTRA_AMOUNT", amount);
        resultIntent.putExtra("EXTRA_CATEGORY", category);
        resultIntent.putExtra("EXTRA_NOTE", note);
        resultIntent.putExtra("EXTRA_DATE", selectedDateCalendar.getTimeInMillis());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // Xử lý sự kiện nhấn nút quay lại trên Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}