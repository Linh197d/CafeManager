package com.app.shopfee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopfee.R;
import com.app.shopfee.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<Address> listAddress;
    private final IClickAddressListener iClickAddressListener;

    public AddressAdapter(List<Address> list, IClickAddressListener listener) {
        this.listAddress = list;
        this.iClickAddressListener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = listAddress.get(position);
        if (address == null) return;
        holder.tvName.setText(address.getName());
        holder.tvPhone.setText(address.getPhone());
        holder.tvAddress.setText(address.getAddress());
        if (address.isSelected()) {
            holder.imgStatus.setImageResource(R.drawable.ic_item_selected);
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_item_unselect);
        }

        holder.imgStatus.setOnClickListener(view -> iClickAddressListener.onClickAddressItem(address));
        if (holder.imgDelete != null) {
            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iClickAddressListener != null) {
                        iClickAddressListener.onClickDeleteAddressItem(address);
                    }
                }
            });
        }
        if (holder.imgEdite != null) {
            holder.imgEdite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iClickAddressListener != null) {
                        iClickAddressListener.onClickEditAddressItem(address);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (listAddress != null) {
            return listAddress.size();
        }
        return 0;
    }

    public interface IClickAddressListener {
        void onClickAddressItem(Address address);

        void onClickDeleteAddressItem(Address address);

        void onClickEditAddressItem(Address address);
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgStatus, imgDelete, imgEdite;
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStatus = itemView.findViewById(R.id.img_status);
            imgDelete = itemView.findViewById(R.id.img_delete_address);
            imgEdite = itemView.findViewById(R.id.img_edit_address);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
}
