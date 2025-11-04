package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SetBudgetActivity extends AppCompatActivity {

    private RecyclerView budgetRecyclerView;
    private BudgetAdapter adapter;
    private Button saveBudgetsButton;
    private List<String> expenseCategories;
    private Map<String, Double> currentBudgets;
    private BudgetDao budgetDao;
    private String currentMonthYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        budgetRecyclerView = findViewById(R.id.budgetRecyclerView);
        saveBudgetsButton = findViewById(R.id.saveBudgetsButton);
        budgetDao = AppDatabase.getDatabase(this).budgetDao();

        // Lấy danh sách hạng mục chi tiêu (tạm thời lấy cứng)
        expenseCategories = Arrays.asList("Ăn uống", "Mua sắm", "Di chuyển", "Hóa đơn", "Giải trí", "Sức khỏe", "Khác");

        // Lấy tháng năm hiện tại (VD: "2025-10")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currentMonthYear = sdf.format(Calendar.getInstance().getTime());

        loadCurrentBudgets();

        adapter = new BudgetAdapter(expenseCategories, currentBudgets);
        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgetRecyclerView.setAdapter(adapter);

        saveBudgetsButton.setOnClickListener(v -> saveBudgets());
    }

    // Đọc ngân sách đã lưu cho tháng hiện tại từ DB
    private void loadCurrentBudgets() {
        currentBudgets = new HashMap<>();
        List<Budget> budgetsFromDb = budgetDao.getBudgetsForMonth(currentMonthYear);
        for (Budget budget : budgetsFromDb) {
            currentBudgets.put(budget.getCategoryName(), budget.getBudgetAmount());
        }
    }

    // Lưu các thay đổi ngân sách vào DB
    private void saveBudgets() {
        Map<String, Double> updatedBudgets = adapter.getBudgetData();
        for (Map.Entry<String, Double> entry : updatedBudgets.entrySet()) {
            String category = entry.getKey();
            Double amount = entry.getValue();
            if (amount != null) {
                Budget budget = new Budget(category, currentMonthYear, amount);
                budgetDao.upsert(budget); // Dùng upsert để thêm mới hoặc cập nhật
            }
        }
        Toast.makeText(this, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
        finish(); // Đóng màn hình sau khi lưu
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