package com.example.worktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SharedPreference extends AppCompatActivity {

    EditText wageAmount;
    Button saveAmount;
    SharedPreferences sp;
    String amount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_preference);

        wageAmount = findViewById(R.id.wageAmount);
        saveAmount = findViewById(R.id.saveWageAmount);

        sp = getSharedPreferences("wagePreference", Context.MODE_PRIVATE);

        saveAmount.setOnClickListener(new handleClick());
    }


    //cookbook listener
    public class handleClick implements View.OnClickListener {
        public void onClick(View arg0) {
            amount = wageAmount.getText().toString();

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("amount",amount);
            editor.commit();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }

}