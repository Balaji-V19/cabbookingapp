package com.example.hp.cabbookingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class Driverlogin extends AppCompatActivity {
    private FirebaseAuth mauth;
    private FirebaseAuth.AuthStateListener Authstatelistner;
    private Button driverlogin,driverregister;
    private EditText email,password;
    RelativeLayout rootlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverlogin);
        mauth=FirebaseAuth.getInstance();
        driverlogin=(Button)findViewById(R.id.driverlogin);
        driverregister=(Button)findViewById(R.id.driverregister);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        Authstatelistner=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null)
                {
                    Intent i=new Intent(Driverlogin.this,Driverlocation.class);
                    startActivity(i);
                    return;
                }
            }
        };

        driverlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SpotsDialog wait=new SpotsDialog(Driverlogin.this);
                wait.show();
                mauth.signInWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString()).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful())
                                {
                                    wait.dismiss();
                                    Toast.makeText(Driverlogin.this,
                                            "Sign in Error", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    wait.dismiss();
                                    Intent i=new Intent(Driverlogin.this,Driverlocation.class);
                                    startActivity(i);

                                }
                            }
                        }
                );
            }
        });
        driverregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SpotsDialog wait=new SpotsDialog(Driverlogin.this);
                wait.show();
                mauth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                wait.dismiss();
                                String userid=mauth.getCurrentUser().getUid();
                                DatabaseReference db=FirebaseDatabase.getInstance()
                                        .getReference().child("users").child("Drivers").child(userid);
                                db.setValue(email.getText().toString());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        wait.dismiss();
                        Toast.makeText(Driverlogin.this, "failed"+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                    }
                });
            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        mauth.addAuthStateListener(Authstatelistner);
    }

    @Override
    public void onStop() {
        super.onStop();
        mauth.removeAuthStateListener(Authstatelistner);
    }
}