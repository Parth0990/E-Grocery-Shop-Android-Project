package com.example.myshopdaily.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshopdaily.R;
import com.example.myshopdaily.adapters.AdapterOrderUser;
import com.example.myshopdaily.adapters.AdapterShop;
import com.example.myshopdaily.models.ModelOrderUser;
import com.example.myshopdaily.models.ModelShop;
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

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv,emailTv,phoneTv, tabShopsTv,tabOrdersTv;
    private RelativeLayout shopRl,ordersRl;
    private ImageButton logoutBtn ,editProfileBtn, settingsBtn;
    private ImageView profileTv;
    private RecyclerView shopsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv =findViewById(R.id.nameTv);
        emailTv =findViewById(R.id.emailTv);
        phoneTv =findViewById(R.id.phoneTv);
        tabShopsTv =findViewById(R.id.tabShopsTv);
        tabOrdersTv =findViewById(R.id.tabOrdersTv);
        shopRl =findViewById(R.id.shopRl);
        ordersRl =findViewById(R.id.ordersRl);
        logoutBtn = findViewById(R.id.LogoutBtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        profileTv=findViewById(R.id.profileTv);
        shopsRv=findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        settingsBtn = findViewById(R.id.settingsBtn);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth=firebaseAuth.getInstance();
        checkUser();

        //at start show shop
        showShopsUI();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make offline
                //sign out
                //go to login activity

                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));


            }
        });

        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show shops
                showShopsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show orders
                showOrdersUI();
            }
        });

        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });

    }

    private void showShopsUI() {
        //show shops ui,hide orders ui
        shopRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        //show shops ui,hide orders ui
        shopRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

    }

    private void makeMeOffline(){

        //after logging in ,make user online
        progressDialog.setMessage("Logging Out..");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online", "false");
        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated success
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkUser() {
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user  == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
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

                            //get user data
                            String name=""+ds.child("name").getValue();
                            String email=""+ds.child("email").getValue();
                            String phone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city=""+ds.child("city").getValue();

//set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_grey).into(profileTv);
                            }
                            catch (Exception e){
                                profileTv.setImageResource(R.drawable.ic_person_grey);

                            }

                            //load only shops that are in city of user
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseerror) {

                    }
                });
    }

    private void loadOrders() {

        //init order list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid = ""+ ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        //set to recycleview
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String myCity) {

        //init list

        shopsList= new ArrayList<>();
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        //clearing list before adding
                        shopsList.clear();
                        for (DataSnapshot ds:datasnapshot.getChildren()){
                            ModelShop modelShop=ds.getValue(ModelShop.class);

                            String shopCity =""+ds.child("city").getValue();
                            //show only user city shops
                            if (shopCity.equals( myCity )){
                                shopsList.add(modelShop);
                            }

                            //if u want to display all shops  , skips the if statement and add this

                            //shopsList.add(modelShop);

                        }
                        //setup adapter
                        adapterShop= new AdapterShop(MainUserActivity.this,shopsList);
                        //set adapter to recyclerview

                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseerror) {

                    }
                });
    }
}
