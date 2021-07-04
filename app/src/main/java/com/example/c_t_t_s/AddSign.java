package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class AddSign extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Declare all variables
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button checkWord, btnUploadImage;
    private EditText word;

    NavigationView nav;
    CameraView cameraView;
    AlertDialog waitingDialog;

    static String u, i, a;
    static int counter = 5;

    //Firebase variables
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef, mDatabaseRef1;
    private StorageTask mUploadTask;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sign);

        // Get variables values
        cameraView = findViewById(R.id.add_camera_view);
        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Please Wait...").setCancelable(false).build();
        checkWord = findViewById(R.id.checkButton);
        btnUploadImage = findViewById(R.id.start);
        word = findViewById(R.id.word);

        //Hide button and camera view
        btnUploadImage.setVisibility(btnUploadImage.INVISIBLE);
        cameraView.setVisibility(cameraView.INVISIBLE);

        setNavigation(); // Setup Navigation view
        setActionbar(); // Setup Drawer layout and Actionbar

        //Connect to firebase
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("UsersSigns").child(user.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("UserSigns").child(user.getUid());
        mAuth = FirebaseAuth.getInstance();
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

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddSign.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        //Camera Kit Setup
        cameraView.addCameraKitListener(new CameraKitEventListener(){
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {}
            @Override
            public void onError(CameraKitError cameraKitError) {}
            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                waitingDialog.show(); //show dialog
                Bitmap bitmap = cameraKitImage.getBitmap(); //Setup format for the captured image
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop(); // Stop the camera view
                upload(bitmap); //Upload image to firebase
            }
            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {}
        });

        checkWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String gottenWord = word.getText().toString().toUpperCase();
                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean found = false;
                        if(dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String f = postSnapshot.child("label").getValue().toString();
                                if (gottenWord.equals(f.toUpperCase())) {
                                    new KAlertDialog(AddSign.this)
                                            .setTitleText("The word already exists")
                                            .setContentText("Please Enter Another Word").confirmButtonColor(R.color.darkBlue)
                                            .show();
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            new KAlertDialog(AddSign.this)
                                    .setTitleText("The word is Acceptable")
                                    .setContentText("Please Start Capturing 5 Images for the word").confirmButtonColor(R.color.darkBlue)
                                    .show();

                            counter= 5;
                            word.setEnabled(false);
                            checkWord.setEnabled(false);
                            btnUploadImage.setVisibility(btnUploadImage.VISIBLE);
                            cameraView.setVisibility(cameraView.VISIBLE);
                            btnUploadImage.setText("Capture " + counter + " Images");
                        }
                        mDatabaseRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });

        //capture image and upload to firebase
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage(); //capture image and call onImage method
            }
        });
    }

    private void setNavigation() {
        nav = findViewById(R.id.navigationView);
        nav.setItemIconTintList(null);
        nav.setNavigationItemSelectedListener(this);
    }

    private void setActionbar() {
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void upload(Bitmap bitmap) {
            ContentResolver cr = getContentResolver();
            String title = "mySigns";
            String description = "...";
            String url = MediaStore.Images.Media.insertImage(cr, bitmap, title, description);

            final StorageReference fileReference = mStorageRef.child(word.getText().toString()).child(System.currentTimeMillis() + "." + getFileExtension(Uri.parse(url)));
            mUploadTask = fileReference.putFile(Uri.parse(url)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mDatabaseRef.child(word.getText().toString()).child("img"+counter).setValue(uri.toString());
                            mDatabaseRef.child(word.getText().toString()).child("label").setValue(word.getText().toString());
                            cameraView.start();
                            waitingDialog.cancel();
                            counter--;
                            if(counter==0){
                                new KAlertDialog(AddSign.this)
                                        .setTitleText("Sign Added").setContentText("Do you wanna to Add Another Sign?").confirmButtonColor(R.color.darkBlue)
                                        .setCancelText("No").showCancelButton(true).setConfirmText("Yes").setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                                    @Override
                                    public void onClick(KAlertDialog kAlertDialog) {
                                        Intent i = new Intent(AddSign.this, MainActivity.class);
                                        startActivity(i);
                                    }
                                }).setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                    @Override
                                    public void onClick(KAlertDialog kAlertDialog) {
                                        Intent i = new Intent(AddSign.this, AddSign.class);
                                        startActivity(i);
                                    }
                                }).show();
                            }else{
                                btnUploadImage.setText("Capture " + counter + " Images");
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddSign.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
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
            Intent start = new Intent(AddSign.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(AddSign.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(AddSign.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(AddSign.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(AddSign.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(AddSign.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(AddSign.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(getApplicationContext(),LogIn.class);
            startActivity(i);
        }
        return true;
    }
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
