package com.app.shopfee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.R;
import com.app.shopfee.listener.IClickCategoryListener;
import com.app.shopfee.model.Category;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final IClickCategoryListener iClickCategoryListener;
    private List<Category> categoryList;

    public CategoryAdapter(List<Category> list, IClickCategoryListener iClickCategoryListener) {
        this.categoryList = list != null ? list : new ArrayList<>();
        this.iClickCategoryListener = iClickCategoryListener;
    }

    public void setData(List<Category> categoryList) {
        this.categoryList.clear();
        if (categoryList != null) {
            this.categoryList.addAll(categoryList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        if (category != null) {
            String bannerUrl = category.getBanner();
            if (bannerUrl != null && !bannerUrl.isEmpty()) {
                Picasso.get()
                        .load(bannerUrl)
                        .placeholder(R.drawable.image_no_available)
                        .error(R.drawable.image_no_available)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.image_no_available);
            }

            holder.textViewName.setText(category.getName());
            holder.itemView.setOnClickListener(view -> iClickCategoryListener.onClickDrinkCategory(category));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textViewName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imganh);
            textViewName = itemView.findViewById(R.id.txtten);
        }
    }
}
