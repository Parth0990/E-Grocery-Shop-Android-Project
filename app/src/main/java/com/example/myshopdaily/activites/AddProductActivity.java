package com.example.myshopdaily.activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshopdaily.Constants;
import com.example.myshopdaily.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class


AddProductActivity extends AppCompatActivity {


    //ui views
    private ImageView backBtn;
    private ImageView productIconTv;
    private EditText titleEt, descriptionEt;
    private TextView categoryTv, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //IMAGE PICK CONSTANTS

    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //PERMISSION ARRAYS
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picked uri
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconTv = findViewById(R.id.productIconTv);
        descriptionEt=findViewById(R.id.descEt);
        titleEt = findViewById(R.id.titleEt);
        categoryTv = findViewById(R.id.categoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        addProductBtn = findViewById(R.id.addProductBtn);


        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialog

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

//INIT PERMS ARRAYS

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);

                } else {

                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);

                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        productIconTv.setOnClickListener(view -> {
            //show dialog

            showImagePickDialog();
        });
        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick category
                categoryDialog();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {

        //input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        //validate data

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Title is Required", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Category is Required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "price is Required", Toast.LENGTH_SHORT).show();
            return;

        }
        if (discountAvailable) {
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {

            discountPrice = "0";
            discountNote = "";
        }

        addProduct();
    }

    private void addProduct() {
        //add data to db

        progressDialog.setMessage("Adding");
        progressDialog.show();

        final String timestamp =""+System.currentTimeMillis();
        if(image_uri==null){
            ///upload without image
            HashMap<String,Object>hashMap=new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("production",""); //no image sett empty
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+firebaseAuth.getUid());

//add to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else{

            //upload with image
            //first upload image to storage
//name nd path of image to be uploaded


            String filePathAndName ="product_images/"+""+ timestamp;
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadImageUri =uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                //uri of image received
                                HashMap<String,Object>hashMap=new HashMap<>();
                                hashMap.put("productId",""+timestamp);
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("productIcon",""+downloadImageUri); //no image sett empty
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailable);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("uid",""+firebaseAuth.getUid());

//add to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

        }


    }
    private void clearData(){
        //clear data after uploading pdrt

        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountedNoteEt.setText("");
        productIconTv.setImageResource(R.drawable.ic__add_shopping_grey);
        image_uri=null;

    }

    private void categoryDialog() {

        //dialog

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //get picked category
                        String category = Constants.productCategories[which];

                        //set picked category
                        categoryTv.setText(category);
                    }
                }).show();

    }

    private void showImagePickDialog() {

        //option to display in dialog

        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
//handle item clicks
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //permission granted
                                pickFromCamera();
                            }else{
                                //perm not granted
                                requestCameraPermission();
                            }
                        }
                        else {
//gallery clicked
                            if (checkStoragePermission()){
                                //perm granted
                                pickFromGallery();
                            }else{
                                //perm not grant
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }
    private void pickFromGallery(){
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }
    private void pickFromCamera(){

        //using media store to pick high quality image
        ContentValues contentValues =new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent , IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result; //returns
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission,CAMERA_REQUEST_CODE);
    }
    //handle perm
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //both perm grant
                        pickFromCamera();
                    }
                    else{
                        //both or 1 perm deny
                        Toast.makeText(this, "Camera & Storage are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //both perm grant
                        pickFromGallery();
                    }
                    else{
                        //both or 1 perm deny
                        Toast.makeText(this, " Storage permission are required", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //handle image pick results

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode== RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery
                //save picked image uri
                image_uri =data.getData();


                //SET IMAGE
                productIconTv.setImageURI(image_uri);
            }
            else if( requestCode == IMAGE_PICK_CAMERA_CODE){
                //image pick from gallery

                productIconTv.setImageURI(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}