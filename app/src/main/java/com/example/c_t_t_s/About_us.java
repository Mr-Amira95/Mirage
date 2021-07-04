package com.example.c_t_t_s;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.developer.kalert.KAlertDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class About_us extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Declare all variables
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView nav;
    EditText feedback;
    Button sendFeedback;
    static String u, i, a;

    //Firebase variables
    DatabaseReference mRef;
    FeedBackDB FeedBackDB;
    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        feedback = findViewById(R.id.feedback);
        sendFeedback = findViewById(R.id.sendFeedback);
        videoView = findViewById(R.id.aboutUsVideo);

        mRef = FirebaseDatabase.getInstance().getReference().child("Feedback");
        FeedBackDB = new FeedBackDB();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        showVideo(videoView);
        setNavigation();
        setActionBar();

        if(user != null){
            mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Accounts").child(user.getUid());
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    u= dataSnapshot.child("username").getValue().toString();
                    i= dataSnapshot.child("image").getValue().toString();
                    a= dataSnapshot.child("account").getValue().toString();

                    if(a.equals("User")){
                        Menu nav_Menu = nav.getMenu();
                        nav_Menu.findItem(R.id.viewFeedback).setVisible(false);
                    }


                    View view =  nav.getHeaderView(0);
                    TextView nav_user = view.findViewById(R.id.username_header);
                    nav_user.setText(u);

                    CircleImageView userImage = view.findViewById(R.id.profilePic);

                    if(!i.equals(" ")){
                        Picasso.get().load(i).into(userImage);
                    }
                    mDatabaseRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String message = feedback.getText().toString();

                if (user != null && !message.isEmpty()) {
                    String email = user.getEmail();

                    FeedBackDB.setFeedback(message);
                    FeedBackDB.setEmail(email);
                    mRef.push().setValue(FeedBackDB);

                    feedback.setText("");
                    new KAlertDialog(About_us.this)
                            .setTitleText("Feedback Sent with your email").confirmButtonColor(R.color.darkBlue)
                            .show();

                }else if (user == null && !message.isEmpty()){
                    String email = "No Email Address";

                    FeedBackDB.setFeedback(message);
                    FeedBackDB.setEmail(email);
                    mRef.push().setValue(FeedBackDB);

                    feedback.setText("");
                    new KAlertDialog(About_us.this)
                            .setTitleText("Feedback Sent without email").confirmButtonColor(R.color.darkBlue)
                            .show();
                }else{
                    new KAlertDialog(About_us.this)
                            .setTitleText("FeedBack is Empty!").confirmButtonColor(R.color.darkBlue)
                            .show();
                }
            }
        });
    }

    private void setActionBar() {
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (user != null){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else{
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

    }

    private void setNavigation() {
        nav = findViewById(R.id.navigationView);
        nav.setItemIconTintList(null);
        nav.setNavigationItemSelectedListener(About_us.this);
    }

    private void showVideo(VideoView videoView) {
        videoView.setVideoPath("android.resource://" + getPackageName()+ "/" + R.raw.video);
        videoView.start();
        MediaController MC = new MediaController(this);
        MC.setAnchorView(videoView);
        videoView.setMediaController(MC);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        int id = menu_item.getItemId() ;
        if (id == R.id.activity_start){
            Intent start = new Intent(About_us.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(About_us.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(About_us.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(About_us.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(About_us.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(About_us.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(About_us.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(About_us.this,LogIn.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (user != null) {
            getMenuInflater().inflate(R.menu.menu_logout,menu);
            return super.onCreateOptionsMenu(menu);
        }
        else{
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        if (item.getItemId()==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(getApplicationContext(),LogIn.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}