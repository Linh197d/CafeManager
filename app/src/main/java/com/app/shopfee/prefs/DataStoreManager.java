package com.app.shopfee.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.app.shopfee.model.User;
import com.app.shopfee.utils.StringUtil;
import com.google.gson.Gson;

public class DataStoreManager {

    public static final String PREF_USER_INFOR = "PREF_USER_INFOR";
    private static final String PREF_NAME = "AppPreferences";
    private static final String PREF_USER_INFO = "PREF_USER_INFO";
    private static final String PREF_LOGIN_TIMESTAMP = "PREF_LOGIN_TIMESTAMP";
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;
    private static DataStoreManager instance;
    private SharedPreferences sharedPreferences;

    // Khởi tạo DataStoreManager và EncryptedSharedPreferences
    public static void init(Context context) {
        instance = new DataStoreManager();

        // Tạo key mã hóa bằng MasterKey (mã hóa dựa trên AES-GCM)
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            instance.sharedPreferences = EncryptedSharedPreferences.create(
                    "secure_prefs", // Tên tệp lưu trữ
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Mã hóa khóa bằng AES256 SIV

                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Mã hóa giá trị bằng AES256 GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Error initializing EncryptedSharedPreferences", e);
        }
    }

    public static DataStoreManager getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }

    // Lấy thông tin người dùng (với giải mã tự động)
    public static User getUser() {
        String jsonUser = DataStoreManager.getInstance()
                .sharedPreferences.getString(PREF_USER_INFOR, "");

        if (!StringUtil.isEmpty(jsonUser)) {
            return new Gson().fromJson(jsonUser, User.class);
        }

        return new User(); // Trả về đối tượng User mặc định nếu không có dữ liệu
    }

    // Lưu thông tin người dùng (với mã hóa)
    public static void setUser(@Nullable User user) {
        String jsonUser = "";
        if (user != null) {
            jsonUser = user.toJSon();
        }

        // Lưu thông tin người dùng đã mã hóa
        DataStoreManager.getInstance().sharedPreferences
                .edit()
                .putString(PREF_USER_INFOR, jsonUser)
                .apply();
    }

    public long getLastLoginTimestamp() {
        return sharedPreferences.getLong(PREF_LOGIN_TIMESTAMP, 0);
    }
    public void setLastLoginTimestamp(long timestamp) {
        sharedPreferences.edit()
                .putLong(PREF_LOGIN_TIMESTAMP, timestamp) // Lưu timestamp vào SharedPreferences
                .apply(); // Áp dụng thay đổi không đồng bộ
    }
    public void clearUser() {
        sharedPreferences.edit()
                .remove(PREF_USER_INFO)
                .remove(PREF_LOGIN_TIMESTAMP)
                .apply();
    }

    public boolean isSessionValid() {
        long lastLogin = getLastLoginTimestamp();
        return lastLogin > 0 && (System.currentTimeMillis() - lastLogin < ONE_DAY_MILLIS);
    }
}