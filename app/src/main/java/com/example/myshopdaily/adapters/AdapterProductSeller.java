package com.example.myshopdaily.adapters;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshopdaily.FilterProduct;
import com.example.myshopdaily.FilterProductUser;
import com.example.myshopdaily.R;
import com.example.myshopdaily.activites.EditProductActivity;
import com.example.myshopdaily.activites.MainSellerActivity;
import com.example.myshopdaily.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {


    private MainSellerActivity context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProduct filter;

    public AdapterProductSeller(MainSellerActivity context, ArrayList<ModelProduct> productList){

        this.context =context;
        this.productList=productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout

        View view= LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
//get data
        ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String discountAvailable =modelProduct.getDiscountAvailable();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon =modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data

        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("₹"+discountPrice);
        holder.originalPriceTv.setText("₹"+originalPrice);


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

        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic__add_shopping_grey).into(holder.productIconTv);
        }
        catch (Exception e){
            holder.productIconTv.setImageResource(R.drawable.ic__add_shopping_grey);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //handle item clicks  show item details(in bottom sheet)
                detailsBottomSheet(modelProduct);//here modelProduct contains details of clicked product0
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {

        //bottom sheet

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottomSheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_detail_seller,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);




        //init views to bottom sheet
        ImageButton backBtn =  view.findViewById(R.id.backBtn);
        ImageButton deleteBtn =  view.findViewById(R.id.deleteBtn);
        ImageButton editBtn =  view.findViewById(R.id.editBtn);
        TextView nameTv = view.findViewById(R.id.nameTv);
        ImageView productIconTv = view.findViewById(R.id.productIconTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descTv = view.findViewById(R.id.descTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

//get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String discountAvailable =modelProduct.getDiscountAvailable();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon =modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();


        //set data
        titleTv.setText(title);
        descTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("₹"+discountPrice);
        originalPriceTv.setText("₹"+originalPrice);
        if (discountAvailable.equals("true")){
            //product disc
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//ADD STRIKE THROUGH DISCOUNT
        }
        else{
            //PRODUCT DON'T ON DC
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);

        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic__add_shopping_grey).into(productIconTv);
        }
        catch (Exception e){
            productIconTv.setImageResource(R.drawable.ic__add_shopping_grey);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //open edit product activity,pass id of product
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);
            }
        });
//delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are You Sure"+title+" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //delete
                                deleteProduct(id);//id is the product id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //cancel,dismiss dialog
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

            }
        });
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {

        //delete prdt using id

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product deleted
                        Toast.makeText(context,"Product deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }


    class HolderProductSeller extends RecyclerView.ViewHolder{

        /*holds views  of recyclerView*/

        private ImageView productIconTv;
        private TextView discountedNoteTv, titleTv,quantityTv,discountedPriceTv,originalPriceTv;





        public HolderProductSeller(@NonNull View itemView){
            super(itemView);

            productIconTv= itemView.findViewById(R.id.productIconTv);
            discountedNoteTv=itemView.findViewById(R.id.discountedPriceTv);
            titleTv=itemView.findViewById(R.id.titleTv);
            quantityTv=itemView.findViewById(R.id.quantityTv);
            discountedNoteTv=itemView.findViewById(R.id.discountedNoteTv);
            discountedPriceTv=itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv=itemView.findViewById(R.id.originalPriceTv);


        }
    }
}
