package com.app.shopfee.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.app.shopfee.R;
import com.app.shopfee.model.User;
import com.app.shopfee.prefs.DataStoreManager;
import com.app.shopfee.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executors;

public class ChangePasswordActivity extends BaseActivity {

    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initToolbar();
        initUi();
        initListener();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.change_password));
    }

    private void initUi() {
        edtOldPassword = findViewById(R.id.edt_old_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    private void initListener() {
        edtOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtOldPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtOldPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });
        edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtNewPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtNewPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtConfirmPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtConfirmPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });

        btnChangePassword.setOnClickListener(v -> onClickValidateChangePassword());
    }

    private void onClickValidateChangePassword() {
        String strOldPassword = edtOldPassword.getText().toString().trim();
        String strNewPassword = edtNewPassword.getText().toString().trim();
        String strConfirmPassword = edtConfirmPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strOldPassword)) {
            showToastMessage(getString(R.string.msg_old_password_require));
        } else if (StringUtil.isEmpty(strNewPassword)) {
            showToastMessage(getString(R.string.msg_new_password_require));
        } else if (StringUtil.isEmpty(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_confirm_password_require));
        } else if (!DataStoreManager.getUser().getPassword().equals(strOldPassword)) {
            showToastMessage(getString(R.string.msg_old_password_invalid));
        } else if (!strNewPassword.equals(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_confirm_password_invalid));
        } else if (strOldPassword.equals(strNewPassword)) {
            showToastMessage(getString(R.string.msg_new_password_invalid));
        } else {
            changePassword(strNewPassword);
        }
    }

    private void changePassword(String newPassword) {
        if (!isPasswordStrong(newPassword)) {
            showToastMessage(getString(R.string.msg_weak_password));
            return;
        }
        authenticateBiometricOrDeviceCredentials(() -> {

            showProgressDialog(true);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                return;
            }

            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        showProgressDialog(false);
                        if (task.isSuccessful()) {
                            showToastMessage(getString(R.string.msg_change_password_successfully));
                            User userLogin = DataStoreManager.getUser();
                            userLogin.setPassword(newPassword);
                            DataStoreManager.setUser(userLogin);
                            edtOldPassword.setText("");
                            edtNewPassword.setText("");
                            edtConfirmPassword.setText("");
                        } else {
                            showToastMessage(getString(R.string.msg_change_password_failed)
                                    + ": " + task.getException().getMessage());
                        }
                    });
        });
    }

    private void authenticateBiometricOrDeviceCredentials(Runnable onSuccess) {
        BiometricManager biometricManager = BiometricManager.from(this);

        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Sinh trắc học khả dụng
                if (isBiometricEnrolled()) {
                    authenticateBiometric(onSuccess);
                } else {
                    showToastMessage("Bạn chưa cài đặt vân tay hoặc khuôn mặt. Sử dụng mã PIN hoặc mật khẩu để xác thực.");
                    authenticateDeviceCredentials(onSuccess);
                }
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Sinh trắc học chưa được cài đặt
                showToastMessage("Bạn chưa cài đặt vân tay hoặc khuôn mặt. Sử dụng mã PIN hoặc mật khẩu để xác thực.");
                authenticateDeviceCredentials(onSuccess);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            default:
                // Không có phần cứng hoặc lỗi khác, chuyển sang mã PIN/mật khẩu
                showToastMessage("Thiết bị không hỗ trợ sinh trắc học. Sử dụng mã PIN hoặc mật khẩu để xác thực.");
                authenticateDeviceCredentials(onSuccess);
                break;
        }
    }

    private boolean isBiometricEnrolled() {
        // Kiểm tra xem có phương thức sinh trắc học nào đã được cài đặt trên thiết bị
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void authenticateBiometric(Runnable onSuccess) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực để đổi mật khẩu")
                .setSubtitle("Sử dụng vân tay hoặc khuôn mặt để xác thực danh tính của bạn")
                .setNegativeButtonText("Hủy")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, Executors.newSingleThreadExecutor(),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        runOnUiThread(onSuccess); // Xác thực thành công
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        runOnUiThread(() -> showToastMessage("Xác thực thất bại: " + errString));
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        runOnUiThread(() -> showToastMessage("Không thể xác thực danh tính của bạn."));
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void authenticateDeviceCredentials(Runnable onSuccess) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực để đổi mật khẩu")
                .setSubtitle("Sử dụng mã PIN, mật khẩu hoặc khóa hình mẫu để xác thực danh tính của bạn")
                .setDeviceCredentialAllowed(true) // Cho phép sử dụng PIN/mật khẩu
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, Executors.newSingleThreadExecutor(),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        runOnUiThread(onSuccess); // Xác thực thành công
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        runOnUiThread(() -> showToastMessage("Xác thực thất bại: " + errString));
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        runOnUiThread(() -> showToastMessage("Không thể xác thực danh tính của bạn."));
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }


    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 // độ dài lớn hơn =8
                && password.matches(".*[A-Z].*")// có 1 ký tự in hoa
                && password.matches(".*[a-z].*")// có 1 ký tự in thờng
                && password.matches(".*\\d.*")// có 1 chữ số
                && password.matches(".*[@#$%^&+=!].*");// có 1 ký tự dặc biệt
    }
}