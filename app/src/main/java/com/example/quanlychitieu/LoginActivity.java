package com.example.quanlychitieu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager; // Hoặc androidx.preference.PreferenceManager
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// --- Import cho Google Sign-In ---
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.app.Activity;
// (Import TextInputEditText nếu bạn dùng)
// import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private SharedPreferences prefs;

    // --- Biến cho Google Sign-In ---
    private SignInButton googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private static final String TAG = "LoginActivity";

    // --- Key SharedPreferences (Cho 1 tài khoản duy nhất) ---
    private static final String PREF_USERNAME = "pref_username";
    private static final String PREF_PASSWORD = "pref_password"; // !! CẢNH BÁO BẢO MẬT !!
    private static final String PREF_LOGGED_IN = "pref_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Kiểm tra đăng nhập trước khi set layout
        if (prefs.getBoolean(PREF_LOGGED_IN, false)) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login2); // Đảm bảo dùng đúng file layout

        // --- Cấu hình Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- Đăng ký ActivityResultLauncher ---
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Log.w(TAG, "Google Sign In bị hủy hoặc thất bại, resultCode: " + result.getResultCode());
                        Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });

        // --- Ánh xạ View ---
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // --- Cài đặt Listeners ---
        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Xử lý đăng nhập bằng tài khoản thường (SharedPreferences)
     */
    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- LẤY DỮ LIỆU TỪ SharedPreferences ---
        String savedUsername = prefs.getString(PREF_USERNAME, null);
        String savedPassword = prefs.getString(PREF_PASSWORD, null); // !! CẢNH BÁO BẢO MẬT !!

        // --- KIỂM TRA ĐĂNG NHẬP ---
        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            // Đăng nhập thành công
            prefs.edit().putBoolean(PREF_LOGGED_IN, true).apply();
            goToMainActivity();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Khởi chạy Intent Đăng nhập Google
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Xử lý kết quả trả về từ Google
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Đăng nhập Google thành công: " + account.getEmail());

            // Đăng nhập Google thành công, coi như đã đăng nhập vào app
            // (Bỏ qua bước kiểm tra/tạo user trong Room)
            prefs.edit().putBoolean(PREF_LOGGED_IN, true).apply();

            // (Tùy chọn) Lưu email của Google làm username để hiển thị
            // prefs.edit().putString(PREF_USERNAME, account.getEmail()).apply();

            goToMainActivity();

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Đăng nhập Google thất bại. Mã lỗi: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Chuyển đến màn hình chính
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}