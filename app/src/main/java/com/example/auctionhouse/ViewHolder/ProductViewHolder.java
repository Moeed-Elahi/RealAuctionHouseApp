package com.example.auctionhouse.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auctionhouse.Interfaces.ItemClickListener;
import com.example.auctionhouse.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView productName, productTimeRemaining, productPrice;
    public ImageView productImageView;
    public ItemClickListener listener;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        productImageView = itemView.findViewById(R.id.product_image);
        productName = itemView.findViewById(R.id.product_name);
        productTimeRemaining = itemView.findViewById(R.id.product_time_remaining);
        productPrice = itemView.findViewById(R.id.product_price);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        listener.OnClick(view, getAdapterPosition(), false);
    }
}
