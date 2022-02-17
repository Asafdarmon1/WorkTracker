package com.example.worktracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worktracker.auth.Login;
import com.example.worktracker.auth.Register;
import com.example.worktracker.model.Work;
import com.example.worktracker.network.MyBroadCastReceiver;
import com.example.worktracker.work.AddWork;
import com.example.worktracker.work.EditWork;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Variables for all the layouts that we created
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    //recycle view
    RecyclerView hourList;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Work, WorkViewHolder> workAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;
    //BroadCast
    private BroadcastReceiver MyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //register to broadcast class
        MyReceiver = new MyBroadCastReceiver();
        //register for the first time, when the app is starting (BroadCast)
        broadcastIntent();

        //start foreground service
        Intent serviceIntent = new Intent(this,MyForegroundService.class);
        //initialize service
        startForegroundService(serviceIntent);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        //get the collection from firebase
        //order by date with ascending order
        //get specific user id, that is connected to our app
        Query query = fStore.collection("work").document(user.getUid()).collection("myWork").orderBy("date", Query.Direction.ASCENDING);

        //execute query, using 1 class where we want to extract the data
        FirestoreRecyclerOptions<Work> allWork = new FirestoreRecyclerOptions.Builder<Work>()
                .setQuery(query, Work.class)
                .build();


        workAdapter = new FirestoreRecyclerAdapter<Work, WorkViewHolder>(allWork) {
            @Override
            protected void onBindViewHolder(@NonNull WorkViewHolder workViewHolder, int i, @NonNull Work work) {
                //bind the received data to the view
                workViewHolder.dateTextView.setText(work.getDate());
                workViewHolder.startTextView.setText(work.getStart());
                workViewHolder.endTextView.setText(work.getEnd());
                workViewHolder.hoursTextView.setText(work.getHours());
                workViewHolder.salaryTextView.setText(work.getSalary());

                //3 dots on each line, update or delete
                ImageView menuIcon = workViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    String docId = workAdapter.getSnapshots().getSnapshot(workViewHolder.getAdapterPosition()).getId();

                    @Override
                    public void onClick(final View v) {
                        PopupMenu menu = new PopupMenu(v.getContext(), v);
                        //change the menu opening side
                        menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i = new Intent(v.getContext(), EditWork.class);
                                i.putExtra("date", work.getDate());
                                i.putExtra("start", work.getStart());
                                i.putExtra("end", work.getEnd());
                                i.putExtra("hours", work.getHours());
                                i.putExtra("salary", work.getSalary());
                                i.putExtra("workId", docId);
                                startActivity(i);
                                return false;
                            }
                        });
                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fStore.collection("work").document(user.getUid()).collection("myWork").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(MainActivity.this, "Work Deleted Successfully..", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Work Doesn't Deleted, Please Try Again..", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        menu.show();
                    }
                });


            }

            

            @NonNull
            @Override
            public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_view_layout, parent, false);
                return new WorkViewHolder(view);
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);

        nav_view = findViewById(R.id.nav_view);
        //set listener to the navigation buttons
        nav_view.setNavigationItemSelectedListener(this);

        //action bar toggle, pass the context where we want the action bar toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        //assign the resources for the recycle view
        hourList = findViewById(R.id.hoursList);
        hourList.setLayoutManager(new LinearLayoutManager(this));
        hourList.setAdapter((RecyclerView.Adapter) workAdapter);

        //display the user name and email in the navigation drawer
        View headerView = nav_view.getHeaderView(0); //0 its the position, we have 1 header
        TextView username = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            username.setText("Temporary User");
        } else {
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

        //add work button

        FloatingActionButton addWorkButton = findViewById(R.id.addWork);
        addWorkButton.setOnClickListener((view) -> {
            startActivity(new Intent(this, AddWork.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        });


    }

    @Override
    //Handle the navigation clicks
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START); // when user click on the navigation drawer, it will close
        switch (item.getItemId()) {

            case R.id.addWork:
                startActivity(new Intent(this, AddWork.class));
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

            case R.id.logout:
                //check if the user is real or anonymous
                //If anonymous, we need to delete all the data
                CheckUser();
                break;

            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(this, Login.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                } else
                    Toast.makeText(this, "You Are Connected!", Toast.LENGTH_SHORT).show();
                break;

            //Dialog Fragment
            case R.id.shareapp:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Here is the share content body";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;

            case R.id.rating:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                break;

            case R.id.about:
                MyDialogFragment myDialogFragment = new MyDialogFragment();
                myDialogFragment.show(getSupportFragmentManager(), "MyFragment");
                break;


            default:
                Toast.makeText(this, "Coming Soon..", Toast.LENGTH_SHORT).show();

        }

        return false;
    }

    private void CheckUser() {
        //check if user is not real
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Splash.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }

    //display all the alert popup
    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are You Sure ?")
                .setMessage("You are logged in with Temporary Account. Logging out will Delete All Your Work!!")
                .setPositiveButton("Sync Work", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Todo: delete all work days created by anonymous user

                        // Todo: delete the anonymous user
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                            @Override
                            public void onSuccess(Void unused) {
                                startActivity(new Intent(getApplicationContext(), Splash.class));
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                                finish();
                            }
                        });
                    }
                });

        warning.show();
    }


    //options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu resource file
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //handle menu clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.wage) //If setting menu is clicked
            startActivity(new Intent(getApplicationContext(),SharedPreference.class));

        return super.onOptionsItemSelected(item);
    }

    public class WorkViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView startTextView;
        public TextView endTextView;
        public TextView hoursTextView;
        public TextView salaryTextView;
        //handle the clicks on the recycle view items!
        View view;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            startTextView = (TextView) itemView.findViewById(R.id.startTextView);
            endTextView = (TextView) itemView.findViewById(R.id.endTextView);
            hoursTextView = (TextView) itemView.findViewById(R.id.hoursTextView);
            salaryTextView = (TextView) itemView.findViewById(R.id.salaryTextView);
            view = itemView;
        }
    }

    //Listen for any changes in the data as the activity in starting
    @Override
    protected void onStart() {
        super.onStart();
        workAdapter.startListening();
    }

    //Once the app is closed, we will stop listen
    @Override
    protected void onStop() {
        super.onStop();
        if (workAdapter != null)
            workAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }

}