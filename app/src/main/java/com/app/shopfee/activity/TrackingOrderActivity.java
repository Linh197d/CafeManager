package com.app.shopfee.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.MyApplication;
import com.app.shopfee.R;
import com.app.shopfee.adapter.DrinkOrderAdapter;
import com.app.shopfee.model.Order;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class TrackingOrderActivity extends BaseActivity {

    private RecyclerView rcvDrinks;
    private LinearLayout layoutReceiptOrder;
    private View dividerStep1, dividerStep2;
    private ImageView imgStep1, imgStep2, imgStep3;
    private TextView tvTakeOrder, tvTakeOrderMessage, tvHuy;

    private long orderId;
    private Order mOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        getDataIntent();
        initToolbar();
        initUi();
        initListener();
        getOrderDetailFromFirebase();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        orderId = bundle.getLong(Constant.ORDER_ID);
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.label_tracking_order));
    }

    private void initUi() {
        rcvDrinks = findViewById(R.id.rcv_drinks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvDrinks.setLayoutManager(linearLayoutManager);
        layoutReceiptOrder = findViewById(R.id.layout_receipt_order);
        dividerStep1 = findViewById(R.id.divider_step_1);
        dividerStep2 = findViewById(R.id.divider_step_2);
        imgStep1 = findViewById(R.id.img_step_1);
        imgStep2 = findViewById(R.id.img_step_2);
        imgStep3 = findViewById(R.id.img_step_3);
        tvTakeOrder = findViewById(R.id.tv_take_order);
        tvTakeOrderMessage = findViewById(R.id.tv_take_order_message);
        tvHuy = findViewById(R.id.tv_huy);
    }

    private void initListener() {
        layoutReceiptOrder.setOnClickListener(view -> {
            if (mOrder == null) return;
            Bundle bundle = new Bundle();
            bundle.putLong(Constant.ORDER_ID, mOrder.getId());
            GlobalFunction.startActivity(TrackingOrderActivity.this,
                    ReceiptOrderActivity.class, bundle);
            finish();
        });

        tvTakeOrder.setOnClickListener(view -> {
            if (mOrder != null && mOrder.getStatus() == Order.STATUS_ARRIVED) {
                updateStatusOrder(Order.STATUS_COMPLETE);
            }
        });
        tvHuy.setOnClickListener(view -> {
            if (mOrder != null && mOrder.getStatus() == Order.STATUS_NEW) {
                updateStatusOrder(Order.STATUS_CANCELLED);
            }

        });
    }

    private void updateOrderStatusAfterRefund() {
        if (mOrder == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("status", Order.STATUS_CANCELLED);

        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(mOrder.getId()))
                .updateChildren(map, (error, ref) -> {
                    if (error != null) {
                        Toast.makeText(TrackingOrderActivity.this, "Lỗi cập nhật trạng thái đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        finish(); // Hoàn tất và quay lại
                    }
                });
    }


    private void getOrderDetailFromFirebase() {
        showProgressDialog(true);
        MyApplication.get(this).getOrderDetailDatabaseReference(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showProgressDialog(false);
                        mOrder = snapshot.getValue(Order.class);
                        if (mOrder == null) return;

                        initData();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showProgressDialog(false);
                        showToastMessage(getString(R.string.msg_get_date_error));
                    }
                });
    }

    private void initData() {
        DrinkOrderAdapter adapter = new DrinkOrderAdapter(mOrder.getDrinks());
        rcvDrinks.setAdapter(adapter);

        switch (mOrder.getStatus()) {
            case Order.STATUS_NEW:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_disable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                imgStep3.setImageResource(R.drawable.ic_step_disable);

                tvHuy.setBackgroundResource(R.drawable.bg_button_enable_corner_16);
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_disable_corner_16);
                tvTakeOrderMessage.setVisibility(View.GONE);
                break;

            case Order.STATUS_DOING:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_enable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setImageResource(R.drawable.ic_step_disable);
                tvHuy.setBackgroundResource(R.drawable.bg_button_disable_corner_16);
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_disable_corner_16);
                tvTakeOrderMessage.setVisibility(View.GONE);
                break;

            case Order.STATUS_ARRIVED:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_enable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setImageResource(R.drawable.ic_step_enable);
                tvHuy.setBackgroundResource(R.drawable.bg_button_disable_corner_16);
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_enable_corner_16);
                tvTakeOrderMessage.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateStatusOrder(int status) {
        if (mOrder == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(mOrder.getId()))
                .updateChildren(map, (error, ref) -> {
                    if (error != null) {
                        showToastMessage("Error updating order status: " + error.getMessage());
                    } else {
                        if (Order.STATUS_COMPLETE == status || Order.STATUS_CANCELLED == status) {
                            finish();
                        }
                    }
                });
    }
}
