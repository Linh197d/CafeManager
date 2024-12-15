package com.app.shopfee.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.app.shopfee.R;
import com.app.shopfee.activity.CategoryDetailActivity;
import com.app.shopfee.activity.DrinkDetailActivity;
import com.app.shopfee.activity.ListdrinkActivity;
import com.app.shopfee.adapter.BannerAdapter;
import com.app.shopfee.adapter.CategoryAdapter;
import com.app.shopfee.adapter.FeaturedAdapter;
import com.app.shopfee.listener.IClickCategoryListener;
import com.app.shopfee.listener.IClickDrinkListener;
import com.app.shopfee.model.Banner;
import com.app.shopfee.model.Category;
import com.app.shopfee.model.Drink;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {

    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private BannerAdapter bannerAdapter;
    private List<Banner> mlistBanner;
    private Timer mTimer;

    private RecyclerView recyclerFatured;
    private FeaturedAdapter adapterFatured;
    private List<Drink> listFatured;

    private RecyclerView recyclerCategory;
    private CategoryAdapter adapterCategory;
    private List<Category> listCategory;
    private TextView edtsearch;

    private View mView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        initUi();
        return mView;
    }

    private void initUi() {
        viewPager = mView.findViewById(R.id.view_pager);
        circleIndicator = mView.findViewById(R.id.circle_indicator);
        edtsearch = mView.findViewById(R.id.edt_search_name);
        displaySearch();

        mlistBanner = getListBanner();
        bannerAdapter = new BannerAdapter(getContext(), mlistBanner);
        viewPager.setAdapter(bannerAdapter);
        circleIndicator.setViewPager(viewPager);
        bannerAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());
        autoBannerImages();

        recyclerFatured = mView.findViewById(R.id.list_fatured);
        LinearLayoutManager linearLayoutFatured = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerFatured.setLayoutManager(linearLayoutFatured);
        recyclerFatured.setAdapter(adapterFatured);
        listFatured = new ArrayList<>();
        getListFeaturedFromFirebase();

        recyclerCategory = mView.findViewById(R.id.list_category);
        LinearLayoutManager linearLayoutCategory = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        listCategory = new ArrayList<>();
        recyclerCategory.setLayoutManager(linearLayoutCategory);
        adapterCategory = new CategoryAdapter(new ArrayList<>(), new IClickCategoryListener() {
            @Override
            public void onClickDrinkCategory(Category category) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.CATEGORY_ID, category.getId());
                GlobalFunction.startActivity(getActivity(), CategoryDetailActivity.class, bundle);

            }
        });
        listCategory = new ArrayList<>();
        recyclerCategory.setAdapter(adapterCategory);
        getListCategoryFromFirebase();
    }

    private List<Banner> getListBanner() {
        List<Banner> list = new ArrayList<>();
        list.add(new Banner(R.drawable.banner3));
        list.add(new Banner(R.drawable.banner5));
        list.add(new Banner(R.drawable.banner1));
        list.add(new Banner(R.drawable.banner4));
        list.add(new Banner(R.drawable.banner2));
        return list;
    }
    private void autoBannerImages(){
        if (mlistBanner == null || mlistBanner.isEmpty() || mTimer != null) {
            return;
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int currentItem = viewPager.getCurrentItem();
                        int totalItem = mlistBanner.size() - 1;
                        if (currentItem < totalItem) {
                            currentItem ++;
                            viewPager.setCurrentItem(currentItem);
                        } else {
                            viewPager.setCurrentItem(0);
                        }
                    }
                });
            }
        }, 500, 3000);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void getListCategoryFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("category");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> tempList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    tempList.add(category);
                }
                if (adapterCategory != null) {
                    adapterCategory.setData(tempList);
                }
                listCategory.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getListFeaturedFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("drink");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Drink> tempList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drink drink = snapshot.getValue(Drink.class);
                    if (drink != null && drink.isFeatured()) {
                        tempList.add(drink);
                    }
                }

                if (adapterFatured != null) {
                    adapterFatured.setData(tempList);
                }
                listFatured.clear();
                listFatured.addAll(tempList);
                displayListFeatured();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void displayListFeatured() {
        FeaturedAdapter adapter = new FeaturedAdapter(listFatured, new IClickDrinkListener() {
            @Override
            public void onClickDrinkItem(Drink drink) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.DRINK_ID, drink.getId());
                GlobalFunction.startActivity(getActivity(), DrinkDetailActivity.class, bundle);
            }
        });
        recyclerFatured.setAdapter(adapter);
    }
private void displaySearch(){
    edtsearch.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle bundle = new Bundle();
            GlobalFunction.startActivity(getActivity(), ListdrinkActivity.class, bundle);
        }
    });
}

}
