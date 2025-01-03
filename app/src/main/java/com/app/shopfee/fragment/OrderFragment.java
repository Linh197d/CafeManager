package com.app.shopfee.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.MyApplication;
import com.app.shopfee.R;
import com.app.shopfee.activity.RatingReviewActivity;
import com.app.shopfee.activity.ReceiptOrderActivity;
import com.app.shopfee.activity.TrackingOrderActivity;
import com.app.shopfee.adapter.OrderAdapter;
import com.app.shopfee.model.Order;
import com.app.shopfee.model.RatingReview;
import com.app.shopfee.model.TabOrder;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private View mView;
    private int orderTabType;
    private List<Order> listOrder;
    private OrderAdapter orderAdapter;

    public static OrderFragment newInstance(int type) {
        OrderFragment orderFragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.ORDER_TAB_TYPE, type);
        orderFragment.setArguments(bundle);
        return orderFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_order, container, false);

        getDataArguments();
        initUi();
        getListOrderFromFirebase();

        return mView;
    }

    private void getDataArguments() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        orderTabType = bundle.getInt(Constant.ORDER_TAB_TYPE);
    }

    private void initUi() {
        listOrder = new ArrayList<>();
        RecyclerView rcvOrder = mView.findViewById(R.id.rcv_order);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvOrder.setLayoutManager(linearLayoutManager);
        orderAdapter = new OrderAdapter(getActivity(), listOrder, new OrderAdapter.IClickOrderListener() {
            @Override
            public void onClickTrackingOrder(long orderId) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.ORDER_ID, orderId);
                GlobalFunction.startActivity(getActivity(), TrackingOrderActivity.class, bundle);
            }

            @Override
            public void onClickReceiptOrder(Order order) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.ORDER_ID, order.getId());
                GlobalFunction.startActivity(getActivity(), ReceiptOrderActivity.class, bundle);
            }

            @Override
            public void onClickRatingReview(Order order) {
                RatingReview ratingReview = new RatingReview(RatingReview.TYPE_RATING_REVIEW_ORDER, String.valueOf(order.getId()));
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constant.RATING_REVIEW_OBJECT, ratingReview);
                GlobalFunction.startActivity(getActivity(), RatingReviewActivity.class, bundle);
            }
        });
        rcvOrder.setAdapter(orderAdapter);
    }

    private void getListOrderFromFirebase() {
        if (getActivity() == null) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            return;
        }
        DatabaseReference ordersRef = MyApplication.get(getActivity()).getOrderDatabaseReference();
        Query query = ordersRef.orderByChild("userEmail").equalTo(userEmail);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOrder.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        if (TabOrder.TAB_ORDER_PROCESS == orderTabType) {
                            if (Order.STATUS_COMPLETE != order.getStatus() && Order.STATUS_CANCELLED != order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        } else if (TabOrder.TAB_ORDER_DONE == orderTabType) {
                            if (Order.STATUS_COMPLETE == order.getStatus() || Order.STATUS_CANCELLED == order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        }
                    }
                }
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (orderAdapter != null) orderAdapter.release();
    }
}
