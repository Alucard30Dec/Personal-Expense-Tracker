package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private SharedPreferences prefs;

    // Key SharedPreferences (giống LoginActivity)
    private static final String PREF_USERNAME = "pref_username";
    private static final String PREF_PASSWORD = "pref_password";
    private static final String PREF_LOGGED_IN = "pref_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2); // Đảm bảo dùng đúng file layout

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        registerButton.setOnClickListener(v -> attemptRegister());
        loginTextView.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem đã có tài khoản nào (1 tài khoản) được lưu chưa
        if (prefs.contains(PREF_USERNAME)) {
            Toast.makeText(this, "Ứng dụng hiện chỉ hỗ trợ một tài khoản", Toast.LENGTH_LONG).show();
            return;
        }

        // Lưu thông tin đăng ký vào SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_USERNAME, username);
        editor.putString(PREF_PASSWORD, password); // !! CẢNH BÁO BẢO MẬT !!
        editor.putBoolean(PREF_LOGGED_IN, true);   // Tự động đăng nhập
        editor.apply();

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển sang màn hình chính
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}