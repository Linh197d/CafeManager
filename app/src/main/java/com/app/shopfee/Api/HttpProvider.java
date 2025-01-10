package com.app.shopfee.Api;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class HttpProvider {

    // Hàm mã hóa dữ liệu
    private static String encryptData(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // Initialization Vector (IV)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Kết hợp dữ liệu mã hóa và IV, sử dụng Base64 để dễ dàng truyền đi
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }

    public static JSONObject sendPost(String URL, RequestBody formBody) {
        JSONObject data = new JSONObject();
        try {
            // Tạo khóa AES
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // Sử dụng AES 256 bit
            SecretKey secretKey = keyGenerator.generateKey();

            // Lấy dữ liệu từ formBody và mã hóa nó
            String sensitiveData = "sensitiveData"; // Thay bằng dữ liệu thực tế từ formBody
            String encryptedData = encryptData(sensitiveData, secretKey);

            // Mã hóa dữ liệu trước khi thêm vào formBody
            FormBody encryptedFormBody = new FormBody.Builder()
                    .add("encrypted_data", encryptedData)
                    .build();

            // Tạo yêu cầu với formBody đã mã hóa
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(spec))
                    .callTimeout(5000, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(encryptedFormBody)  // Gửi dữ liệu đã mã hóa
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.println(Log.ERROR, "BAD_REQUEST", response.body().string());
                data = null;
            } else {
                data = new JSONObject(response.body().string());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}