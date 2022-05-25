package com.example.myshopdaily.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Float2;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshopdaily.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView profileTv;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;

    private String shopUid;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        //init views
        backBtn = findViewById(R.id.backBtn);
        profileTv = findViewById(R.id.profileTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        //get shop uid from intent

        shopUid = getIntent().getStringExtra("shopUid");


        firebaseAuth = FirebaseAuth.getInstance();
        //load shop Info: shop name, shop image
        loadShopInfo();
        //if user has written review to this shop, load it
        loadMyReview();

        //go back to previous activity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //input data
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String shopName = ""+ dataSnapshot.child("shopName").getValue();
                String shopImage = ""+ dataSnapshot.child("profileimage").getValue();

                //set shop info to ui
                shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_grey).into(profileTv);
                }
                catch (Exception e){
                    profileTv.setImageResource(R.drawable.ic_store_grey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //my review is available in this shop

                            //get review details
                            String uid = "" + dataSnapshot.child("uid").getValue();
                            String ratings = "" + dataSnapshot.child("ratings").getValue();
                            String review = "" + dataSnapshot.child("review").getValue();
                            String timestamp = "" + dataSnapshot.child("timestamp").getValue();

                            //set review details to our ui
                            final float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();

        //for time of review
        String timestamp = ""+ System.currentTimeMillis();

        //setup date in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+ firebaseAuth.getUid());
        hashMap.put("ratings", ""+ ratings);
        hashMap.put("review", ""+ review);
        hashMap.put("timestamp", ""+ timestamp);

        //put to db: DB > Users > ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review Published Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding review to db
                        Toast.makeText(WriteReviewActivity.this, ""+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}