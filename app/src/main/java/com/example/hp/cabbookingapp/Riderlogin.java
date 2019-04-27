package com.example.hp.cabbookingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Riderlogin extends AppCompatActivity {
    private FirebaseAuth mauth;
    private FirebaseAuth.AuthStateListener Authstatelistner;
    EditText email,password;
    Button login,register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riderlogin);
        login=(Button)findViewById(R.id.riderlogin);
        register=(Button)findViewById(R.id.riderregister);
        email=(EditText)findViewById(R.id.rideremail);
        password=(EditText)findViewById(R.id.riderpassword);
        mauth=FirebaseAuth.getInstance();
        Authstatelistner=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null)
                {
                    Intent i=new Intent(Riderlogin.this,customer_location.class);
                    startActivity(i);
                    return;
                }
            }
        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SpotsDialog wait=new SpotsDialog(Riderlogin.this);
                wait.show();
                mauth.signInWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString()).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful())
                                {
                                    wait.dismiss();
                                    Toast.makeText(Riderlogin.this,
                                            "Sign in Error", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    wait.dismiss();
                                    Intent i=new Intent(Riderlogin.this,customer_location.class);
                                    startActivity(i);

                                }
                            }
                        }
                );

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SpotsDialog wait=new SpotsDialog(Riderlogin.this);
                wait.show();
                mauth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                wait.dismiss();
                                String userid=mauth.getCurrentUser().getUid();
                                DatabaseReference db=FirebaseDatabase.getInstance()
                                        .getReference().child("users").child("RIDERS").child(userid);
                                db.setValue(email.getText().toString());


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        wait.dismiss();
                        Toast.makeText(Riderlogin.this, "failed"+e.getMessage()
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
