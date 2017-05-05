package com.graphhopper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.lang.Math.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class Collection extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    Button myB;
    Button myB2;
    String uid;
    DatabaseReference listLoc;
    private LocationManager locationManager;
    private String latitude = "0";
    private String longitude = "0";
    private String preLatitude;
    private String preLongitude;
    private String speed;
    private int background = 0;
    private CountDownTimer count;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Location lastKnownLocation;
    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        myB = (Button) findViewById(R.id.my_b);
        myB.setText("Stop Tracking");
        myB2 = (Button) findViewById(R.id.button2);
        myB2.setText("Run in Background");

        // Set button response
        setListeners();

        // Load in unique identifier if exists, create if not
        uid = "invalid";
        SharedPreferences sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = sp.getString("uid", uid);

        if (uid.equals("invalid")) {
            uid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("uid", uid);
            editor.commit();
        }

        // Setup database to save data
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Data");
        listLoc = myRef.child("User:" + uid);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        // Loop tracking
        count = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(Collection.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if (lastKnownLocation != null) {
                            preLatitude = latitude;
                            preLongitude = longitude;
                            latitude = String.valueOf(lastKnownLocation.getLatitude());
                            longitude = String.valueOf(lastKnownLocation.getLongitude());
                        }
                    }
                }
                double prelat = Double.parseDouble(preLatitude);
                double prelong = Double.parseDouble(preLongitude);
                double lat = Double.parseDouble(latitude);
                double longi = Double.parseDouble(longitude);
                double dlat = toRadians(lat - prelat);
                double dlon = toRadians(longi - prelong);
                prelat = toRadians(prelat);
                lat = toRadians(lat);
                double a = sin(dlat/2) * sin(dlat/2) +
                        sin(dlon/2) * sin(dlon/2) * cos(prelat) * cos(lat);
                double c = 2 * atan2(sqrt(a), sqrt(1-a));
                double dist = 6371 * c;
                speed = Double.toString(dist / 5000);
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                if (currentDateTimeString.split(" ").length > 4) {
                    currentDateTimeString = currentDateTimeString.split(" ")[3] + " " + currentDateTimeString.split(" ")[4];
                }
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = date.format(cal.getTime());
                DatabaseReference dateEntry = listLoc.child(formattedDate);
                DatabaseReference newListLoc = dateEntry.push();
                newListLoc.setValue(currentDateTimeString + " " + latitude + " " + longitude + " " + speed);
                this.start();
            }
        };

        count.start();
    }

    private void setListeners() {
        myB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.cancel();
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = date.format(cal.getTime());
                DatabaseReference dateEntry = listLoc.child(formattedDate);
                DatabaseReference newListLoc = dateEntry.push();
                newListLoc.setValue("end");
                setContentView(R.layout.activity_main);
                Intent intent = new Intent(Collection.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        myB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                background = 1;
                moveTaskToBack(true);
            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // location-related task.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        // Setup Location Manager to find GPS coordinates
                        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if (lastKnownLocation != null) {
                        }
                    }

                } else {

                    // permission denied
                }
                return;
            }

        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(2500);
                mLocationRequest.setFastestInterval(2000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);

                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (lastKnownLocation != null) {
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if (background == 0) {
            count.cancel();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = date.format(cal.getTime());
            DatabaseReference dateEntry = listLoc.child(formattedDate);
            DatabaseReference newListLoc = dateEntry.push();
            newListLoc.setValue("end");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (lastKnownLocation != null) {
                }
            }
        }
    }
}
