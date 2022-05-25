package com.example.myshopdaily.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshopdaily.FilterProductUser;
import com.example.myshopdaily.R;
import com.example.myshopdaily.activites.ShopDetailsActivity;
import com.example.myshopdaily.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;


public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {


    private Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProductUser filter;


    public AdapterProductUser(ShopDetailsActivity context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList=productsList;
    }



    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout

        View view= LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data
        ModelProduct modelProduct=productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice(); ;
        String productDescription = modelProduct.getProductDescription();
        String productTitle =modelProduct.getProductTitle();
        String productQuantity =modelProduct.getProductQuantity();
        String productId=modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();


        //set data
        holder.titleTv.setText(productTitle);
        holder.discountedNoteTv.setText(discountNote);
        holder.descTv.setText(productDescription);
        holder.originalPriceTv.setText("$"+originalPrice);
        holder.discountedPriceTv.setText("$"+discountPrice);

        if (discountAvailable.equals("true")){
            //product disc
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//ADD STRIKE THROUGH DISCOUNT
        }
        else{
            //PRODUCT DON'T ON DC
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);

        }
        try{
            Picasso.get().load(productIcon).placeholder(R.drawable.ic__add_shopping_grey).into(holder.productIconTv);
        }
        catch (Exception e){
            holder.productIconTv.setImageResource(R.drawable.ic__add_shopping_grey);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show product details
            }
        });
    }
    private double cost =0;
    private double finalCost =0;
    private int quantity=0;
    private void showQuantityDialog(ModelProduct modelProduct) {

        //inflate layout

        View view= LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);

        //init layout views

        ImageView productTv = view.findViewById(R.id.productTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descTv = view.findViewById(R.id.descTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model

        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        String price;
        if (modelProduct.getDiscountAvailable().equals("true")){

            //product have discount
            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//ADD STRIKE THROUGH DISCOUNT
        }
        else {
            //product dont have dc

            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price=modelProduct.getOriginalPrice();

        }

        cost = Double.parseDouble(price.replaceAll("₹",""));
        finalCost = Double.parseDouble(price.replaceAll("₹",""));
        quantity =1;

        //dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setView(view);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_cart2_grey).into(productTv);
        }
        catch(Exception e){
            productTv.setImageResource(R.drawable.ic_cart2_grey);
        }
        titleTv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descTv.setText(""+description);
        discountedNoteTv.setText(""+discountNote);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("₹"+modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("₹"+modelProduct.getDiscountPrice());
        finalPriceTv.setText("₹"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();


        //increase quanitity
        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost+cost;
                quantity++;

                finalPriceTv.setText("₹"+finalCost);
                quantityTv.setText(""+quantity);

            }
        });
        //decrement quantity
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quantity>1){
                    finalCost=finalCost-cost;
                    quantity--;

                    finalPriceTv.setText("₹"+finalCost);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalprice = finalPriceTv.getText().toString().trim().replace("₹","");
                String quantity = quantityTv.getText().toString().trim();

                addtoCart(productId,title,priceEach,totalprice,quantity);

                dialog.dismiss();
            }
        });


    }


    private int itemId=1;
    private void addtoCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;

        EasyDB easyDB= EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PId", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_ID", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart....", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity)context).cartCount();




    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProductUser(this,filterList);
        }
        return filter;

    }

    class HolderProductUser extends RecyclerView.ViewHolder {



        //ui views
        private ImageView productIconTv ;
        private TextView discountedNoteTv,titleTv,descTv, addToCartTv,discountedPriceTv,originalPriceTv;




        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            //init views
            productIconTv=itemView.findViewById(R.id.productIconTv);
            discountedNoteTv=itemView.findViewById(R.id.discountedNoteTv);
            titleTv=itemView.findViewById(R.id.titleTv);
            descTv=itemView.findViewById(R.id.descTv);
            addToCartTv=itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv=itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv=itemView.findViewById(R.id.originalPriceTv);

        }
    }
}
