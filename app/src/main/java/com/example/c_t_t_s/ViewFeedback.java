package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;


public class ViewFeedback extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Declare all variables
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView recyclerView;
    private FeedbackAdapter feedbackAdapter;
    private ArrayList<FeedBackDB> feedBackDB; //Declare an Array to store Feedback form the Database

    NavigationView nav;
    private DrawerLayout drawerLayout;
    DatabaseReference mDatabaseRef, mDatabaseRef1;
    FirebaseAuth mAuth;
    static String u, i, a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        setNavigation(); // Setup Navigation view
        setActionbar(); // Setup Drawer layout and Actionbar

        setRecycler();

        //Connect to firebase
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseRef1= FirebaseDatabase.getInstance().getReference().child("Accounts").child(user.getUid());

        //setting Username and User Image in Navigation drawer
        mDatabaseRef1.addValueEventListener(new ValueEventListener() {
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
                mDatabaseRef1.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //Declare an Array for Feedbacks
        feedBackDB=new ArrayList<>();

        //connect to database to get the Feedbacks
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Feedback");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //reach each feedback on database and store it in Array
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    FeedBackDB feedback =postSnapshot.getValue(FeedBackDB.class);
                    feedBackDB.add(feedback);
                }
                //set each feedback on screen
                feedbackAdapter = new FeedbackAdapter(ViewFeedback.this, feedBackDB);
                recyclerView.setAdapter(feedbackAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewFeedback.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setRecycler() {
        recyclerView=findViewById(R.id.feedbackRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setNavigation() {
        nav = findViewById(R.id.navigationView);
        nav.setItemIconTintList(null);
        nav.setNavigationItemSelectedListener(ViewFeedback.this);
    }

    private void setActionbar() {
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout,menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        int id = menu_item.getItemId() ;
        if (id == R.id.activity_start){
            Intent start = new Intent(ViewFeedback.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(ViewFeedback.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(ViewFeedback.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(ViewFeedback.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(ViewFeedback.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(ViewFeedback.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(ViewFeedback.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(getApplicationContext(),LogIn.class);
            startActivity(i);
        }
        return true;
    }
}