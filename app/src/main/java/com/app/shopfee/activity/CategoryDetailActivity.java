package com.app.shopfee.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.MyApplication;
import com.app.shopfee.R;
import com.app.shopfee.adapter.DrinkAdapter;
import com.app.shopfee.event.SearchKeywordEvent;
import com.app.shopfee.model.Drink;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CategoryDetailActivity extends AppCompatActivity {
    private RecyclerView rcvDrink;
    private TextView txtname;
    private List<Drink> listDrink;
    private List<Drink> listDrinkDisplay;
    private DrinkAdapter drinkAdapter;
    private int categoryId;
    private EditText edtSearchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        initToolbar();
        getDataArguments();
        initUi();

        getListDrink();
        getCategoryName(categoryId);
    }

    private void getDataArguments() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            categoryId = bundle.getInt(Constant.CATEGORY_ID);
        }
    }

    private void initUi() {
        rcvDrink = findViewById(R.id.rvc_item_category);
        txtname = findViewById(R.id.txtname);
        edtSearchName = findViewById(R.id.edt_search_name);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDrink.setLayoutManager(layoutManager);

        listDrink = new ArrayList<>();
        listDrinkDisplay = new ArrayList<>();

        drinkAdapter = new DrinkAdapter(listDrinkDisplay, drink -> {
            Bundle bundle = new Bundle();
            bundle.putInt(Constant.DRINK_ID, drink.getId());
            GlobalFunction.startActivity(this, DrinkDetailActivity.class, bundle);
        });
        rcvDrink.setAdapter(drinkAdapter);

        edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString().trim().toLowerCase();
                filterList(query);
            }
        });
    }

    private void filterList(String query) {
        String normalizedQuery = normalizeString(query);
        List<Drink> filterList = new ArrayList<>();
        for (Drink drink : listDrink) {
            String normalizedName = normalizeString(drink.getName());
            if (normalizedName.contains(normalizedQuery)) {
                filterList.add(drink);
            }
        }
        listDrinkDisplay.clear();
        listDrinkDisplay.addAll(filterList);
        drinkAdapter.notifyDataSetChanged();
    }

    private String normalizeString(String input) {
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase();
    }

    private void getListDrink() {
        MyApplication.get(this).getDrinkDatabaseReference()
                .orderByChild(Constant.CATEGORY_ID).equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listDrink = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Drink drink = dataSnapshot.getValue(Drink.class);
                            if (drink != null) {
                                listDrink.add(drink);
                            }
                        }
                        updateDrinkList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void updateDrinkList() {
        listDrinkDisplay.clear();
        listDrinkDisplay.addAll(listDrink);
        drinkAdapter.notifyDataSetChanged();
    }

    private void getCategoryName(int categoryId) {
        MyApplication.get(this).getCategoryDatabaseReference().child(String.valueOf(categoryId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String categoryName = snapshot.child("name").getValue(String.class);
                            if (categoryName != null) {
                                txtname.setText(categoryName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText("");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchKeywordEvent(SearchKeywordEvent event) {
        String keyword = event.getKeyword().toLowerCase();
        filterList(keyword);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
