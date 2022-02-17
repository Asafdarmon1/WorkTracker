package com.example.worktracker.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worktracker.MainActivity;
import com.example.worktracker.R;
import com.example.worktracker.Splash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText lEmail,lPassword;
    Button loginNow;
    TextView forgetPassword,createAccount;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //enable back button and set title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login To WorkTracker");


        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);
        loginNow = findViewById(R.id.loginBtn);
        forgetPassword = findViewById(R.id.forgotPasword);
        createAccount = findViewById(R.id.createAccount);

        progressBar = findViewById(R.id.lProgressBar);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //display warning if user is logging to and account;
        showWarning();

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the user input
                String mEmail = lEmail.getText().toString();
                String mPassword = lPassword.getText().toString();

                //check if empty
                if(mPassword.isEmpty() || mEmail.isEmpty()){
                    Toast.makeText(Login.this,"Fields Are Required.", Toast.LENGTH_SHORT).show();
                    return;

                }

                progressBar.setVisibility(View.VISIBLE);

                //delete work of the anonymous user

                if(fAuth.getCurrentUser().isAnonymous()){
                    user = fAuth.getCurrentUser();

                    fStore.collection("work").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Login.this,"All Temp work are deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //delete anonymous user
                    user.delete();

                }

                //sign the user with its email and password
                fAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Login.this,"Success.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener(){


                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this,"Login Failed." + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });




    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are You Sure ?")
                .setMessage("Linking Existing Account Will Delete All The Work!. Create New Account to Save It!")
                .setPositiveButton("Save Work", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Its Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                    }
                });

        warning.show();
    }
}