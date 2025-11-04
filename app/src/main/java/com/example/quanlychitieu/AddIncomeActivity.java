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
import android.Manifest; // Cho quyền
import android.content.ActivityNotFoundException; // Cho lỗi Speech-to-Text
import android.content.pm.PackageManager; // Cho kiểm tra quyền
import android.speech.RecognizerIntent; // Cho Speech-to-Text Intent
import android.widget.ImageButton; // Cho nút microphone
import androidx.annotation.NonNull; // Cho annotation @NonNull
import androidx.annotation.Nullable; // Cho annotation @Nullable (trong onActivityResult)
import androidx.core.app.ActivityCompat; // Cho yêu cầu quyền
import androidx.core.content.ContextCompat; // Cho kiểm tra quyền
import java.util.ArrayList; // Cho kết quả Speech-to-Text
import android.util.Log;

public class AddIncomeActivity extends AppCompatActivity {

    // --- Biến giao diện ---
    private EditText amountEditText, noteEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private TextView dateTextView;
    private LinearLayout suggestionLayout;
    private Button suggestion1, suggestion2, suggestion3;
    private ImageButton micButton;
    // ✅ Thêm hằng số
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int SPEECH_REQUEST_CODE = 10;
    private static final int EDIT_REQUEST_CODE = 101;

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
        micButton = findViewById(R.id.micButton); // ✅ Ánh xạ nút mic

        // --- Cài đặt chức năng ---
        setupDatePicker();
        setupCategorySpinner();
        setupAmountSuggestions();
        setupMicButton(); // ✅ Gọi hàm cài đặt mic

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
    // --- CÁC HÀM MỚI CHO CHỨC NĂNG GIỌNG NÓI ---
    private void setupMicButton() {
        micButton.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechToText();
            } else {
                requestAudioPermission();
            }
        });
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                Toast.makeText(this, "Cần cấp quyền ghi âm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói ghi chú...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ", Toast.LENGTH_SHORT).show();
        }
    }

    // --- CẬP NHẬT onActivityResult ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Gọi super trước
        Log.d("SpeechToText", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode); // Log khi hàm được gọi

        if (requestCode == SPEECH_REQUEST_CODE) { // Kiểm tra đúng requestCode
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d("SpeechToText", "Result OK, data received: " + (result != null ? result.toString() : "null")); // Log kết quả
                if (result != null && !result.isEmpty()) {
                    noteEditText.setText(result.get(0));
                    noteEditText.setSelection(noteEditText.getText().length());
                } else {
                    Log.w("SpeechToText", "Result list is null or empty"); // Log nếu không có kết quả text
                }
            } else {
                // Log các trường hợp lỗi khác từ Google Speech Recognizer
                Log.e("SpeechToText", "Speech recognition failed or cancelled. ResultCode: " + resultCode);
                // Có thể thêm Toast thông báo lỗi ở đây nếu muốn
                // Toast.makeText(this, "Không nhận dạng được giọng nói (Mã lỗi: " + resultCode + ")", Toast.LENGTH_SHORT).show();
            }
        }
        // Xử lý kết quả sửa nếu màn hình này có sửa
        else if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Transaction editedTransaction = (Transaction) data.getSerializableExtra("EDITED_TRANSACTION_DATA");
            if (editedTransaction != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("EDITED_TRANSACTION_DATA", editedTransaction);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}