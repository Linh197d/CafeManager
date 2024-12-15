package com.app.shopfee.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.shopfee.Api.JavaMailSender;
import com.app.shopfee.R;
import com.app.shopfee.model.User;
import com.app.shopfee.prefs.DataStoreManager;
import com.app.shopfee.utils.GlobalFunction;
import com.app.shopfee.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends BaseActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtOtp;
    private Button btnRegister;
    private LinearLayout layoutLogin;
    private TextView layOtp, tvOtpMessage;
    private boolean isEnableButtonRegister;
    private long otpRequestTime = 0;
    private static final long OTP_REQUEST_INTERVAL = 30000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initUi();
        initListener();
    }

    private void initUi() {
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtOtp = findViewById(R.id.edt_otp);
        btnRegister = findViewById(R.id.btn_register);
        layoutLogin = findViewById(R.id.layout_login);
        layOtp = findViewById(R.id.txt_layotp);
        tvOtpMessage = findViewById(R.id.tv_otp_message);
    }

    private void initListener() {
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateRegisterButtonState();
            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateRegisterButtonState();
            }
        });

        layoutLogin.setOnClickListener(v -> finish());
        layOtp.setOnClickListener(view -> {
            long currentTime = System.currentTimeMillis();
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            } else {
                sendOtpToEmail(email);
                otpRequestTime = currentTime;
                tvOtpMessage.setVisibility(View.VISIBLE);
                layOtp.setEnabled(false);
                layOtp.setAlpha(0.4f);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    layOtp.setEnabled(true);
                    layOtp.setAlpha(1.0f);
                    tvOtpMessage.setVisibility(View.GONE);
                }, OTP_REQUEST_INTERVAL);
            }
        });


        btnRegister.setOnClickListener(v -> onClickValidateRegister());
    }

    private void updateRegisterButtonState() {
        String strEmail = edtEmail.getText().toString().trim();
        String strOtp = edtOtp.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();


        boolean enableButton = !StringUtil.isEmpty(strEmail)  && !StringUtil.isEmpty(strOtp) && !StringUtil.isEmpty(strPassword);
        isEnableButtonRegister = enableButton;
        btnRegister.setBackgroundResource(enableButton ? R.drawable.bg_button_enable_corner_16 : R.drawable.bg_button_disable_corner_16);
    }

    public static class OTPUtils {
        public static String generateOTP() {
            int randomPin = (int) (Math.random() * 900000) + 100000;
            return String.valueOf(randomPin);
        }
    }

    private void sendOtpToEmail(String email) {
        String otp = OTPUtils.generateOTP();
        String userId = email.split("@")[0];
        String subject = "Mã OTP của bạn";
        String message = "Mã OTP của bạn là: " + otp;
        JavaMailSender javaMailAPI = new JavaMailSender(email, subject, message);
        javaMailAPI.execute();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("OTPs");
        databaseReference.child(userId).setValue(otp)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "OTP đã được gửi đến email của bạn", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Lỗi lưu OTP", Toast.LENGTH_SHORT).show();
                });
    }

    private void onClickValidateRegister() {
        if (!isEnableButtonRegister) return;

        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        String inputOtp = edtOtp.getText().toString().trim();

        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(getString(R.string.msg_password_require));
        } else if (strPassword.length() < 6) {
            showToastMessage("Mật khẩu phải có ít nhất 6 ký tự");
        } else if (StringUtil.isEmpty(inputOtp)) {
            showToastMessage("Vui lòng nhập mã OTP");
        } else if (!StringUtil.isValidEmail(strEmail) || !strEmail.endsWith("@gmail.com")) {
            showToastMessage(getString(R.string.msg_email_invalid));
        } else {
            verifyOtpAndRegisterUser(strEmail, strPassword, inputOtp);
        }
    }

    private void verifyOtpAndRegisterUser(String email, String password, String inputOtp) {
        String userId = email.split("@")[0];

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("OTPs");
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String storedOtp = task.getResult().getValue(String.class);
                if (storedOtp != null && storedOtp.equals(inputOtp)) {
                    registerUserFirebase(email, password);
                    databaseReference.child(userId).removeValue();
                } else {
                    showToastMessage("Mã OTP không đúng");
                }
            } else {
                showToastMessage("Lỗi xác thực OTP");
            }
        }).addOnFailureListener(e -> {
            showToastMessage("Lỗi xác thực OTP");
        });
    }

    private void registerUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            showToastMessage("Đăng ký thành công. Vui lòng đăng nhập.");
                            GlobalFunction.startActivity(RegisterActivity.this, LoginActivity.class);
                            finishAffinity();
                        }
                    } else {
                        showToastMessage(getString(R.string.msg_register_error));
                    }
                });
    }
}
