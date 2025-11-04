package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast; // Import Toast để debug (tùy chọn)

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays; // Đảm bảo có import này
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// ✅ Implement interface OnCategoryClickListener
public class BudgetActivity extends AppCompatActivity implements BudgetProgressAdapter.OnCategoryClickListener {

    private RecyclerView budgetProgressRecyclerView;
    private BudgetProgressAdapter adapter;
    private List<BudgetProgress> budgetProgressData;
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;
    private String currentMonthYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget); // Dòng này có thể gây lỗi nếu layout sai

        Toolbar toolbar = findViewById(R.id.toolbar); // Kiểm tra ID toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        budgetProgressRecyclerView = findViewById(R.id.budgetProgressRecyclerView); // Kiểm tra ID RecyclerView
        budgetDao = AppDatabase.getDatabase(this).budgetDao();
        transactionDao = AppDatabase.getDatabase(this).transactionDao();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currentMonthYear = sdf.format(Calendar.getInstance().getTime());

        budgetProgressRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter ở đây hoặc trong loadBudgetData nhưng phải đúng constructor
        budgetProgressData = new ArrayList<>();
        adapter = new BudgetProgressAdapter(this, budgetProgressData); // Đảm bảo có 'this'
        adapter.setOnCategoryClickListener(this);
        budgetProgressRecyclerView.setAdapter(adapter);

        // loadBudgetData() thường được gọi trong onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgetData(); // Tải hoặc tải lại dữ liệu khi màn hình hiển thị
    }

    private void loadBudgetData() {
        // Xóa dữ liệu cũ trước khi nạp dữ liệu mới
        budgetProgressData.clear();

        // 1. Lấy ngân sách cho tháng hiện tại
        List<Budget> budgets = budgetDao.getBudgetsForMonth(currentMonthYear);
        Map<String, Double> budgetMap = new HashMap<>();
        for (Budget budget : budgets) {
            budgetMap.put(budget.getCategoryName(), budget.getBudgetAmount());
        }

        // 2. Lấy tất cả giao dịch để tính toán chi tiêu cho tháng hiện tại
        List<Transaction> allTransactions = transactionDao.getAllTransactions();
        Map<String, Double> spentMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        try {
            // Đảm bảo tính toán dựa trên tháng chính xác
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date monthDate = monthFormat.parse(currentMonthYear);
            cal.setTime(monthDate != null ? monthDate : new Date()); // Thêm kiểm tra null
        } catch (Exception e) {
            cal.setTime(new Date()); // Dự phòng về ngày hiện tại
        }
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        Date startOfMonth = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date startOfNextMonth = cal.getTime();

        for (Transaction t : allTransactions) {
            // Lọc các khoản chi tiêu trong tháng hiện tại
            if (t.isExpense() && t.getDate() != null && !t.getDate().before(startOfMonth) && t.getDate().before(startOfNextMonth)) { // Thêm kiểm tra null cho date
                String category = t.getCategory();
                spentMap.put(category, spentMap.getOrDefault(category, 0.0) + t.getAmount());
            }
        }

        // 3. Tạo dữ liệu hiển thị
        // Sử dụng danh sách hạng mục chi tiêu đã định nghĩa
        List<String> expenseCategories = Arrays.asList("Ăn uống", "Mua sắm", "Di chuyển", "Hóa đơn", "Giải trí", "Sức khỏe","Khác");
        for (String category : expenseCategories) {
            double budgetAmount = budgetMap.getOrDefault(category, 0.0);
            double spentAmount = spentMap.getOrDefault(category, 0.0);
            // Chỉ thêm những hạng mục có đặt ngân sách HOẶC có chi tiêu
            if (budgetAmount > 0 || spentAmount > 0) {
                budgetProgressData.add(new BudgetProgress(category, budgetAmount, spentAmount));
            }
        }

        // --- SỬA DÒNG NÀY ---
        // Kiểm tra xem adapter đã được khởi tạo chưa (tránh lỗi khi hàm được gọi lần đầu từ onCreate)
        if (adapter == null) {
            adapter = new BudgetProgressAdapter(this, budgetProgressData); // Khởi tạo nếu chưa có
            adapter.setOnCategoryClickListener(this); // Set listener
            budgetProgressRecyclerView.setAdapter(adapter); // Set adapter cho RecyclerView
        } else {
            adapter.notifyDataSetChanged(); // Chỉ cần cập nhật dữ liệu nếu adapter đã có
        }
    }

    // ✅ Implement phương thức từ interface click listener
    @Override
    public void onCategoryClick(int position) {
        // Thêm kiểm tra để tránh lỗi index ngoài giới hạn
        if (budgetProgressData != null && position >= 0 && position < budgetProgressData.size()) {
            BudgetProgress clickedItem = budgetProgressData.get(position);
            Intent intent = new Intent(this, CategoryTransactionsActivity.class);
            intent.putExtra("CATEGORY_NAME", clickedItem.getCategoryName());
            intent.putExtra("MONTH_YEAR", currentMonthYear); // Truyền chuỗi tháng/năm
            startActivity(intent);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu mục.", Toast.LENGTH_SHORT).show(); // Thông báo lỗi tùy chọn
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.budget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) { // Xử lý sự kiện nhấn nút Up
            finish();
            return true;
        } else if (itemId == R.id.action_set_budget) {
            Intent intent = new Intent(this, SetBudgetActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}