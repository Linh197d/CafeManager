package com.app.shopfee.utils;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.app.shopfee.constant.AboutUsConfig;
import com.app.shopfee.listener.IGetTimeListener;
import com.app.shopfee.prefs.DataStoreManager;

import java.util.Calendar;

public class GlobalFunction {

    public static void startActivity(Context context, Class<?> clz) {
        Intent intent = new Intent(context, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(context, clz);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static int encodeEmailUser() {
        int hashCode = DataStoreManager.getUser().getEmail().hashCode();
        if (hashCode < 0) {
            hashCode = hashCode * -1;
        }
        return hashCode;
    }

    public static void showTimePicker(Context context, final IGetTimeListener getDateListener) {
        Calendar mCalendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener callBack = (view, hourOfDay, minute) -> {
            String time;
            time = StringUtil.getDoubleNumber(hourOfDay) + ":" +
                    StringUtil.getDoubleNumber(minute);
            getDateListener.getTime(DateTimeUtils.parseTimeFormat(time));
        };
        TimePickerDialog timePicker = new TimePickerDialog(context,
                callBack, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),
                true);
        timePicker.show();
    }

    public static void onClickOpenGmail(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", AboutUsConfig.GMAIL, null));
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }

    public static void onClickOpenFacebook(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String urlFacebook = AboutUsConfig.LINK_FACEBOOK;

        try {
            PackageManager packageManager = context.getPackageManager();
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { // newer versions of fb app support custom URLs
                urlFacebook = "fb://facewebmodal/f?href=" + AboutUsConfig.LINK_FACEBOOK;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        intent.setData(Uri.parse(urlFacebook));
        context.startActivity(intent);
    }

    public static void callPhoneNumber(Activity activity) {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 101);
                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + AboutUsConfig.PHONE_NUMBER));
            activity.startActivity(callIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.
                    getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }
}
