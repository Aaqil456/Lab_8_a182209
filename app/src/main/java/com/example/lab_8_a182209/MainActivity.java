package com.example.lab_8_a182209;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
  TextView tv_location;
  FusedLocationProviderClient mFusedLocationClient;
  LocationRequest mLocationRequest;
  LocationCallback mLocationCallback;
  double latitude,longitude;
  LocationSettingsRequest.Builder mLocationSettingsBuilder;
    SettingsClient client;
    Task<LocationSettingsResponse> task;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    Button btnPlay, btnStop;
    ImageView imgLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnStop=findViewById(R.id.btnStop);
        btnPlay=findViewById(R.id.btnPlay);
        imgLocation=findViewById(R.id.imgLocation);

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Location finding stopped", Toast.LENGTH_SHORT).show();
                onPause();
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Continue location finding", Toast.LENGTH_SHORT).show();

                onResume();
            }
        });

        tv_location=findViewById(R.id.tv_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null) {

                    return;
                }
                else {
                    latitude=locationResult.getLastLocation().getLatitude();
                    longitude=locationResult.getLastLocation().getLongitude();
                    tv_location.append("\n" + "Latitude: " + locationResult.getLastLocation().getLatitude()
                            + " Longitude: " + locationResult.getLastLocation().getLongitude());
                }
            }
        };

        setLocationRequestSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startLocationUpdate();
        imgLocation.setBackgroundResource(R.drawable.ic_location_on);
        requestLocationUpdate();
    }
    @Override
    protected void onPause() {
        super.onPause();

        if(mFusedLocationClient!=null) {
            imgLocation.setBackgroundResource(R.drawable.ic_baseline_location_off_24);

            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
           // Toast.makeText(MainActivity.this, "Listener is removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationUpdate(){
        mLocationSettingsBuilder= new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        client = LocationServices.getSettingsClient(MainActivity.this);
        task = client.checkLocationSettings(mLocationSettingsBuilder.build());

        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdate();
            }
        }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException) {
                    try {

                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);

                    }catch (IntentSender.SendIntentException sendIntentException) {

                    }
                }
            }
        });


    }

    private  void setLocationRequestSetting() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //HIGH_ACCURACE - gps (most precise n high consume battery)
        //LOW_POWER - Cell tower
        //BALANCED_POWER_ACCURACY = wifi/cell tower
        //NO_POWER - locations triggered by other app
    }

    private void startLocationUpdate(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                    showExplaination();


            }else{
                ActivityCompat.requestPermissions(MainActivity.this
                ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
            }

        }else{
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
            //Toast.makeText(MainActivity.this, "Location permission was granted", Toast.LENGTH_SHORT).show();


        }

    }
    private void showExplaination() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Requires Location Permission")
                .setMessage("This app needs location permission to work")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       // Toast.makeText(MainActivity.thiS, "Sorry, this function cannot work until permission is granted", Toast.LENGTH_SHORT).show();

                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "Location setting has turned on", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "Location setting has turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
        }
    }
}