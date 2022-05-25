package com.example.myshopdaily.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshopdaily.adapters.AdapterOrderShop;
import com.example.myshopdaily.adapters.AdapterProductSeller;
import com.example.myshopdaily.Constants;
import com.example.myshopdaily.models.ModelOrderShop;
import com.example.myshopdaily.models.ModelProduct;
import com.example.myshopdaily.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTv,shopNameTv,emailTv,tabProductsTv,tabOrdersTv,filteredProductTv, filteredOrdersTv;
    private ImageView profileTv;
    private RelativeLayout productsRl,ordersRl;
    private RecyclerView productsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv =findViewById(R.id.nameTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        emailTv=findViewById(R.id.emailTv);
        tabProductsTv=findViewById(R.id.tabProductsTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        filteredProductTv=findViewById(R.id.filteredProductTv);
        EditText searchProductEt = findViewById(R.id.searchProductEt);
        ImageButton logoutBtn = findViewById(R.id.LogoutBtn);
        ImageButton editProfileBtn = findViewById(R.id.editProfileBtn);
        ImageButton addProductBtn = findViewById(R.id.addProductBtn);
        ImageButton filterProductBtn = findViewById(R.id.filterProductBtn);
        profileTv = findViewById(R.id.profileTv);
        productsRl=findViewById(R.id.productsRl);
        ordersRl=findViewById(R.id.ordersRl);
        productsRv=findViewById(R.id.productsRv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        ImageButton filterOrderBtn = findViewById(R.id.filterOrdersBtn);
        ordersRv = findViewById(R.id.ordersRv);
        ImageButton reviewsBtn = findViewById(R.id.reviewsBtn);
        ImageButton settingsBtn = findViewById(R.id.settingsBtn);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth= FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        //search

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logoutBtn.setOnClickListener(view -> {
            //make offline
            //sign out
            //go to login activity

            makeMeOffline();

        });

        editProfileBtn.setOnClickListener(view -> {
            //open edit profile
            startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
        });

        addProductBtn.setOnClickListener(view -> {
            //open add product activity
            startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));

        });

        tabProductsTv.setOnClickListener(view -> {
//load products
            showProductsUI();
        });

        tabOrdersTv.setOnClickListener(view -> {
//load order
            showOrdersUI();
        });
        filterProductBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Choose Category")
                    .setItems(Constants.productCategories1, (dialogInterface, which) -> {
                        //get selected item
                        String selected = Constants.productCategories1[which];
                        filteredProductTv.setText(selected);
                        if (selected.equals("All")){
                            //load all
                            loadAllProducts();
                        }
                        else{
                            //load filtered
                            loadFilteredProducts(selected);
                        }
                    })
                    .show();
        });

        filterOrderBtn.setOnClickListener(view -> {
            //option to display in dialog
            String[] options = {"All", "In Progress", "Completed", "Cancelled"};
            //dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Filter Orders:")
                    .setItems(options, (dialog, which) -> {
                        //handle item clicks
                        if (which == 0){
                            //All clicked
                            filteredOrdersTv.setText("Showing All Orders");
                            adapterOrderShop.getFilter().filter("");//show all orders
                        }
                        else {
                            String optionClicked = options[which];
                            filteredOrdersTv.setText("Showing"+ optionClicked+ " Orders");
                            adapterOrderShop.getFilter().filter(optionClicked);
                        }

                    })
                    .show();
        });

        reviewsBtn.setOnClickListener(view -> {
            //open same reviews activity as used in user main page.
            Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
            intent.putExtra("shopUid", ""+firebaseAuth.getUid());
            startActivity(intent);
        });

        //start settings screen
        settingsBtn.setOnClickListener(view -> startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class)));

    }

    private void loadAllOrders() {
        //init array list
        orderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding new data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        //set adapter to recycleview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void loadFilteredProducts(String selected) {

        productList = new ArrayList<>();
        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        //before getting reset List
                        productList.clear();
                        for(DataSnapshot ds: datasnapshot.getChildren()){

                            String productCategory =""+ds.child("productCategory").getValue();
                            // if selected category matches product category then add in list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        //setup adapter
                        adapterProductSeller= new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadAllProducts() {

        productList = new ArrayList<>();
        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        //before getting reset List
                        productList.clear();
                        for(DataSnapshot ds: datasnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller= new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void showProductsUI() {

        //show product ui and hide other ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorAccent));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        //show order ui and hide other ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);



        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorAccent));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

    }




    private void makeMeOffline() {
        //after logging in ,make user online
        progressDialog.setMessage("Logging Out..");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online", "false");
        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    //updated success
                    firebaseAuth.signOut();
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    // failed updating
                    progressDialog.dismiss();
                    Toast.makeText(MainSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user  == null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();

        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data from db
                            String name=""+ds.child("name").getValue();
//                            String accountType = ""+ds.child("accountType").getValue();
                            String email =""+ds.child("email").getValue();
                            String shopName=""+ds.child("shopName").getValue();
                            String ProfileImage = ""+ds.child("profileImage").getValue();

                            //set data to ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                            try {
                                Picasso.get().load(ProfileImage).placeholder(R.drawable.ic_store_grey).into(profileTv);


                            }catch (Exception e){
                                profileTv.setImageResource(R.drawable.ic_store_grey);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseerror) {

                    }
                });
    }
}