package com.example.myshopdaily.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshopdaily.R;
import com.example.myshopdaily.activites.ShopDetailsActivity;
import com.example.myshopdaily.models.ModelCartItem;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{

    private EasyDB easyDB;

    private final Context context;
    private final ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems){
        this.context = context;
        this.cartItems = cartItems;
    }



    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {

        //get data
        ModelCartItem modelCartItem = cartItems.get(position);

        String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listner, delete item from cart
        holder.itemRemoveTv.setOnClickListener(view -> {
            //will create table if not exists, but in that case will must exists
            easyDB= EasyDB.init(context, "ITEMS_DB")
                    .setTableName("ITEMS_TABLE")
                    .addColumn(new Column("Item_Id", "text", "unique"))
                    .addColumn(new Column("Item_PId", "text", "not null"))
                    .addColumn(new Column("Item_Name", "text", "not null"))
                    .addColumn(new Column("Item_Price_Each", "text", "not null"))
                    .addColumn(new Column("Item_Price", "text", "not null"))
                    .addColumn(new Column("Item_Quantity", "text", "not null"))
                    .doneTableColumn();
            easyDB.deleteRow(1, id);
            Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

            //refresh list
            cartItems.remove(position);
            notifyItemChanged(position);
            notifyDataSetChanged();

            double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().trim().replace("₹","")));
            double totalPrice = tx - Double.parseDouble(cost.replace("₹",""));
            double deliverFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("₹","")));
            double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("%.2f", deliverFee));
            ((ShopDetailsActivity)context).allTotalPrice = 0.00;
            ((ShopDetailsActivity)context).sTotalTv.setText("₹"+ String.format("%.2f", sTotalPrice));
            ((ShopDetailsActivity)context).allTotalPriceTv.setText("₹"+ String.format("%.2f", Double.parseDouble(String.format("%.2f",totalPrice))));

            //after removing item from cart, update cart count
//                 ((ShopDetailsActivity)context).cartCount();
            ((ShopDetailsActivity)context).cartCount();
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); // return number of records
    }

    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder{
        //ui views of row_cartitems.xml
        private final TextView itemTitleTv;
        private final TextView itemPriceTv;
        private final TextView itemPriceEachTv;
        private final TextView itemQuantityTv;
        private final TextView itemRemoveTv;

        public HolderCartItem(@Nonnull View itemView){
            super(itemView);
            // init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}
