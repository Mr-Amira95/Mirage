package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import com.squareup.picasso.Picasso;
import com.developer.kalert.KAlertDialog;


public class testKnowledge extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Declare all variables
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    NavigationView nav;
    TextView letterRandom;
    CameraView cameraViewt;
    AlertDialog waitingDialogt;
    Button start;

    static String u, i, a;
    static String alphabet;

    FirebaseAutoMLRemoteModel remoteModel; // For loading the model remotely
    FirebaseVisionImageLabeler labeler; //For running the image labeler
    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder; // Which option is use to run the labeler local or remotely
    FirebaseModelDownloadConditions conditions; //Conditions to download the model
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_knowledge);

        //Get variables values
        letterRandom = findViewById(R.id.letter);
        cameraViewt = findViewById(R.id.cameraT);
        waitingDialogt = new SpotsDialog.Builder().setContext(this).setMessage("Please Wait...").setCancelable(false).build();
        start = findViewById(R.id.startT);

        setAlphabet(); //Setup random alphabet on text view
        setNavigation(); //Setup Navigation Drawer
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

        //Camera Kit Setup
        cameraViewt.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }
            @Override
            public void onError(CameraKitError cameraKitError) {
            }
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialogt.show(); //show Dialog

                //Setup format for the captured image
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraViewt.getWidth(),cameraViewt.getHeight(),false);

                cameraViewt.stop(); //Stop the camera view
                runDetectore(bitmap); //call method to start process on image
            }
            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {}
        });

        //capture image and load the model remotely
        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                cameraViewt.start();
                cameraViewt.captureImage(); //Capture image and call onImage method
                fromRemoteModel(); //load the remote model in the application
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

    private void setAlphabet() {
        String[] text= new String[] {"أ","ب","ف","ك","ن","ي","و","م","ر","ح"};
        Random rand= new Random();
        int randomm= rand.nextInt(text.length);
        alphabet = text[randomm];
        letterRandom.setText(alphabet);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        int id = menu_item.getItemId() ;
        if (id == R.id.activity_start){
            Intent start = new Intent(testKnowledge.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(testKnowledge.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(testKnowledge.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(testKnowledge.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(testKnowledge.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(testKnowledge.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(testKnowledge.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(getApplicationContext(),LogIn.class);
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
    @Override
    protected void onResume() {
        super.onResume();
        cameraViewt.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        cameraViewt.stop();
    }
    private void runDetectore(Bitmap bitmap) {
        //convert image from bitmap to firebase vision image to start process
        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        //connect to firebase Auto machine learning remotely
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isComplete()) {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                    .setConfidenceThreshold(0.0f)
                                    .build();
                            try {
                                labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                                //Call process to match images with a Dataset on firebase using auto machine learning techniques
                                processImageLabeler(labeler, image);
                            } catch (FirebaseMLException exception) {
                                Log.e("TAG", "onSuccess: " + exception);
                                Toast.makeText(testKnowledge.this, "Ml exeception", Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(testKnowledge.this, "Not downloaded", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: "+e );
                Toast.makeText(testKnowledge.this, "err"+e, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
        labeler.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {

                //declare variables to get the highest confidence after matching the image with the dataset
                double conf =0;
                String lab= "";

                for (FirebaseVisionImageLabel label : Objects.requireNonNull(task.getResult())) {
                    String eachlabel = label.getText().toUpperCase();
                    float confidence = label.getConfidence();

                    if(confidence>conf){
                        conf = confidence;
                        lab = eachlabel;
                    }
                }

                //showing dialog if the image captured matches the alphabet shown for testing or not
                if(alphabet.equals("أ") && lab.equals("ALEF")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ب") && lab.equals("BA")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ح") && lab.equals("HAA")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ف") && lab.equals("FA")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ك") && lab.equals("KAF")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ر") && lab.equals("RA")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("م") && lab.equals("MEM")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ن") && lab.equals("NON")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("و") && lab.equals("WAW")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else if(alphabet.equals("ي") && lab.equals("YA")){
                    new KAlertDialog(testKnowledge.this, KAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Good job!").setContentText("Your Sign is correct").setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).show();
                }else{
                    new KAlertDialog(testKnowledge.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Hard Luck").setContentText("Do you wanna to try it again?")
                            .setCancelText("No").showCancelButton(true).setConfirmText("Yes").setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            Intent i = new Intent(testKnowledge.this, testKnowledge.class);
                            startActivity(i);
                        }
                    }).setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            kAlertDialog.cancel();
                            cameraViewt.start();

                        }
                    }).show();
                }
                waitingDialogt.cancel(); //Stop the Dialog

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(testKnowledge.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Loading dataset (Model) remotely form firebase
    private void fromRemoteModel() {
        remoteModel = new FirebaseAutoMLRemoteModel.Builder("Signs_202052222155").build();
        conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {}
                });
    }
}