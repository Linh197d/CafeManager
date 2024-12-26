package com.app.shopfee.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import com.app.shopfee.R;
import com.app.shopfee.prefs.DataStoreManager;
import com.app.shopfee.utils.GlobalFunction;
import com.app.shopfee.utils.StringUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    private final Handler handler = new Handler();
    private final Runnable runnable = this::goToActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(runnable, 2000);
    }

    private void checkLoginSession() {
        DataStoreManager dataStoreManager = DataStoreManager.getInstance();
        if (!dataStoreManager.isSessionValid()) {
            dataStoreManager.clearUser();
            goToLoginActivity();
        }
    }

    private void goToActivity() {
        checkLoginSession();
        DataStoreManager dataStoreManager = DataStoreManager.getInstance();
        if (DataStoreManager.getUser() != null
                && !StringUtil.isEmpty(DataStoreManager.getUser().getEmail())
                && dataStoreManager.isSessionValid()) {
            GlobalFunction.startActivity(this, MainActivity.class);
        } else {
            GlobalFunction.startActivity(this, LoginActivity.class);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

    }
}
