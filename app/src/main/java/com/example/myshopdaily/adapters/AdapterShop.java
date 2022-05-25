package com.example.myshopdaily.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshopdaily.R;
import com.example.myshopdaily.activites.ShopDetailsActivity;
import com.example.myshopdaily.activites.MainUserActivity;
import com.example.myshopdaily.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop  extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private MainUserActivity context;
    public ArrayList<ModelShop> shopsList;

    public AdapterShop(MainUserActivity context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout of row hsop

        View view= LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return  new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {

        //get data
        ModelShop modelShop=shopsList.get(position);
        String accountType= modelShop.getAccountType();
        String address= modelShop.getAddress();
        String city= modelShop.getCity();
        String country= modelShop.getCountry();
        String deliveryFee= modelShop.getDeliveryFee();
        String email= modelShop.getEmail();
        String latitude= modelShop.getLatitude();
        String longitude= modelShop.getLongitude();
        String online= modelShop.getOnline();
        String name= modelShop.getName();
        String phone= modelShop.getPhone();
        String uid= modelShop.getUid();
        String timestamp= modelShop.getTimestamp();
        String shopOpen= modelShop.getShopOpen();
        String state = modelShop.getState();
        String profileImage= modelShop.getProfileImage();
        String shopName= modelShop.getShopName();

        loadReviews(modelShop, holder);// load avg rating, set to ratingBar

        //set data

        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);

        //check if offline
        if(online.equals("true")) {

            //SHOP OWNER IS ONLINE
            holder.onlineTv.setVisibility(View.VISIBLE);
        }
        else{
            //SHOP OWNER IS OFF
            holder.onlineTv.setVisibility(View.GONE);
        }
//check if shop open
        if (shopOpen.equals("true")){
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        }else {
            //shop closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(holder.shopTv);
        }
        catch (Exception e){
            holder.shopTv.setImageResource(R.drawable.ic_store_grey);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop,final HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        ratingSum = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            float rating = Float.parseFloat(""+ ds.child("ratings").getValue());
                            ratingSum = ratingSum+ rating;//for avg rating, add(additem of) all ratings, later will divide it by number of reviews

                        }


                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgrating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgrating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopsList.size(); //retrun number of records
    }

    //view holder
    class  HolderShop extends RecyclerView.ViewHolder{

        private ImageView shopTv,onlineTv;
        private TextView shopClosedTv,shopNameTv,phoneTv,addressTv;
        private RatingBar ratingBar;


        public HolderShop(@NonNull View itemView) {
            super(itemView);


            //ui views of row_shop
            shopTv=itemView.findViewById(R.id.shopTv);
            onlineTv=itemView.findViewById(R.id.onlineTv);
            shopClosedTv=itemView.findViewById(R.id.shopClosedTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            phoneTv=itemView.findViewById(R.id.phoneTv);
            addressTv=itemView.findViewById(R.id.addressTv);
            ratingBar=itemView.findViewById(R.id.ratingBar);

        }




    }
}
