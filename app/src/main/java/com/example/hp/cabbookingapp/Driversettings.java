package com.example.hp.cabbookingapp;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Driversettings extends AppCompatActivity {

    private EditText driname,driphone,dricar;
    private Button update,exit;
    private String driprename,driprevphone,driprevcar,driuserid;
    private FirebaseAuth mauth;
    private DatabaseReference driverdata;
    private ImageView driprofilepic;
    private Uri finaluri;
    private String profileurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driversettings);
        driname=(EditText)findViewById(R.id.driname);
        driphone=(EditText)findViewById(R.id.driphone);
        dricar=(EditText)findViewById(R.id.dricar);
        update=(Button)findViewById(R.id.driupdate);
        exit=(Button)findViewById(R.id.driprevious);
        driprofilepic=(ImageView)findViewById(R.id.driprofileimage);
        driprofilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte=new Intent(Intent.ACTION_PICK);
                inte.setType("image/*");
                startActivityForResult(inte,1);
            }
        });
        mauth=FirebaseAuth.getInstance();
        driuserid=mauth.getCurrentUser().getUid();
        driverdata=FirebaseDatabase.getInstance().getReference().child("users")
                .child("Drivers").child(driuserid);
        updatecusdetails();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertdateindatabase();
                finish();
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && requestCode == Activity.RESULT_OK)
        {
            final Uri datas=data.getData();
            finaluri=datas;
            driprofilepic.setImageURI(finaluri);
        }
    }

    private void insertdateindatabase() {
        driprename=driname.getText().toString();
        driprevphone=driphone.getText().toString();
        driprevcar=dricar.getText().toString();
        Map map=new HashMap();
        map.put("name",driprename);
        map.put("phone",driprevphone);
        map.put("car",dricar);
        driverdata.updateChildren(map);
        if (finaluri!=null)
        {
            StorageReference storage=FirebaseStorage.getInstance().getReference()
                    .child("profile pictures").child(driuserid);
            Bitmap bitmap=null;
            try {
                bitmap=MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),finaluri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,bytes);
            byte[] dat=bytes.toByteArray();
            UploadTask uploadTask=storage.putBytes(dat);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadurl=taskSnapshot.getUploadSessionUri();
                    Map newimage=new HashMap();
                    newimage.put("profile",downloadurl.toString());
                    driverdata.updateChildren(newimage);
                }
            });
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Driversettings.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            });
        }
        else {
            Toast.makeText(this, "Nothing", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void updatecusdetails() {
        driverdata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null)
                    {
                        driprename=map.get("name").toString();
                        driname.setText(driprename);
                    }
                    if(map.get("phone")!=null)
                    {
                        driprevphone=map.get("phone").toString();
                        driphone.setText(driprevphone);
                    }
                    if(map.get("car")!=null)
                    {
                        driprevcar=map.get("car").toString();
                        dricar.setText(driprevcar);
                    }
                    if (map.get("profile pictures")!=null)
                    {
                        profileurl=map.get("profile pictures").toString();
                        Glide.with(getApplication()).load(profileurl).into(driprofilepic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
