package com.example.worktracker.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worktracker.MainActivity;
import com.example.worktracker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    EditText userName,userEmail,userPass,userConfPass;
    Button syncAccount;
    TextView loginAct;
    ProgressBar progressBar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //enable back button and set title
        getSupportActionBar().setTitle("Create New Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPass = findViewById(R.id.password);
        userConfPass = findViewById(R.id.passwordConfirm);

        syncAccount = findViewById(R.id.sync);
        loginAct = findViewById(R.id.Login);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        
        loginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                //create animation
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();
            }
        });

        syncAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String UserName = userName.getText().toString();
                String UserEmail = userEmail.getText().toString();
                String UserPass = userPass.getText().toString();
                String UserConfPass = userConfPass.getText().toString();

                if(UserEmail.isEmpty() || UserName.isEmpty() || UserConfPass.isEmpty() || UserPass.isEmpty()) {
                    Toast.makeText(Register.this,"All Fields Are Required.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!UserPass.equals(UserConfPass)) {
                    userConfPass.setError("Password Does Not Match.");
                }

                //merge anonymous user with current email and password
                AuthCredential credential = EmailAuthProvider.getCredential(UserEmail,UserPass);
                fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Register.this,"Work are synced.",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                        //store the user name and email, to display it on the navigation drawer
                        FirebaseUser usr = fAuth.getCurrentUser();
                        //create new profile change request in order to change the object
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(UserName)
                                .build();
                        usr.updateProfile(request);

                        //we start the main activity again to refresh, without it the user name doesn't appear
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this,"Failed to connect, Try again.",Toast.LENGTH_SHORT).show();

                    }
                });


                }




            });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
        finish();
        return super.onOptionsItemSelected(item);

    }
}