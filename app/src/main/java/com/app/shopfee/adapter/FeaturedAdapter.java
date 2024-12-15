package com.app.shopfee.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.R;
import com.app.shopfee.listener.IClickDrinkListener;
import com.app.shopfee.model.Category;
import com.app.shopfee.model.Drink;
import com.app.shopfee.utils.Constant;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.DrinkViewHolder>{
    private final IClickDrinkListener iClickDrinkListener;
    private List<Drink> drinks;

    public FeaturedAdapter(List<Drink> list, IClickDrinkListener listener) {
        this.drinks = list;
        this.iClickDrinkListener = listener;
    }
    public void setData(List<Drink> drinks) {
        if (drinks != null) {
            this.drinks = drinks;
        } else {
            this.drinks = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedAdapter.DrinkViewHolder holder, int position) {
        Drink drink = drinks.get(position);
        if (drink != null) {
            String bannerUrl = drink.getBanner();
            if (bannerUrl != null && !bannerUrl.isEmpty()) {
                Picasso.get()
                        .load(bannerUrl)
                        .placeholder(R.drawable.image_no_available)
                        .error(R.drawable.image_no_available)
                        .into(holder.imageView);

            } else {
                holder.imageView.setImageResource(R.drawable.image_no_available);
            }
            holder.cardView.setOnClickListener(view
                    -> iClickDrinkListener.onClickDrinkItem(drink));
            holder.textViewName.setText(drink.getName());


            if (drink.getSale() <= 0) {
                String strPrice = drink.getPrice() + Constant.CURRENCY;
                holder.textsale.setText(strPrice);
            } else {
                holder.textprice.setVisibility(View.VISIBLE);

                String strOldPrice = drink.getPrice() + Constant.CURRENCY;
                holder.textprice.setText(strOldPrice);
                holder.textprice.setPaintFlags(holder.textprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                String strRealPrice = drink.getRealPrice() + Constant.CURRENCY;
                holder.textsale.setText(strRealPrice);
            }
        }
    }
    @Override
    public int getItemCount() {
        return drinks.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewName;
        private TextView textprice, textsale;
        private CardView cardView;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imganh);
            textViewName = itemView.findViewById(R.id.txtten);
            textprice = itemView.findViewById(R.id.txtgia);
            textsale = itemView.findViewById(R.id.txtsale);
            cardView = (CardView) itemView.findViewById(R.id.CardView);
        }
    }
}
