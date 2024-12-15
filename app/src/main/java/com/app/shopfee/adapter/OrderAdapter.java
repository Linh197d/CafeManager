package com.app.shopfee.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.R;
import com.app.shopfee.model.DrinkOrder;
import com.app.shopfee.model.Order;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlideUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private final List<Order> listOrder;
    private final IClickOrderListener iClickOrderListener;

    public interface IClickOrderListener {
        void onClickTrackingOrder(long orderId);
        void onClickReceiptOrder(Order order);
        void onClickRatingReview(Order order);
    }

    public OrderAdapter(Context context, List<Order> list, IClickOrderListener listener) {
        this.context = context;
        this.listOrder = list;
        this.iClickOrderListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = listOrder.get(position);

        if (order == null) return;

        List<DrinkOrder> drinks = order.getDrinks();
        if (drinks.isEmpty()) return;

        DrinkOrder firstDrinkOrder = drinks.get(0);
        if (firstDrinkOrder == null) return;

        GlideUtils.loadUrl(firstDrinkOrder.getImage(), holder.imgDrink);

        holder.tvOrderId.setText(String.valueOf(order.getId()));
        holder.tvTotal.setText(String.valueOf(order.getTotal()) + Constant.CURRENCY);
        holder.tvDrinksName.setText(order.getListDrinksName());
        holder.tvQuantity.setText("(" + drinks.size() + " " + context.getString(R.string.label_item) + ")");

        int orderStatus = order.getStatus();
        switch (orderStatus) {
            case Order.STATUS_COMPLETE:
                holder.tvSuccess.setVisibility(View.VISIBLE);
                holder.tvSuccess.setText(context.getString(R.string.label_success));
                holder.tvSuccess.setBackgroundResource(R.drawable.bg_white_corner_6_border_green);
                holder.tvSuccess.setTextColor(Color.GREEN);
                holder.tvAction.setText(context.getString(R.string.label_receipt_order));
                holder.layoutReview.setVisibility(View.VISIBLE);
                holder.tvRate.setText(String.valueOf(order.getRate()));
                holder.tvReview.setText(order.getReview());
                holder.layoutReview.setOnClickListener(v -> {
                    if (iClickOrderListener != null) {
                        iClickOrderListener.onClickRatingReview(order);
                    }
                });
                holder.layoutAction.setOnClickListener(v ->
                        iClickOrderListener.onClickReceiptOrder(order));
                break;
            case Order.STATUS_CANCELLED:
                holder.tvSuccess.setVisibility(View.VISIBLE);
                holder.tvSuccess.setText("Đã hủy");
                holder.tvSuccess.setBackgroundResource(R.drawable.bg_white_corner_6_border_red);
                holder.tvSuccess.setTextColor(Color.RED);
                holder.tvAction.setText(context.getString(R.string.label_receipt_order));
                holder.layoutReview.setVisibility(View.GONE);
                holder.layoutAction.setOnClickListener(v ->
                        iClickOrderListener.onClickReceiptOrder(order));
                break;
            default:
                holder.tvSuccess.setVisibility(View.GONE);
                holder.tvAction.setText(context.getString(R.string.label_tracking_order));
                holder.layoutReview.setVisibility(View.GONE);
                holder.layoutAction.setOnClickListener(v ->
                        iClickOrderListener.onClickTrackingOrder(order.getId()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }

    public void release() {
        context = null;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgDrink;
        private final TextView tvOrderId;
        private final TextView tvTotal;
        private final TextView tvDrinksName;
        private final TextView tvQuantity;
        private final TextView tvSuccess;
        private final LinearLayout layoutAction;
        private final TextView tvAction;
        private final LinearLayout layoutReview;
        private final TextView tvRate;
        private final TextView tvReview;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDrink = itemView.findViewById(R.id.img_drink);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvDrinksName = itemView.findViewById(R.id.tv_drinks_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvSuccess = itemView.findViewById(R.id.tv_success);
            layoutAction = itemView.findViewById(R.id.layout_action);
            tvAction = itemView.findViewById(R.id.tv_action);
            layoutReview = itemView.findViewById(R.id.layout_review);
            tvRate = itemView.findViewById(R.id.tv_rate);
            tvReview = itemView.findViewById(R.id.tv_review);
        }
    }
}
