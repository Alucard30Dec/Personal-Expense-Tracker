package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast; // Import Toast để thông báo lỗi nếu cần

import java.text.ParseException; // Import ParseException
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections; // Import Collections
import java.util.Comparator; // Import Comparator
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TextView categoryTotalTextView;
    private List<Transaction> categoryTransactions;
    private TransactionDao transactionDao;
    private String categoryName;
    private String monthYearString; // Đổi tên biến cho rõ ràng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_transactions);

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // --- Ánh xạ View ---
        recyclerView = findViewById(R.id.categoryTransactionsRecyclerView);
        categoryTotalTextView = findViewById(R.id.categoryTotalTextView);
        transactionDao = AppDatabase.getDatabase(this).transactionDao();
        categoryTransactions = new ArrayList<>();

        // --- Nhận dữ liệu ---
        Intent intent = getIntent();
        categoryName = intent.getStringExtra("CATEGORY_NAME");
        monthYearString = intent.getStringExtra("MONTH_YEAR"); // Nhận chuỗi yyyy-MM

        // --- Kiểm tra dữ liệu đầu vào ---
        if (categoryName == null || monthYearString == null) {
            Toast.makeText(this, "Lỗi: Thiếu dữ liệu hạng mục hoặc tháng!", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity nếu thiếu dữ liệu
            return;
        }

        // Đặt tiêu đề Toolbar
        getSupportActionBar().setTitle("Giao dịch '" + categoryName + "'");

        // --- Cài đặt RecyclerView ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(categoryTransactions); // Khởi tạo adapter với list rỗng
        recyclerView.setAdapter(adapter);

        // --- Tải và hiển thị dữ liệu ---
        loadCategoryTransactions();
    }

    private void loadCategoryTransactions() {
        // Lấy tất cả giao dịch từ DB trước
        List<Transaction> allTransactions = transactionDao.getAllTransactions();
        categoryTransactions.clear(); // Xóa list cũ
        double totalSpentInCategory = 0;

        // --- Xác định ngày bắt đầu và kết thúc của tháng ---
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date monthDate = monthFormat.parse(monthYearString); // Parse chuỗi yyyy-MM
            if (monthDate == null) throw new ParseException("Không thể parse ngày", 0); // Xử lý nếu parse thất bại
            cal.setTime(monthDate);
        } catch (ParseException e) {
            Toast.makeText(this, "Lỗi định dạng tháng!", Toast.LENGTH_SHORT).show();
            cal.setTime(new Date()); // Dự phòng về tháng hiện tại
        }

        cal.set(Calendar.DAY_OF_MONTH, 1); // Ngày đầu tháng
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        cal.add(Calendar.MONTH, 1); // Sang tháng sau
        cal.add(Calendar.MILLISECOND, -1); // Lùi lại 1 mili giây -> thời điểm cuối cùng của tháng trước
        Date endOfMonth = cal.getTime(); // Ngày cuối tháng

        // --- Lọc giao dịch ---
        for (Transaction t : allTransactions) {
            // Kiểm tra hạng mục VÀ nằm trong khoảng thời gian của tháng
            if (t.getCategory().equals(categoryName) &&
                    !t.getDate().before(startOfMonth) && !t.getDate().after(endOfMonth)) {
                categoryTransactions.add(t);
                if (t.isExpense()) {
                    totalSpentInCategory += t.getAmount();
                }
            }
        }

        // --- Cập nhật UI ---
        categoryTotalTextView.setText(String.format(Locale.getDefault(),
                "Tổng chi cho %s: %,.0f đ", categoryName, totalSpentInCategory));

        // Sắp xếp theo ngày mới nhất lên đầu (tùy chọn)
        Collections.sort(categoryTransactions, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        adapter.filterList(categoryTransactions); // Cập nhật dữ liệu cho Adapter
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Xử lý nút quay lại
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}