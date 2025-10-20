package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter; // Import chung
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import java.util.LinkedHashMap;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
public class StatisticsActivity extends AppCompatActivity {

    private List<Transaction> fullTransactionList; // Danh sách đầy đủ
    private TextView totalIncomeTextView, totalExpenseTextView;
    private PieChart pieChart;
    private RadioGroup statsFilterRadioGroup;
    private BarChart barChart;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        statsFilterRadioGroup = findViewById(R.id.statsFilterRadioGroup);

        // Nhận danh sách giao dịch đầy đủ từ MainActivity
        fullTransactionList = (ArrayList<Transaction>) getIntent().getSerializableExtra("TRANSACTION_LIST");

        // Lọc và hiển thị dữ liệu ban đầu (Tất cả)
        filterAndDisplayData("all");

        // Lắng nghe sự kiện lọc
        statsFilterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.stats_radio_today) {
                filterAndDisplayData("today");
            } else if (checkedId == R.id.stats_radio_week) {
                filterAndDisplayData("week");
            } else if (checkedId == R.id.stats_radio_month) {
                filterAndDisplayData("month");
            } else if (checkedId == R.id.stats_radio_all) {
                filterAndDisplayData("all");
            }
        });
    }

    private void filterAndDisplayData(String period) {
        if (fullTransactionList == null) return;
        List<Transaction> filteredList = filterTransactionsByPeriod(period); // Tách logic lọc ra hàm riêng

        // Cập nhật giao diện với danh sách đã lọc
        calculateTotals(filteredList);
        setupPieChart(filteredList);
        // ✅ GỌI HÀM THIẾT LẬP BIỂU ĐỒ MỚI
        setupBarChart(); // Bar chart luôn hiển thị 6 tháng gần nhất, không theo filter
        setupLineChart(); // Line chart cũng vậy
    }

    // Sửa lại các hàm để nhận vào danh sách đã được lọc
    private void calculateTotals(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }
        totalIncomeTextView.setText(String.format("Thu: %,.0f đ", totalIncome));
        totalExpenseTextView.setText(String.format("Chi: %,.0f đ", totalExpense));
    }

    private void setupPieChart(List<Transaction> transactions) {
        Map<String, Float> expenseByCategory = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                float amount = (float) transaction.getAmount();
                expenseByCategory.put(category, expenseByCategory.getOrDefault(category, 0f) + amount);
            }
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        if (expenseByCategory.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        for (Map.Entry<String, Float> entry : expenseByCategory.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");

        // ✅ BƯỚC 1: TẠO DANH SÁCH MÀU TÙY CHỈNH
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(52, 152, 219));  // Xanh dương
        colors.add(Color.rgb(231, 76, 60));   // Đỏ
        colors.add(Color.rgb(46, 204, 113));  // Xanh lá
        colors.add(Color.rgb(241, 196, 15));  // Vàng
        colors.add(Color.rgb(155, 89, 182));  // Tím
        colors.add(Color.rgb(230, 126, 34));  // Cam
        colors.add(Color.rgb(26, 188, 156));  // Xanh ngọc
        colors.add(Color.rgb(52, 73, 94));    // Xám đậm

        // Bạn có thể thêm nhiều màu hơn nữa vào đây

        // ✅ BƯỚC 2: SỬ DỤNG DANH SÁCH MÀU MỚI
        dataSet.setColors(colors);

        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Chi tiêu");
        pieChart.setDrawEntryLabels(false);

        // Phần cài đặt Chú thích (Legend) giữ nguyên
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(14f);

        pieChart.invalidate();
        pieChart.animateY(1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // ✅ HÀM MỚI ĐỂ LỌC THEO THỜI GIAN
    private List<Transaction> filterTransactionsByPeriod(String period) {
        List<Transaction> filteredList = new ArrayList<>();
        if (fullTransactionList == null) return filteredList;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date startOfToday = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date startOfWeek = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();

        for (Transaction transaction : fullTransactionList) {
            switch (period) {
                case "today":
                    if (!transaction.getDate().before(startOfToday)) filteredList.add(transaction);
                    break;
                case "week":
                    if (!transaction.getDate().before(startOfWeek)) filteredList.add(transaction);
                    break;
                case "month":
                    if (!transaction.getDate().before(startOfMonth)) filteredList.add(transaction);
                    break;
                case "all":
                default:
                    filteredList.add(transaction);
                    break;
            }
        }
        return filteredList;
    }
    // ✅ HÀM MỚI CHO BIỂU ĐỒ CỘT
    private void setupBarChart() {
        if (fullTransactionList == null) return;

        // Dùng LinkedHashMap để giữ thứ tự tháng
        Map<String, float[]> monthlyData = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/yy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Chuẩn bị map cho 6 tháng gần nhất
        for (int i = 0; i < 6; i++) {
            String monthKey = monthFormat.format(cal.getTime());
            monthlyData.put(monthKey, new float[]{0f, 0f}); // [0]=Income, [1]=Expense
            cal.add(Calendar.MONTH, -1);
        }

        // Tính toán tổng thu/chi cho từng tháng
        for (Transaction t : fullTransactionList) {
            String transactionMonth = monthFormat.format(t.getDate());
            if (monthlyData.containsKey(transactionMonth)) {
                float[] totals = monthlyData.get(transactionMonth);
                if (!t.isExpense()) { // Thu nhập
                    totals[0] += (float) t.getAmount();
                } else { // Chi tiêu
                    totals[1] += (float) t.getAmount();
                }
            }
        }

        // Tạo dữ liệu cho biểu đồ
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        // Lấy dữ liệu theo thứ tự ngược lại (từ cũ đến mới) để hiển thị đúng trên biểu đồ
        List<String> sortedMonths = new ArrayList<>(monthlyData.keySet());
        Collections.reverse(sortedMonths);

        for (String monthKey : sortedMonths) {
            float[] totals = monthlyData.get(monthKey);
            incomeEntries.add(new BarEntry(index, totals[0]));
            expenseEntries.add(new BarEntry(index, totals[1]));
            try {
                // Định dạng lại label tháng cho đẹp hơn (MM/yy)
                Date date = monthFormat.parse(monthKey);
                labels.add(displayFormat.format(date));
            } catch (Exception e) {
                labels.add(monthKey); // Fallback
            }
            index++;
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeDataSet.setColor(Color.parseColor("#4CAF50")); // Màu xanh lá
        incomeDataSet.setValueTextSize(10f);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseDataSet.setColor(Color.parseColor("#F44336")); // Màu đỏ
        expenseDataSet.setValueTextSize(10f);

        // Gom nhóm 2 cột Thu và Chi cho mỗi tháng
        float groupSpace = 0.1f;
        float barSpace = 0.05f; // Khoảng cách giữa 2 cột trong 1 nhóm
        float barWidth = 0.4f; // Chiều rộng mỗi cột
        // (barWidth + barSpace) * 2 + groupSpace = 1.0 (tổng chiều rộng cho 1 nhóm tháng)

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barData.setBarWidth(barWidth);

        barChart.setData(barData);
        barChart.groupBars(0f, groupSpace, barSpace); // Bắt đầu từ vị trí 0

        // Tùy chỉnh trục X (Tháng)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true); // Căn giữa label tháng
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.size()); // Đảm bảo đủ không gian cho các label

        // Tùy chỉnh khác
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setDrawGridLines(false); // Ẩn đường kẻ dọc
        barChart.getAxisLeft().setDrawGridLines(false); // Ẩn đường kẻ ngang trục trái
        barChart.getAxisRight().setEnabled(false); // Ẩn trục phải
        barChart.invalidate(); // Vẽ lại biểu đồ
        barChart.animateY(1000);
    }
    // ✅ HÀM MỚI CHO BIỂU ĐỒ ĐƯỜNG
    private void setupLineChart() {
        if (fullTransactionList == null) return;

        // Tính toán Thu nhập ròng (Thu - Chi) cho 6 tháng gần nhất
        Map<String, Float> monthlyNetIncome = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/yy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Chuẩn bị map cho 6 tháng
        for (int i = 0; i < 6; i++) {
            String monthKey = monthFormat.format(cal.getTime());
            monthlyNetIncome.put(monthKey, 0f); // Bắt đầu với net income = 0
            cal.add(Calendar.MONTH, -1);
        }

        // Tính toán
        for (Transaction t : fullTransactionList) {
            String transactionMonth = monthFormat.format(t.getDate());
            if (monthlyNetIncome.containsKey(transactionMonth)) {
                float currentNet = monthlyNetIncome.get(transactionMonth);
                if (!t.isExpense()) { // Thu nhập
                    currentNet += (float) t.getAmount();
                } else { // Chi tiêu
                    currentNet -= (float) t.getAmount();
                }
                monthlyNetIncome.put(transactionMonth, currentNet);
            }
        }

        // Tạo dữ liệu cho biểu đồ
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        // Sắp xếp lại tháng từ cũ đến mới
        List<String> sortedMonths = new ArrayList<>(monthlyNetIncome.keySet());
        Collections.reverse(sortedMonths);

        for (String monthKey : sortedMonths) {
            entries.add(new Entry(index, monthlyNetIncome.get(monthKey)));
            try {
                Date date = monthFormat.parse(monthKey);
                labels.add(displayFormat.format(date));
            } catch (Exception e) {
                labels.add(monthKey);
            }
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Thu nhập ròng");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true); // Tô màu vùng dưới đường line
        dataSet.setFillColor(Color.BLUE);
        dataSet.setFillAlpha(50); // Độ mờ của màu tô

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Tùy chỉnh trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(labels.size()); // Hiển thị tất cả label

        // Tùy chỉnh khác
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
        lineChart.animateX(1000);
    }
}