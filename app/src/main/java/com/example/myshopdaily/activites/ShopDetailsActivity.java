package com.example.myshopdaily.activites;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myshopdaily.Constants;
import com.example.myshopdaily.R;
import com.example.myshopdaily.adapters.AdapterCartItem;
import com.example.myshopdaily.adapters.AdapterProductUser;
import com.example.myshopdaily.models.ModelCartItem;
import com.example.myshopdaily.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {


    public String deliveryFee;
    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv, dfeeTv, allTotalPriceTv;
    // dclare ui views
    private ImageView shopTv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv,
            addressTv, filteredProductTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn, reviewBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;
    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    private FirebaseAuth firebaseAuth;
    //progress Dialog
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;
    private EasyDB easyDB;
    private float ratingSum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopTv = findViewById(R.id.shopTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductTv = findViewById(R.id.filteredProductTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);


        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of shop from intent

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();//avg rating, set on ratingbar

        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", "text", "unique"))
                .addColumn(new Column("Item_PId", "text", "not null"))
                .addColumn(new Column("Item_Name", "text", "not null"))
                .addColumn(new Column("Item_Price_Each", "text", "not null"))
                .addColumn(new Column("Item_Price", "text", "not null"))
                .addColumn(new Column("Item_Quantity", "text", "not null"))
                .doneTableColumn();

        //each shop have its own products and orders s if user add items to cart and go back and open cart in different shop then cart should be differnet
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();


        //search

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go previous activity
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show cart dialog
                showCartDialog();

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Filter Products")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadShopProducts();
                                } else {
                                    //load filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        //handle reviewBtn click, open reviews activity
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        ratingSum = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;//for avg rating, add(additem of) all ratings, later will divide it by number of reviews

                        }


                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgrating = ratingSum / numberOfReviews;

                        ratingBar.setRating(avgrating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
//         easyDB= EasyDB.init(this, "ITEMS_DB")
//                .setTableName("ITEMS_TABLE")
//                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
//                .addColumn(new Column("Item_PId", new String[]{"text", "not null"}))
//                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
//                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
//                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
//                .doneTableColumn();

        easyDB.deleteAllDataFromTable(); //delete all records from cart
    }

    public void cartCount() {
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();

        if (count <= 0) {
            //no item in cart count textview
            cartCountTv.setVisibility(View.GONE);
        } else {
            //have items in cart, show cart count textview and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);//concatenate with string, because we can't set interger in textview
        }
    }

    private void showCartDialog() {

        //init list
        cartItemList = new ArrayList<>();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        // init view
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dfeeTv = view.findViewById(R.id.dfeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", "text", "unique"))
                .addColumn(new Column("Item_PId", "text", "not null"))
                .addColumn(new Column("Item_Name", "text", "not null"))
                .addColumn(new Column("Item_Price_Each", "text", "not null"))
                .addColumn(new Column("Item_Price", "text", "not null"))
                .addColumn(new Column("Item_Quantity", "text", "not null"))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem("" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );

            cartItemList.add(modelCartItem);

        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);

        //set to recycleview
        cartItemsRv.setAdapter(adapterCartItem);

        dfeeTv.setText("₹" + deliveryFee);
        sTotalTv.setText("₹" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("₹" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("₹", ""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(view1 -> {
            //first validate delivery address
            if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                //user didn't enter address in profile
                Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                return; //don't procede further
            } else if (myPhone.equals("") || myPhone.equals("null")) {
                //user didn't enter phone number in profile
                Toast.makeText(ShopDetailsActivity.this, "Please enter your Phone Number in your profile before placing order...", Toast.LENGTH_SHORT).show();
                return; //don't procede further
            }
            if (cartItemList.size() == 0) {
                // cart list is empty
                Toast.makeText(ShopDetailsActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                return; // don't procede further
            }
            submitOrder();
        });
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        final String timestamp = "" + System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("₹", "");//remove $ if contains

        //add latitude, longtitude of user to each order | delete previous orders from firebase or add manually to them

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);
        hashMap.put("deliveryFee", "" + deliveryFee);


        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // order info added now add order items
                        for (int i = 0; i < cartItemList.size(); i++) {
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void openMap() {

        //saddr= source address
        //daddr=destinaiton address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();

    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        for (DataSnapshot ds : datasnapshot.getChildren()) {

                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseerror) {

                    }
                });
    }


    private void loadShopDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                String name = "" + datasnapshot.child("name").getValue();
                shopName = "" + datasnapshot.child("shopName").getValue();
                shopEmail = "" + datasnapshot.child("email").getValue();
                shopPhone = "" + datasnapshot.child("phone").getValue();
                shopLatitude = "" + datasnapshot.child("latitude").getValue();
                shopAddress = "" + datasnapshot.child("address").getValue();
                shopLongitude = "" + datasnapshot.child("longitude").getValue();
                deliveryFee = "" + datasnapshot.child("deliveryFee").getValue();
                String profileImage = "" + datasnapshot.child("profileImage").getValue();
                String shopOpen = "" + datasnapshot.child("shopOpen").getValue();


                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: ₹" + deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopTv);

                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {

            }
        });
    }

    private void loadShopProducts() {
        //init list
        productsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for (DataSnapshot ds : datasnapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseerror) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId) {
        //when user place order, send notification to seller

        //prepare date for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;//must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order" + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order...";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationJo.put("date", notificationBodyJo);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, response -> {
            //after sending fcm start order details activity
            //after placing order open details page
            //open oreder details, we need to keys there, orderID, orderTo
            Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderTo", shopUid);
            intent.putExtra("orderId", orderId);
            startActivity(intent);// no get these values through intent on OrderDetailsUsersActivity


        }, error -> {
            //if failed sending fcm, still start order details activity
            Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderTo", shopUid);
            intent.putExtra("orderId", orderId);
            startActivity(intent);// no get these values through intent on OrderDetailsUsersActivity

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return super.getHeaders();
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}