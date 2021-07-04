package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChangePassword extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Declare all variables
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    NavigationView nav;
    EditText pass, cPass, oPass;
    Button change;

    //Firebase variables
    private DatabaseReference mDatabaseRef1;
    static String u, i, a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //get variables values
        oPass = findViewById(R.id.password);
        pass = findViewById(R.id.passwordReset);
        cPass = findViewById(R.id.passwordResetConfirm);
        change = findViewById(R.id.changePassBtn);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef1= FirebaseDatabase.getInstance().getReference().child("Accounts").child(user.getUid());

        setNavigation(); //Setup Navigation view
        setActionbar(); //Setup Drawer layout and Actionbar

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

        //get the values from user and check if valid and update password on database
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting the values from user
                final String p = pass.getText().toString();
                final String cp= cPass.getText().toString();
                final String op = oPass.getText().toString();

                //check if the values entered are valid
                    if (p.length() <= 8 || p.length() >= 15 || p.contains(" ")) {
                        new KAlertDialog(ChangePassword.this, KAlertDialog.ERROR_TYPE)
                                .setTitleText("Password Isn't Valid!").confirmButtonColor(R.color.darkBlue)
                                .show();
                        pass.requestFocus();
                    } else if (!(p.equals(cp))) {
                        new KAlertDialog(ChangePassword.this, KAlertDialog.ERROR_TYPE)
                                .setTitleText("Password Doesn't Match!").confirmButtonColor(R.color.darkBlue)
                                .show();
                    } else if(op.equals(p)) {
                        new KAlertDialog(ChangePassword.this, KAlertDialog.ERROR_TYPE)
                                .setTitleText("Please Don't use the old password!").confirmButtonColor(R.color.darkBlue)
                                .show();
                    }else {
                        //update the password into database
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), op);

                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                new KAlertDialog(ChangePassword.this, KAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Password updated").confirmButtonColor(R.color.darkBlue)
                                                        .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                                    @Override
                                                    public void onClick(KAlertDialog kAlertDialog) {
                                                        FirebaseAuth.getInstance().signOut();
                                                        finish();
                                                        Intent i = new Intent(getApplicationContext(),LogIn.class);
                                                        startActivity(i);
                                                    }
                                                }).show();

                                            } else {
                                                new KAlertDialog(ChangePassword.this, KAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Error password not updated!").confirmButtonColor(R.color.darkBlue)
                                                        .show();
                                            }
                                        }
                                    });
                                } else {
                                    new KAlertDialog(ChangePassword.this, KAlertDialog.ERROR_TYPE)
                                            .setTitleText("Error auth failed!").confirmButtonColor(R.color.darkBlue)
                                            .show();
                                }
                            }
                        });

                    }
            }
        });
    }

    private void setActionbar() {
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setNavigation() {
        nav = findViewById(R.id.navigationView);
        nav.setItemIconTintList(null);
        nav.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        int id = menu_item.getItemId() ;
        if (id == R.id.activity_start){
            Intent start = new Intent(ChangePassword.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(ChangePassword.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(ChangePassword.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(ChangePassword.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(ChangePassword.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(ChangePassword.this,ViewFeedback.class);
            startActivity(view);
        }        else if (id == R.id.activity_About_Us){
            Intent about = new Intent(ChangePassword.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(ChangePassword.this,LogIn.class);
            startActivity(i);
        }
        return true;
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
}
