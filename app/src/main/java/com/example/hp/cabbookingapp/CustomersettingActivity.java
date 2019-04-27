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

public class CustomersettingActivity extends AppCompatActivity {
    private EditText name,phone,gender;
    private Button update,exit;
    private String prename,prevphone,prevgender,userid;
    private FirebaseAuth mauth;
    private DatabaseReference ref;
    private ImageView profilepic;
    private Uri finaluri;
    private String profileurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customersetting);
        name=(EditText)findViewById(R.id.Cusname);
        phone=(EditText)findViewById(R.id.Cusphone);
        gender=(EditText)findViewById(R.id.CusGender);
        update=(Button)findViewById(R.id.cusupdate);
        exit=(Button)findViewById(R.id.cusprevious);
        profilepic=(ImageView)findViewById(R.id.profileimage);
        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte=new Intent(Intent.ACTION_PICK);
                inte.setType("image/*");
                startActivityForResult(inte,1);
            }
        });
        mauth=FirebaseAuth.getInstance();
        userid=mauth.getCurrentUser().getUid();
        ref=FirebaseDatabase.getInstance().getReference().child("users")
                .child("RIDERS").child(userid);
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
            profilepic.setImageURI(finaluri);
        }
    }

    private void insertdateindatabase() {
        prename=name.getText().toString();
        prevphone=phone.getText().toString();
        prevgender=gender.getText().toString();
        Map map=new HashMap();
        map.put("name",prename);
        map.put("phone",prevphone);
        map.put("gender",prevgender);
        ref.updateChildren(map);
        if (finaluri!=null)
        {
            StorageReference storage=FirebaseStorage.getInstance().getReference()
                    .child("profile pictures").child(userid);
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
                    ref.updateChildren(newimage);
                }
            });
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CustomersettingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null)
                    {
                        prename=map.get("name").toString();
                        name.setText(prename);
                    }
                    if(map.get("phone")!=null)
                    {
                        prevphone=map.get("phone").toString();
                        phone.setText(prevphone);
                    }
                    if(map.get("gender")!=null)
                    {
                        prevgender=map.get("gender").toString();
                        gender.setText(prevgender);
                    }
                    if (map.get("profile pictures")!=null)
                    {
                        profileurl=map.get("profile pictures").toString();
                        Glide.with(getApplication()).load(profileurl).into(profilepic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
