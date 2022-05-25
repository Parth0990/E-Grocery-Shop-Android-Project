package com.example.myshopdaily.activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshopdaily.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    private EditText nameEt, phoneEt, countryEt, StateEt, cityEt, addressEt, emailEt, passwordEt, cPasswordEt;
    private ImageView profileTv;
    private ImageButton backBtn, gpsBtn;


    private Uri image_uri;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int CAMERA_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission array
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private double latitude, longitude;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //init Ui design
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileTv = findViewById(R.id.profileTv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        StateEt = findViewById(R.id.StateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cPasswordEt);
        Button registerBtn = findViewById(R.id.registerBtn);
        TextView registerSellerTv = findViewById(R.id.registerSellerTv);


        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog =new ProgressDialog((this));
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(view -> onBackPressed());
        gpsBtn.setOnClickListener(view -> {
            //detect current location
            if (checkLocationPermission()) {
                //already allowed
                detectLocation();
            } else {
                //not allow
                requestLocationPermission();
            }
        });

        profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image
                showImagePickDialog();
            }
        });

        registerBtn.setOnClickListener(view -> {
            //register user
            inputData();
        });


        registerSellerTv.setOnClickListener(view -> {
            //open seller register activity
            startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class));

        });
    }

    private String fullName;
    private String phoneNumber;
    private String country;
    private String State;
    private String city;
    private String address;
    private String email;
    private String password;

    private void inputData() {
        //input data
        fullName=nameEt.getText().toString().trim();
        phoneNumber=phoneEt.getText().toString().trim();
        country=countryEt.getText().toString().trim();
        State=StateEt.getText().toString().trim();
        city=cityEt.getText().toString().trim();
        address=addressEt.getText().toString().trim();
        email=emailEt.getText().toString().trim();
        password=passwordEt.getText().toString().trim();
        String cPassword = cPasswordEt.getText().toString().trim();
        //validate data
        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this,"Enter Name",Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this,"Enter Phone Number",Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude== 0.0 || longitude==0.0){
            Toast.makeText(this,"Please click GPS button to detect location",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email Pattern",Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){
            Toast.makeText(this,"Password must be at least 6 character long ",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(cPassword)){
            Toast.makeText(this,"Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }



    private void createAccount(){
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password).
                addOnSuccessListener(authResult -> {
                    //account created
                    saverFirebaseData();
                })
                .addOnFailureListener(e -> {
                    //failed creating account
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info..");

        String timestamp=""+System.currentTimeMillis();


        if(image_uri==null){
            //save data without image

            //setup date to save
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("country",""+country);
            hashMap.put("state",""+State);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("profileImage","");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //db updated
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        //failed updating db
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                        finish();
                    });
        }else {
            //save info with page
            String filePathName = "profiles_images/"+""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // get url of upload image
                        Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadImageUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            //setup date to save
                            HashMap<String,Object>hashMap=new HashMap<>();
                            hashMap.put("uid",""+firebaseAuth.getUid());
                            hashMap.put("email",""+email);
                            hashMap.put("name",""+fullName);
                            hashMap.put("phone",""+phoneNumber);
                            hashMap.put("country",""+country);
                            hashMap.put("state",""+State);
                            hashMap.put("city",""+city);
                            hashMap.put("address",""+address);
                            hashMap.put("latitude",""+latitude);
                            hashMap.put("longitude",""+longitude);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("accountType","User");
                            hashMap.put("online","true");
                            hashMap.put("profileImage", ""+downloadImageUri);//url of upload image

                            //save to db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                                    .addOnSuccessListener(unused -> {
                                        //db updated
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        //failed updating db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                                        finish();
                                    });

                        }

                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    });

        }
    }
    private void showImagePickDialog() {
        //options to display
        String[] options = {"Camera", "Gallery"};
        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //handle click
                        if (which == 0){
                            //Camera click
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();
                            }
                            else {
                                //not allowed
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery click
                            if (checkStoragePermission()){
                                //storage permission allowed
                                pickFromGallery();
                            }
                            else {
                                //not allowed, request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE );
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        System.out.println("Results : ====================" + cameraPermissions);
        System.out.println("Results : ====================" + cameraPermissions.length);
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void detectLocation() {
        Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                     int[] grantResults),
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//         return;
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  1000 * 60 * 2, 10, (LocationListener) this);
            System.out.println("Location click" + locationManager);
        } catch (Exception e){
            System.out.println("Location Error" + locationManager);
        }
    }
    private void findAddress() {
//find country address,sate,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses= geocoder.getFromLocation(latitude, longitude,1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String State = addresses.get(0).getAdminArea();
            String country =addresses.get(0).getCountryName();

            //set addresses

            countryEt.setText(country);
            StateEt.setText(State);
            cityEt.setText(city);
            addressEt.setText(address);

        } catch (Exception e) {
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }


    }
    private boolean checkLocationPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                ( PackageManager.PERMISSION_GRANTED);

    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }





    @Override
    public void onLocationChanged( Location location) {
        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {
        //gps/location disabled

        Toast.makeText(this,"Turn On Location" ,Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        detectLocation();
                        //permission granted
                    }
                    else{
                        Toast.makeText(this,"Location permission required", Toast.LENGTH_SHORT).show();
                        //perm denied
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                        //permission granted
                    }
                    else{
                        Toast.makeText(this,"Camera permissions are required", Toast.LENGTH_SHORT).show();
                        //perm denied
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                        //permission granted
                    }
                    else{
                        Toast.makeText(this,"Storage permissions are required", Toast.LENGTH_SHORT).show();
                        //perm denied
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){

                //get picked image
                image_uri = data.getData();
                //set to imageview
                profileTv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){

                //set to imageview
                profileTv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}