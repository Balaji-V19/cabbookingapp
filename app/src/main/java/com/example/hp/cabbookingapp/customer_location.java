package com.example.hp.cabbookingapp;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class customer_location extends FragmentActivity implements OnMapReadyCallback
,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,
        LocationListener {
     private Button logout,request,setting;
     GoogleApiClient mgoogleclient;
     Location mlastlocation;
     LatLng pickuplocation;
     LocationRequest mlocationrequset;
     private boolean requestforride=false;
     private Marker pickupmarker;
     private String destination;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(customer_location.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        logout=(Button)findViewById(R.id.cuslogout);
        request=(Button)findViewById(R.id.request);
        setting=(Button)findViewById(R.id.cussetting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(customer_location.this,CustomersettingActivity.class);
                startActivity(i);
            }
        });




     PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
        getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination=place.getName().toString();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(customer_location.this, ""+status.getStatus(), Toast.LENGTH_SHORT).show();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(customer_location.this,MainActivity.class);
                startActivity(i);
            }
        });
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestforride)
                {
                    requestforride=false;
                    geoQuery.removeAllListeners();
                    driverref.removeEventListener(driverreflistner);
                    String user=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Pick uphere");
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.removeLocation(user);
                    if (driverid!=null)
                    {
                        DatabaseReference refer=FirebaseDatabase.getInstance().getReference()
                                .child("users").child("Drivers").child(driverid);
                        refer.setValue(true);
                        driverid=null;
                    }
                    request.setText("Request");
                    driver=false;
                    radius=1;
                    if (pickupmarker!=null)
                    {
                        pickupmarker.remove();
                    }
                }
                else {
                    requestforride=true;
                    String user=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Pick uphere");
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(user, new GeoLocation(mlastlocation.getLatitude(),
                            mlastlocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error!=null)
                            {
                                Toast.makeText(customer_location.this,
                                        ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                pickuplocation=new LatLng(mlastlocation.getLatitude(),
                                        mlastlocation.getLongitude());
                               pickupmarker= mMap.addMarker(new MarkerOptions().position
                                       (pickuplocation).title("pick up here"));
                                request.setText("getting your driver....");
                                gettingdriver();
                            }
                        }
                    });
                }
            }
        });
    }
    private int radius=1;
    private boolean driver=false;
    public String driverid;
    GeoQuery geoQuery;

    private void gettingdriver() {
        DatabaseReference reference=FirebaseDatabase.getInstance()
                .getReference().child("Driversavailable");
        GeoFire geoFire=new GeoFire(reference);
        geoQuery=geoFire.queryAtLocation(new GeoLocation
                (pickuplocation.latitude,pickuplocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driver && requestforride)
                {
                    driver=true;
                    driverid=key;
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference()
                            .child("users").child("Drivers").child(driverid)
                            .child("Customer request");
                    String cusid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("Customer ride id",cusid);
                    map.put("destination",destination);
                    ref.updateChildren(map);
                    request.setText("getting driver location");
                    driverlocation();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driver)
                {
                    radius++;
                    gettingdriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker drivermarker;
    DatabaseReference driverref;
    private ValueEventListener driverreflistner;

    private void driverlocation() {
        driverref=FirebaseDatabase.getInstance().getReference()
                .child("Drivers Working").child(driverid).child("l");
        driverreflistner=driverref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestforride)
                {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double driverlocationlat=0;
                    double driverlocationlon=0;
                    if (map.get(0)!=null)
                    {
                        driverlocationlat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null)
                    {
                        driverlocationlon=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng latLng=new LatLng(driverlocationlat,driverlocationlon);
                    if (drivermarker!=null)
                    {
                        drivermarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickuplocation.latitude);
                    loc1.setLongitude(pickuplocation.longitude);
                    Location loc2=new Location("");
                    loc2.setLatitude(latLng.latitude);
                    loc2.setLongitude(latLng.longitude);
                    float distance=loc1.distanceTo(loc2);
                    if (distance<100)
                    {
                        request.setText("Driver is here");
                    }
                    else {
                        request.setText("Distance"+String.
                                valueOf(distance));
                    }

                    drivermarker=mMap.addMarker(new MarkerOptions().position(latLng)
                            .title("your driver").icon(BitmapDescriptorFactory.fromResource(
                                    R.mipmap.ic_car
                            )));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(customer_location.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        Buildgoogleapi();
        mMap.setMyLocationEnabled(true);

    }
    protected synchronized void Buildgoogleapi()
    {
        mgoogleclient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleclient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationrequset=new LocationRequest();
        mlocationrequset.setInterval(1000);
        mlocationrequset.setFastestInterval(1000);
        mlocationrequset.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(customer_location.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleclient,
                mlocationrequset,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));


    }

    final int Location_Requset=1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case Location_Requset:
                if (grantResults.length>0 && grantResults[0]==PackageManager.
                        PERMISSION_GRANTED)
                {
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(this, "please give the permission", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
