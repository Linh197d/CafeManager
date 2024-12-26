package com.app.shopfee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.Api.CreateOrder;
import com.app.shopfee.R;
import com.app.shopfee.adapter.CartAdapter;
import com.app.shopfee.database.DrinkDatabase;
import com.app.shopfee.event.AddressSelectedEvent;
import com.app.shopfee.event.DisplayCartEvent;
import com.app.shopfee.event.OrderSuccessEvent;
import com.app.shopfee.event.PaymentMethodSelectedEvent;
import com.app.shopfee.event.VoucherSelectedEvent;
import com.app.shopfee.model.Address;
import com.app.shopfee.model.Drink;
import com.app.shopfee.model.DrinkOrder;
import com.app.shopfee.model.Order;
import com.app.shopfee.model.PaymentMethod;
import com.app.shopfee.model.Voucher;
import com.app.shopfee.prefs.DataStoreManager;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CartActivity extends BaseActivity {

    private RecyclerView rcvCart;
    private LinearLayout layoutAddOrder;
    private RelativeLayout layoutPaymentMethod;
    private TextView tvPaymentMethod;

    private RelativeLayout layoutAddress;
    private TextView tvAddress;
    private RelativeLayout layoutVoucher;
    private TextView tvVoucher;
    private TextView tvNameVoucher;
    private TextView tvPriceDrink;
    private TextView tvCountItem;
    private TextView tvAmount;
    private TextView tvPriceVoucher;
    private TextView tvCheckout;

    private List<Drink> listDrinkCart;
    private CartAdapter cartAdapter;
    private int priceDrink;
    private int mAmount;
    private PaymentMethod paymentMethodSelected;
    private Address addressSelected;
    private Voucher voucherSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initToolbar();
        initUi();
        initListener();
        initData();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.label_cart));
    }

    private void initUi() {
        rcvCart = findViewById(R.id.rcv_cart);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvCart.setLayoutManager(linearLayoutManager);
        layoutAddOrder = findViewById(R.id.layout_add_order);
        layoutPaymentMethod = findViewById(R.id.layout_payment_method);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        layoutAddress = findViewById(R.id.layout_address);
        tvAddress = findViewById(R.id.tv_address);
        layoutVoucher = findViewById(R.id.layout_voucher);
        tvVoucher = findViewById(R.id.tv_voucher);
        tvNameVoucher = findViewById(R.id.tv_name_voucher);
        tvCountItem = findViewById(R.id.tv_count_item);
        tvPriceDrink = findViewById(R.id.tv_price_drink);
        tvAmount = findViewById(R.id.tv_amount);
        tvPriceVoucher = findViewById(R.id.tv_price_voucher);
        tvCheckout = findViewById(R.id.tv_checkout);
    }

    private void initListener() {
        layoutAddOrder.setOnClickListener(v -> finish());
        layoutPaymentMethod.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            if (paymentMethodSelected != null) {
                bundle.putInt(Constant.PAYMENT_METHOD_ID, paymentMethodSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, PaymentMethodActivity.class, bundle);
        });

        layoutAddress.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            if (addressSelected != null) {
                bundle.putLong(Constant.ADDRESS_ID, addressSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, AddressActivity.class, bundle);
        });

        layoutVoucher.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt(Constant.AMOUNT_VALUE, priceDrink);
            if (voucherSelected != null) {
                bundle.putInt(Constant.VOUCHER_ID, voucherSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, VoucherActivity.class, bundle);
        });

        tvCheckout.setOnClickListener(view -> onClickPaymentZaloPay());
    }

    private void initData() {
        listDrinkCart = new ArrayList<>();
        listDrinkCart = DrinkDatabase.getInstance(this).drinkDAO().getListDrinkCart();
        if (listDrinkCart == null || listDrinkCart.isEmpty()) {
            return;
        }
        cartAdapter = new CartAdapter(listDrinkCart, new CartAdapter.IClickCartListener() {
            @Override
            public void onClickDeleteItem(Drink drink, int position) {
                DrinkDatabase.getInstance(CartActivity.this).drinkDAO().deleteDrink(drink);
                listDrinkCart.remove(position);


                displayCountItemCart();
                calculateTotalPrice();
                cartAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new DisplayCartEvent());
            }

            @Override
            public void onClickUpdateItem(Drink drink, int position) {
                DrinkDatabase.getInstance(CartActivity.this).drinkDAO().updateDrink(drink);
                cartAdapter.notifyItemChanged(position);
                calculateTotalPrice();
                cartAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new DisplayCartEvent());
            }

            @Override
            public void onClickEditItem(Drink drink) {
                DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference().child("drink");

                drinksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String drinkId = null;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Drink firebaseDrink = snapshot.getValue(Drink.class);
                            if (firebaseDrink != null && firebaseDrink.getName().equals(drink.getName())) {
                                drinkId = snapshot.getKey();
                                break;
                            }
                        }

                        if (drinkId != null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Constant.DRINK_ID, Integer.parseInt(drinkId));
                            bundle.putSerializable(Constant.DRINK_OBJECT, drink);
                            GlobalFunction.startActivity(CartActivity.this, DrinkDetailActivity.class, bundle);
                        } else {
                            Toast.makeText(CartActivity.this, "Không tìm thấy đồ uống", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CartActivity.this, "Lỗi khi truy vấn dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
        rcvCart.setAdapter(cartAdapter);
        calculateTotalPrice();
        displayCountItemCart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            initData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void displayCountItemCart() {
        String strCountItem = "(" + listDrinkCart.size() + " " + getString(R.string.label_item) + ")";
        tvCountItem.setText(strCountItem);
    }

    private void calculateTotalPrice() {
        if (listDrinkCart == null || listDrinkCart.isEmpty()) {
            String strZero = 0 + Constant.CURRENCY;
            priceDrink = 0;
            tvPriceDrink.setText(strZero);

            mAmount = 0;
            tvAmount.setText(strZero);
            return;
        }

        int totalPrice = 0;
        for (Drink drink : listDrinkCart) {
            totalPrice += drink.getTotalPrice();
        }

        priceDrink = totalPrice;
        String strPriceDrink = priceDrink + Constant.CURRENCY;
        tvPriceDrink.setText(strPriceDrink);

        mAmount = totalPrice;
        if (voucherSelected != null) {
            mAmount -= voucherSelected.getPriceDiscount(priceDrink);
        }
        String strAmount = mAmount + Constant.CURRENCY;
        tvAmount.setText(strAmount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPaymentMethodSelectedEvent(PaymentMethodSelectedEvent event) {
        if (event.getPaymentMethod() != null) {
            paymentMethodSelected = event.getPaymentMethod();
            tvPaymentMethod.setText(paymentMethodSelected.getName());
        } else {
            tvPaymentMethod.setText(getString(R.string.label_no_payment_method));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddressSelectedEvent(AddressSelectedEvent event) {
        if (event.getAddress() != null) {
            addressSelected = event.getAddress();
            tvAddress.setText(addressSelected.getAddress());
        } else {
            tvAddress.setText(getString(R.string.label_no_address));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoucherSelectedEvent(VoucherSelectedEvent event) {
        if (event.getVoucher() != null) {
            voucherSelected = event.getVoucher();
            tvVoucher.setText(voucherSelected.getTitle());
            tvNameVoucher.setText(voucherSelected.getTitle());
            String strPriceVoucher = "-" + voucherSelected.getPriceDiscount(priceDrink) + Constant.CURRENCY;
            tvPriceVoucher.setText(strPriceVoucher);
        } else {
            tvVoucher.setText(getString(R.string.label_no_voucher));
            tvNameVoucher.setText(getString(R.string.label_no_voucher));
            String strPriceVoucher = "-0" + Constant.CURRENCY;
            tvPriceVoucher.setText(strPriceVoucher);
        }
        calculateTotalPrice();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderSuccessEvent(OrderSuccessEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void onClickPaymentZaloPay() {
        if (listDrinkCart == null || listDrinkCart.isEmpty()) return;
        if (paymentMethodSelected == null) {
            showToastMessage(getString(R.string.label_choose_payment_method));
            return;
        }
        if (addressSelected == null) {
            showToastMessage(getString(R.string.label_choose_address));
            return;
        }

        if (paymentMethodSelected.getId() == 4) {
            authenticateBiometric(() -> {

                handleZaloPayPayment();
            });
        } else {
            createAndSendOrder();
        }
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
                        onSuccess.run(); // Thực hiện logic đổi mật khẩu nếu xác thực thành công
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        showToastMessage("Xác thực thất bại: " + errString);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        showToastMessage("Không thể xác thực danh tính của bạn.");
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void handleZaloPayPayment() {
        CreateOrder orderApi = new CreateOrder();
        try {
            JSONObject data = orderApi.createOrder(String.valueOf(mAmount + "000"));
            String code = data.getString("return_code");

            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(CartActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        runOnUiThread(() -> {
                            Toast.makeText(CartActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                            createAndSendOrder();
                        });
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        runOnUiThread(() -> {
                            Toast.makeText(CartActivity.this, "Đã huỷ thanh toán", Toast.LENGTH_SHORT).show();

                        });
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        runOnUiThread(() -> {
                            Toast.makeText(CartActivity.this, "Lỗi thanh toán", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Toast.makeText(this, "Lỗi tạo đơn hàng ZaloPay", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void createAndSendOrder() {
        Order orderBooking = new Order();
        orderBooking.setId(System.currentTimeMillis());
        orderBooking.setUserEmail(DataStoreManager.getUser().getEmail());
        orderBooking.setDateTime(String.valueOf(System.currentTimeMillis()));

        List<DrinkOrder> drinks = new ArrayList<>();
        for (Drink drink : listDrinkCart) {
            drinks.add(new DrinkOrder(drink.getName(), drink.getOption(), drink.getCount(), drink.getPriceOneDrink(), drink.getImage()));
        }
        orderBooking.setDrinks(drinks);
        orderBooking.setPrice(priceDrink);

        if (voucherSelected != null) {
            orderBooking.setVoucher(voucherSelected.getPriceDiscount(priceDrink));
        }
        orderBooking.setTotal(mAmount);
        orderBooking.setPaymentMethod(paymentMethodSelected.getName());
        orderBooking.setAddress(addressSelected);
        orderBooking.setStatus(Order.STATUS_NEW);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.ORDER_OBJECT, orderBooking);
        GlobalFunction.startActivity(CartActivity.this, PaymentActivity.class, bundle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
