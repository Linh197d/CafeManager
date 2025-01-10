package com.app.shopfee.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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

    }

    private void goToActivity() {
        DataStoreManager dataStoreManager = DataStoreManager.getInstance();
        Log.d("TAG", "goToActivity1: " + dataStoreManager.isSessionValid());
        if (!dataStoreManager.isSessionValid()) {
            dataStoreManager.clearUser();
            goToLoginActivity();
        }else {
            Log.d("TAG", "goToActivity2: " + StringUtil.isEmpty(DataStoreManager.getUser().getEmail()));
            Log.d("TAG", "goToActivity: " + DataStoreManager.getUser().getPassword().toString());
            if (DataStoreManager.getUser() != null
                    && !StringUtil.isEmpty(DataStoreManager.getUser().getEmail())
                    && dataStoreManager.isSessionValid()) {
                GlobalFunction.startActivity(this, MainActivity.class);
            } else {
                GlobalFunction.startActivity(this, LoginActivity.class);
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

    }
}
