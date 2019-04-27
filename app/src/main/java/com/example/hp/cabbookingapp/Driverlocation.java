package com.example.hp.cabbookingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driverlocation extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.OnConnectionFailedListener
        , LocationListener {
    GoogleApiClient mgoogleapic;
    Location mlastlocation;
    LocationRequest mlocationrequest;
    private Button logout,settings;
    private LinearLayout cusinfo;
    private String customerid="";
    private TextView cusname,cusphone,cusgender,cusdestination;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverlocation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driverlocation.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        else {
            mapFragment.getMapAsync(this);
        }
        settings=(Button)findViewById(R.id.drisettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Driverlocation.this,Driversettings.class);
                startActivity(intent);
                return;
            }
        });

        cusinfo=(LinearLayout)findViewById(R.id.cusinfo);
        cusname=(TextView)findViewById(R.id.customername);
        cusphone=(TextView)findViewById(R.id.customerphone);
        cusgender=(TextView)findViewById(R.id.customergender);
        cusdestination=(TextView)findViewById(R.id.customerdestination);
        logout=(Button)findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(Driverlocation.this,MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
        getassignedcustomer();

    }


    private void getassignedcustomer() {
        String driverid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference alligenref=FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child("Drivers").child(driverid).child("Customer request")
                .child("Customer ride id");
        alligenref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                        customerid=dataSnapshot.getValue().toString();
                        getpickuplocation();
                        getassignedcusinfo();
                }
                else {
                    customerid="";
                    if (assignpickuplocationlistener!=null)
                    {
                        assignpickuplocation.removeEventListener(assignpickuplocationlistener);
                    }
                     if (pickuplocation!=null)
                    {
                        pickuplocation.remove();
                    }
                    cusinfo.setVisibility(View.GONE);
                    cusname.setText("");
                    cusphone.setText("");
                    cusgender.setText("");
                    cusdestination.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getassignedcusinfo() {
        cusinfo.setVisibility(View.VISIBLE);
        DatabaseReference reff=FirebaseDatabase.getInstance().getReference().child("users")
                .child("RIDERS").child(customerid);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null)
                    {
                        cusname.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null)
                    {
                        cusphone.setText(map.get("phone").toString());
                    }
                    if(map.get("gender")!=null)
                    {
                        cusgender.setText(map.get("gender").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private DatabaseReference assignpickuplocation;
    private ValueEventListener assignpickuplocationlistener;
    private Marker pickuplocation;

    private void getpickuplocation() {
        assignpickuplocation=FirebaseDatabase.getInstance().getReference()
                .child("Pick uphere")
                .child(customerid).child("l");
        assignpickuplocationlistener=assignpickuplocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&!customerid.equals(""))
                {
                    List<Object> map=(List<Object>)dataSnapshot.getValue();
                    double lat=0;
                    double lon=0;
                    if (map.get(0)!=null)
                    {
                        lat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null)
                    {
                        lon=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng latLng=new LatLng(lat,lon);
                    pickuplocation=mMap.addMarker(new MarkerOptions().position(latLng).title("pick up location"));

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
            ActivityCompat.requestPermissions(Driverlocation.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        buildgoogleapiclient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildgoogleapiclient()
    {
        mgoogleapic=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleapic.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        mlastlocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refavilable=FirebaseDatabase.getInstance().getReference
                ().child("Driversavailable");
        DatabaseReference refworking=FirebaseDatabase.getInstance().getReference
                ().child("Drivers Working");
        GeoFire geofireavilable=new GeoFire(refavilable);
        GeoFire geofireworkig=new GeoFire(refworking);
        switch (customerid)
        {
            case "":
                geofireworkig.removeLocation(customerid);
                geofireavilable.setLocation(userid, new GeoLocation(location.getLatitude(),
                        location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error==null)
                        {
                            Toast.makeText(Driverlocation.this, "added in database", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(Driverlocation.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
             default:
                 geofireavilable.removeLocation(customerid);
                 geofireworkig.setLocation(userid, new GeoLocation(location.getLatitude(),
                         location.getLongitude()), new GeoFire.CompletionListener() {
                     @Override
                     public void onComplete(String key, DatabaseError error) {
                         if (error==null)
                         {
                             Toast.makeText(Driverlocation.this, "added in database", Toast.LENGTH_SHORT).show();
                         }
                         else {
                             Toast.makeText(Driverlocation.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
                 break;

        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationrequest = new LocationRequest();
        mlocationrequest.setInterval(1000);
        mlocationrequest.setFastestInterval(1000);
        mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driverlocation.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},Location_Requset);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapic, mlocationrequest,
                this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleapic,this);
        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Driversavilable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userid);

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
}
