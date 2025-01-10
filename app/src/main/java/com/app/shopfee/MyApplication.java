package com.app.shopfee;

import android.app.Application;
import android.content.Context;

import com.app.shopfee.prefs.DataStoreManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class MyApplication extends Application {

    private static final String FIREBASE_URL = "https://quanlycafe-6ea45-default-rtdb.firebaseio.com";
    private FirebaseDatabase mFirebaseDatabase;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        DataStoreManager.init(getApplicationContext());
        ZaloPaySDK.init(2553, Environment.SANDBOX);

    }


    public DatabaseReference getVoucherDatabaseReference() {
        return mFirebaseDatabase.getReference("voucher");
    }

    public DatabaseReference getAddressDatabaseReference() {
        return mFirebaseDatabase.getReference("address");
    }

    public DatabaseReference getPaymentMethodDatabaseReference() {
        return mFirebaseDatabase.getReference("payment");
    }

    public DatabaseReference getCategoryDatabaseReference() {
        return mFirebaseDatabase.getReference("category");
    }

    public DatabaseReference getDrinkDatabaseReference() {
        return mFirebaseDatabase.getReference("drink");
    }

    public DatabaseReference getDrinkDetailDatabaseReference(int drinkId) {
        return mFirebaseDatabase.getReference("drink/" + drinkId);
    }

    public DatabaseReference getToppingDatabaseReference() {
        return mFirebaseDatabase.getReference("topping");
    }

    public DatabaseReference getFeedbackDatabaseReference() {
        return mFirebaseDatabase.getReference("/feedback");
    }

    public DatabaseReference getOrderDatabaseReference() {
        return mFirebaseDatabase.getReference("order");
    }

    public DatabaseReference getRatingDrinkDatabaseReference(String drinkId) {
        return mFirebaseDatabase.getReference("/drink/" + drinkId + "/rating");
    }

    public DatabaseReference getOrderDetailDatabaseReference(long orderId) {
        return mFirebaseDatabase.getReference("order/" + orderId);
    }
}
