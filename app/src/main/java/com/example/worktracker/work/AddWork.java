package com.example.worktracker.work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.worktracker.MainActivity;
import com.example.worktracker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddWork extends AppCompatActivity {
    FirebaseFirestore fStore;
    FirebaseUser user;


    //define calender variables
    Button start,end;
    int startWorkHour;
    int startWorkMinute;
    int endWorkHour;
    int endWorkMinute;
    String startWork;
    String endWork;
    String totalWork;
    String date;
    float diffHours;
    float diffMinutes;
    CalendarView calendarView;
    ProgressBar progressBarSave;

    //shared preference
    SharedPreferences sp;
    float wage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_work);

        //enable back button and set title
        getSupportActionBar().setTitle("Add Your Work");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //shared Preferences
        //shared preference
        // Check Shared Preference
        sp = getApplicationContext().getSharedPreferences("wagePreference", Context.MODE_PRIVATE);
        wage = Float.valueOf(sp.getString("amount","31"));

        //initialize firebase
        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        progressBarSave = findViewById(R.id.progressBar);
        //Save work floating button
        FloatingActionButton saveDates = findViewById(R.id.saveDates);
        saveDates.setOnClickListener((view) -> {
            if(startWork == null || endWork == null) {
                Toast.makeText(this, "Please Choose Work Hours ", Toast.LENGTH_LONG).show();
                return;
            }

            //if the work is the current day (user doesn't pick a date)
            if(date == null){
                Calendar c = Calendar.getInstance();
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                date = String.valueOf(mDay) + "/" + String.valueOf(mMonth+1);
            }

            progressBarSave.setVisibility(view.VISIBLE);

            //save work!
            //work is our collection, inside work we will have many days
            DocumentReference docref = fStore.collection("work").document(user.getUid()).collection("myWork").document();
            Map<String,Object> work = new HashMap<>();
            work.put("date",date);
            work.put("start",startWork);
            work.put("end",endWork);

            //calculate work hours and salary
            long Difference;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            try {
                Date date1 = format.parse(startWork);
                Date date2 = format.parse(endWork);
                Difference = date2.getTime() - date1.getTime();
                diffMinutes =  Difference / (60 * 1000) % 60;
                diffHours = Difference / (60 * 60 * 1000);
                totalWork = String.valueOf((int) diffHours) + ":" + String.valueOf((int)diffMinutes);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            work.put("hours",String.valueOf(totalWork));

            //shared preference
            // Check Shared Preference
            float salary = (diffHours*wage) + (diffMinutes/60)*wage;
            work.put("salary",String.valueOf(salary));

            //insert work to firebase, if succeeded Toast "OK", if not, Toast "Error"
            docref.set(work).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(AddWork.this,"Work Added...",Toast.LENGTH_SHORT).show();
                    progressBarSave.setVisibility(view.INVISIBLE);
                    onBackPressed();
                }
            }).addOnFailureListener(new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddWork.this,"Error, Try Again...",Toast.LENGTH_SHORT).show();
                }
            });
        });
        start = findViewById(R.id.startWork);
        end = findViewById(R.id.endWork);

        //Calender Listener
        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                date = dayOfMonth +  "/" + (month + 1);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);

    }

    public void startTimePicker(View view){

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                startWorkHour = selectedHour;
                startWorkMinute = selectedMinute;
                startWork = String.format(Locale.getDefault(),"%02d:%02d",startWorkHour,startWorkMinute);
                start.setText(startWork);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,listener,startWorkHour,startWorkMinute,true);
        timePickerDialog.setTitle("Select Time: ");

        timePickerDialog.show();
    }


    public void endTimePicker(View view){

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                endWorkHour = selectedHour;
                endWorkMinute = selectedMinute;
                endWork = String.format(Locale.getDefault(),"%02d:%02d",endWorkHour,endWorkMinute);
                end.setText(endWork);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,listener,endWorkHour,endWorkMinute,true);
        timePickerDialog.setTitle("Select Time: ");

        timePickerDialog.show();
    }





}