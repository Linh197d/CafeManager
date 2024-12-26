package com.app.shopfee.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.MyApplication;
import com.app.shopfee.R;
import com.app.shopfee.adapter.AddressAdapter;
import com.app.shopfee.event.AddressSelectedEvent;
import com.app.shopfee.model.Address;
import com.app.shopfee.prefs.DataStoreManager;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.app.shopfee.utils.StringUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends BaseActivity {

    private List<Address> listAddress;
    private AddressAdapter addressAdapter;
    private long addressSelectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        loadDataIntent();
        initToolbar();
        initUi();
        loadListAddressFromFirebase();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        addressSelectedId = bundle.getLong(Constant.ADDRESS_ID, 0);
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText(getString(R.string.address_title));
    }

    private void initUi() {
        RecyclerView rcvAddress = findViewById(R.id.rcv_address);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvAddress.setLayoutManager(linearLayoutManager);
        listAddress = new ArrayList<>();

        addressAdapter = new AddressAdapter(listAddress, new AddressAdapter.IClickAddressListener() {
            @Override
            public void onClickAddressItem(Address address) {
                handleClickAddress(address);
            }

            @Override
            public void onClickDeleteAddressItem(Address address) {
                deleteAddress(address);
            }

            @Override
            public void onClickEditAddressItem(Address address) {
                editAddress(address);
            }
        });

        rcvAddress.setAdapter(addressAdapter);

        Button btnAddAddress = findViewById(R.id.btn_add_address);
        btnAddAddress.setOnClickListener(view -> onClickAddAddress());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadListAddressFromFirebase() {
        showProgressDialog(true);
        String userEmail = DataStoreManager.getUser().getEmail();
        MyApplication.get(this).getAddressDatabaseReference()
                .orderByChild("userEmail").equalTo(userEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showProgressDialog(false);
                        resetListAddress();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Address address = dataSnapshot.getValue(Address.class);
                            if (address != null) {
                                listAddress.add(0, address);
                            }
                        }
                        if (addressSelectedId > 0 && listAddress != null && !listAddress.isEmpty()) {
                            for (Address address : listAddress) {
                                if (address.getId() == addressSelectedId) {
                                    address.setSelected(true);
                                    break;
                                }
                            }
                        }

                        if (addressAdapter != null) addressAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showProgressDialog(false);
                        showToastMessage(getString(R.string.msg_get_date_error));
                    }
                });
    }

    private void resetListAddress() {
        if (listAddress != null) {
            listAddress.clear();
        } else {
            listAddress = new ArrayList<>();
        }
    }

    private void handleClickAddress(Address address) {
        EventBus.getDefault().post(new AddressSelectedEvent(address));
        finish();
    }

    @SuppressLint("InflateParams")
    public void onClickAddAddress() {
        View viewDialog = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_address, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        EditText edtName = viewDialog.findViewById(R.id.edt_name);
        EditText edtPhone = viewDialog.findViewById(R.id.edt_phone);
        EditText edtAddress = viewDialog.findViewById(R.id.edt_address);
        TextView tvCancel = viewDialog.findViewById(R.id.tv_cancel);
        TextView tvAdd = viewDialog.findViewById(R.id.tv_add);

        tvCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        tvAdd.setOnClickListener(v -> {
            String strName = edtName.getText().toString().trim();
            String strPhone = edtPhone.getText().toString().trim();
            String strAddress = edtAddress.getText().toString().trim();

            if (StringUtil.isEmpty(strName) || StringUtil.isEmpty(strPhone) || StringUtil.isEmpty(strAddress)) {
                GlobalFunction.showToastMessage(this, getString(R.string.message_enter_infor));
            } else {
                long id = System.currentTimeMillis();
                String userEmail = DataStoreManager.getUser().getEmail();
                Address address = new Address(id, strName, strPhone, strAddress, userEmail);
                MyApplication.get(this).getAddressDatabaseReference()
                        .child(String.valueOf(id))
                        .setValue(address, (error1, ref1) -> {
                            if (error1 == null) {
                                GlobalFunction.showToastMessage(this,
                                        getString(R.string.msg_add_address_success));
                                GlobalFunction.hideSoftKeyboard(this);
                                bottomSheetDialog.dismiss();
                            } else {
                                GlobalFunction.showToastMessage(this,
                                        "Lỗi khi thêm địa chỉ: " + error1.getMessage());
                            }
                        });
            }
        });

        bottomSheetDialog.show();
    }

    private void deleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage("Bạn có chắc muốn xoá?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference addressRef = MyApplication.get(AddressActivity.this)
                                .getAddressDatabaseReference().child(String.valueOf(address.getId()));
                        addressRef.removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    Toast.makeText(getApplicationContext(), "Đã xoá thành công", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Không thể xoá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @SuppressLint("InflateParams")
    private void editAddress(Address address) {
        View viewDialog = getLayoutInflater().inflate(R.layout.layout_edit_address, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        EditText edtName = viewDialog.findViewById(R.id.edt_edit_name);
        EditText edtPhone = viewDialog.findViewById(R.id.edt_edit_phone);
        EditText edtAddress = viewDialog.findViewById(R.id.edt_edit_address);
        TextView tvCancel = viewDialog.findViewById(R.id.tv_edit_cancel);
        TextView tvSave = viewDialog.findViewById(R.id.tv_edit);

        edtName.setText(address.getName());
        edtPhone.setText(address.getPhone());
        edtAddress.setText(address.getAddress());

        tvCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        tvSave.setOnClickListener(v -> {
            String strName = edtName.getText().toString().trim();
            String strPhone = edtPhone.getText().toString().trim();
            String strAddress = edtAddress.getText().toString().trim();

            if (StringUtil.isEmpty(strName) || StringUtil.isEmpty(strPhone) || StringUtil.isEmpty(strAddress)) {
                GlobalFunction.showToastMessage(this, getString(R.string.message_enter_infor));
            } else {
                int newPhone = Integer.parseInt(strPhone);
                Address updatedAddress = new Address(address.getId(), strName, String.valueOf(newPhone), strAddress, address.getUserEmail());

                DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("address").child(String.valueOf(address.getId()));
                addressRef.setValue(updatedAddress)
                        .addOnSuccessListener(aVoid -> {
                            GlobalFunction.showToastMessage(this, "Cập nhật thành công");
                            GlobalFunction.hideSoftKeyboard(this);
                            bottomSheetDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            GlobalFunction.showToastMessage(this, "Cập nhật thất bại: " + e.getMessage());
                        });
            }
        });

        bottomSheetDialog.show();
    }

}
