package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Picasso;
import java.util.Calendar;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditInfo extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

   //Declare all variables
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private static final int CHOOSE_IMAGE = 1;
    private Uri imgUrl;

    NavigationView nav;
    TextView birthdate ;
    EditText fName, lName, email, phone, username;
    Button changePassword, changePic, update, delete;
    ImageView img;
    static String f, l, e, p, b, u, i, a;
    static boolean updated;

    //Firebase variables
    private StorageReference mStorageRef, mStorageRef1;
    private StorageTask mUploadTask;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef, mDatabaseRef1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        //Get variables values
        fName = findViewById(R.id.firstNameU);
        lName = findViewById(R.id.lastNameU);
        email = findViewById(R.id.emailU);
        phone = findViewById(R.id.phoneU);
        birthdate = findViewById(R.id.birthdateU);
        username = findViewById(R.id.userNameU);
        changePassword = findViewById(R.id.changePass);
        changePic = findViewById(R.id.choosePhotoU);
        update = findViewById(R.id.update);
        delete = findViewById(R.id.delete);
        img = findViewById(R.id.personalImgU);

        setNavigation(); //Setup Navigation view
        setActionbar(); //Setup Drawer layout and Actionbar

        //Connect to firebase
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Accounts").child(user.getUid());

        //setting Username and User Image in Navigation drawer
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //Setup Calendar
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);

        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(EditInfo.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        birthdate.setText(date);
                    }
                },year,month,day);
                dpd.show();
            }
        });
        //Connect to firebase storages
        mStorageRef = FirebaseStorage.getInstance().getReference("Accounts");
        mStorageRef1 = FirebaseStorage.getInstance().getReference("UserSigns").child(user.getUid());
        mDatabaseRef1= FirebaseDatabase.getInstance().getReference().child("UsersSigns").child(user.getUid());

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //getting account information from database
                f= dataSnapshot.child("fName").getValue().toString();
                l= dataSnapshot.child("lName").getValue().toString();
                e= dataSnapshot.child("email").getValue().toString();
                p= dataSnapshot.child("phone").getValue().toString();
                b= dataSnapshot.child("birthdate").getValue().toString();
                u= dataSnapshot.child("username").getValue().toString();
                i= dataSnapshot.child("image").getValue().toString();
                a= dataSnapshot.child("account").getValue().toString();

                if(a.equals("User")){
                    Menu nav_Menu = nav.getMenu();
                    nav_Menu.findItem(R.id.viewFeedback).setVisible(false);
                }

                //showing account information on the screen for the client
                fName.setHint(f);
                lName.setHint(l);
                email.setHint(e);
                phone.setHint(p);
                birthdate.setText(b);
                username.setHint(u);
                if(!i.equals(" ")){
                    Picasso.get().load(i).into(img);
                }
                mDatabaseRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //choosing image to set as new profile picture
        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChoose();
            }
        });

        //getting values from database to update account information
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            updated = false;
            //getting values from database
            final String fn = fName.getText().toString();
            final String ln = lName.getText().toString();
            final String em = email.getText().toString();
            final String ph = phone.getText().toString();
            final String bi = birthdate.getText().toString();
            final String us = username.getText().toString();

            //checking each value entered then storing it on database
            if(!fn.equals(f) && !fn.equals("")){
            mDatabaseRef.child("fName").setValue(fn);
            updated = true ;
            }

            if(!ln.equals(l) && !ln.equals("")){
                mDatabaseRef.child("lName").setValue(ln);
                updated = true ;
            }

            if(!ph.equals(p) && !ph.isEmpty()) {
                if ((ph.length() != 9 && ph.length() != 10) || (ph.length() == 10 && ph.charAt(0) != '0') ||
                (ph.length() == 10 && ph.charAt(1) != '7') || (ph.length() == 9 && ph.charAt(0) != '7')) {
                    new KAlertDialog(EditInfo.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Incorrect Phone Number!").confirmButtonColor(R.color.darkBlue)
                            .show();
                } else {
                    mDatabaseRef.child("phone").setValue(ph);
                    updated = true ;
                }
            }
            if(!bi.equals(b) && !bi.equals("")){
                mDatabaseRef.child("birthdate").setValue(bi);
                updated = true ;
            }
            if(!us.equals(u) && !us.equals("")){
                mDatabaseRef.child("username").setValue(us);
                updated = true ;
            }

            if(imgUrl != null){
                final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imgUrl));
                mUploadTask = fileReference.putFile(imgUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mDatabaseRef.child("image").setValue(uri.toString());
                                updated = true;
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditInfo.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            if(!em.equals(e) && !em.isEmpty()){
                user.updateEmail(em)
                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("email").setValue(em);
                            new KAlertDialog(EditInfo.this, KAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Information Updated").confirmButtonColor(R.color.darkBlue)
                                    .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                        @Override
                                        public void onClick(KAlertDialog kAlertDialog) {
                                            Intent i = new Intent(EditInfo.this, LogIn.class);
                                            startActivity(i);
                                        }
                                    }).show();
                        }
                    }
                });
            }
            if(updated){
                new KAlertDialog(EditInfo.this, KAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Information Updated").confirmButtonColor(R.color.darkBlue)
                        .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                            @Override
                            public void onClick(KAlertDialog kAlertDialog) {
                                Intent i = new Intent(EditInfo.this, EditInfo.class);
                                startActivity(i);
                            }
                        }).show();
            }
            }
        });

        //button change password that redirects to change password screen
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditInfo.this, ChangePassword.class);
                startActivity(i);
            }
        });

        //delete Account
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        mDatabaseRef.removeValue();

                        mDatabaseRef1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildrenCount() != 0) {
                                    mDatabaseRef1.removeValue();
                                }
                                mDatabaseRef1.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });

                        if(!i.equals(" ")){
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(i);
                            storageReference.delete();
                        }
                        new KAlertDialog(EditInfo.this, KAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Account Deleted").confirmButtonColor(R.color.darkBlue)
                                .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                    @Override
                                    public void onClick(KAlertDialog kAlertDialog) {
                                        Intent i = new Intent(EditInfo.this, LogIn.class);
                                        startActivity(i);
                                    }
                                }).show();
                    }
                }
                });
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
            Intent start = new Intent(EditInfo.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(EditInfo.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(EditInfo.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(EditInfo.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
                Intent Edit = new Intent(EditInfo.this,EditInfo.class);
                startActivity(Edit);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(EditInfo.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(EditInfo.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(EditInfo.this,LogIn.class);
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

    //show file choose dialog to choose a profile picture from the phone
    private void showFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CHOOSE_IMAGE);
    }
    //Setting the image chosen on Image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUrl = data.getData();
            img.setImageURI(imgUrl);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
