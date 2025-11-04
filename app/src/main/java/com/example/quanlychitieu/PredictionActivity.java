package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PredictionActivity extends AppCompatActivity {

    private RecyclerView predictionRecyclerView;
    private PredictionAdapter adapter;
    private List<CategoryPrediction> predictionData;
    private TransactionDao transactionDao;
    private final int MONTHS_TO_AVERAGE = 3; // Số tháng lấy trung bình

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        predictionRecyclerView = findViewById(R.id.predictionRecyclerView);
        transactionDao = AppDatabase.getDatabase(this).transactionDao();
        predictionData = new ArrayList<>();

        calculatePredictions();

        adapter = new PredictionAdapter(predictionData);
        predictionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        predictionRecyclerView.setAdapter(adapter);
    }

    private void calculatePredictions() {
        predictionData.clear();
        Calendar cal = Calendar.getInstance();

        // 1. Xác định khoảng thời gian (MONTHS_TO_AVERAGE tháng gần nhất)
        cal.add(Calendar.MONTH, -MONTHS_TO_AVERAGE); // Lùi lại N tháng
        cal.set(Calendar.DAY_OF_MONTH, 1); // Ngày đầu tiên của tháng bắt đầu
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();

        cal = Calendar.getInstance(); // Reset về ngày hiện tại
        cal.set(Calendar.DAY_OF_MONTH, 1); // Ngày đầu tiên của tháng hiện tại
        cal.add(Calendar.MILLISECOND, -1); // Lùi lại 1ms -> Thời điểm cuối cùng của tháng trước
        Date endDate = cal.getTime();

        // 2. Lấy giao dịch chi tiêu trong khoảng thời gian đó
        List<Transaction> pastExpenses = transactionDao.getExpensesBetweenDates(startDate, endDate);

        // 3. Tính tổng chi tiêu cho mỗi hạng mục
        Map<String, Double> categoryTotals = new HashMap<>();
        if (pastExpenses != null) {
            for (Transaction t : pastExpenses) {
                categoryTotals.put(t.getCategory(),
                        categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }

        // 4. Tính trung bình và tạo dữ liệu dự đoán
        // Lấy danh sách hạng mục chi tiêu (có thể lấy từ nguồn khác)
        List<String> expenseCategories = Arrays.asList("Ăn uống", "Mua sắm", "Di chuyển", "Hóa đơn", "Giải trí", "Sức khỏe", "Khác");
        for (String category : expenseCategories) {
            double total = categoryTotals.getOrDefault(category, 0.0);
            if (total > 0) { // Chỉ dự đoán cho hạng mục đã từng chi
                double average = total / MONTHS_TO_AVERAGE;
                predictionData.add(new CategoryPrediction(category, average));
            }
        }

        // Sắp xếp theo số tiền dự đoán giảm dần (tùy chọn)
        predictionData.sort((p1, p2) -> Double.compare(p2.getPredictedAmount(), p1.getPredictedAmount()));
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