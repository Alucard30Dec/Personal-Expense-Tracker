package com.example.quanlychitieu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.CheckBox; // Import CheckBox
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects; // Import Objects

public class AddEditDebtActivity extends AppCompatActivity {

    // --- Biến giao diện ---
    private EditText debtNameEditText, debtAmountEditText;
    private RadioGroup debtTypeRadioGroup;
    private RadioButton radioBorrow, radioLend;
    private TextView dueDateTextView;
    private CheckBox paidCheckBox; // CheckBox trạng thái
    private Button saveDebtButton;

    // --- Biến dữ liệu & logic ---
    private DebtDao debtDao;
    private Calendar selectedDueDateCalendar;
    private Debt debtToEdit = null; // Lưu khoản nợ/vay cần sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_debt); // Đảm bảo layout này có CheckBox

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // --- Ánh xạ View ---
        debtNameEditText = findViewById(R.id.debtNameEditText);
        debtAmountEditText = findViewById(R.id.debtAmountEditText);
        debtTypeRadioGroup = findViewById(R.id.debtTypeRadioGroup);
        radioBorrow = findViewById(R.id.radioBorrow);
        radioLend = findViewById(R.id.radioLend);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        paidCheckBox = findViewById(R.id.paidCheckBox); // Ánh xạ CheckBox
        saveDebtButton = findViewById(R.id.saveDebtButton);

        // --- Khởi tạo Database DAO ---
        debtDao = AppDatabase.getDatabase(this).debtDao();
        selectedDueDateCalendar = null;

        // --- Kiểm tra xem có dữ liệu sửa được gửi sang không ---
        if (getIntent().hasExtra("EDIT_DEBT_DATA")) {
            debtToEdit = (Debt) getIntent().getSerializableExtra("EDIT_DEBT_DATA");
            getSupportActionBar().setTitle("Sửa Vay/Nợ");
            saveDebtButton.setText("Cập nhật");
            paidCheckBox.setVisibility(View.VISIBLE); // Hiển thị CheckBox khi sửa
            prefillEditData();
        } else {
            getSupportActionBar().setTitle("Thêm Vay/Nợ");
            paidCheckBox.setVisibility(View.GONE); // Ẩn CheckBox khi thêm mới
        }

        // --- Cài đặt Listeners ---
        dueDateTextView.setOnClickListener(v -> showDatePickerDialog());
        saveDebtButton.setOnClickListener(v -> saveDebt());
    }

    private void prefillEditData() {
        if (debtToEdit == null) return;
        debtNameEditText.setText(debtToEdit.getName());
        debtAmountEditText.setText(String.valueOf((long)debtToEdit.getAmount()));
        if (debtToEdit.isLending()) {
            radioLend.setChecked(true);
        } else {
            radioBorrow.setChecked(true);
        }
        if (debtToEdit.getDueDate() != null) {
            selectedDueDateCalendar = Calendar.getInstance();
            selectedDueDateCalendar.setTime(debtToEdit.getDueDate());
            updateDueDateInView();
        }
        paidCheckBox.setChecked(debtToEdit.isPaid());
        paidCheckBox.setText(debtToEdit.isLending() ? "Đã thu" : "Đã trả");
    }

    private void saveDebt() {
        String name = debtNameEditText.getText().toString().trim();
        String amountStr = debtAmountEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Vui lòng nhập Tên và Số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isLending = radioLend.isChecked();
        Date dueDate = (selectedDueDateCalendar != null) ? selectedDueDateCalendar.getTime() : null;
        boolean isPaid = paidCheckBox.isChecked(); // Lấy trạng thái từ CheckBox

        if (debtToEdit != null) { // --- Chế độ Sửa ---
            Debt updatedDebt = new Debt(name, amount, isLending, dueDate, isPaid);
            updatedDebt.id = debtToEdit.id;
            debtDao.update(updatedDebt);
            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        } else { // --- Chế độ Thêm mới ---
            Debt newDebt = new Debt(name, amount, isLending, dueDate, false);
            debtDao.insert(newDebt);
            Toast.makeText(this, "Đã thêm mới", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK); // Đặt kết quả trả về là OK
        finish(); // Đóng Activity
    }

    private void showDatePickerDialog() {
        Calendar initialCalendar = Calendar.getInstance();
        if (selectedDueDateCalendar != null) {
            initialCalendar = selectedDueDateCalendar;
        }
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            if (selectedDueDateCalendar == null) {
                selectedDueDateCalendar = Calendar.getInstance();
            }
            selectedDueDateCalendar.set(Calendar.YEAR, year);
            selectedDueDateCalendar.set(Calendar.MONTH, month);
            selectedDueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDueDateInView();
        };
        new DatePickerDialog(this, dateSetListener,
                initialCalendar.get(Calendar.YEAR),
                initialCalendar.get(Calendar.MONTH),
                initialCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDueDateInView() {
        if (selectedDueDateCalendar != null) {
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            dueDateTextView.setText(sdf.format(selectedDueDateCalendar.getTime()));
        } else {
            dueDateTextView.setText("Chọn ngày đáo hạn (Tùy chọn)");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}