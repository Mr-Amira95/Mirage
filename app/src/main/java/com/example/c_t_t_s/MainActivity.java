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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Locale;
import java.util.Objects;

import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Declare all variables
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView nav;
    CameraView cameraView;
    AlertDialog waitingDialog;
    TextView convertedText;
    Button startDetection, startVoice;
    static String u, i, a;
    TextToSpeech tts;
    static String text="";

    FirebaseAutoMLRemoteModel remoteModel; // For loading the model remotely
    FirebaseVisionImageLabeler labeler; //For running the image labeler
    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder; // Which option is use to run the labeler local or remotely
    FirebaseModelDownloadConditions conditions; //Conditions to download the model
    FirebaseVisionImage image; // preparing the input image
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        text = ""; // Text that will be used in text-to-speech

        //Get variables values
        cameraView = findViewById(R.id.camera_view);
        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Please Wait...").setCancelable(false).build();
        convertedText = findViewById(R.id.convertedText);
        startDetection =findViewById(R.id.start);
        startVoice = findViewById(R.id.voice);

        setnavigation(); //Setup Navigation view
        setActionbar(); //Setup Drawer layout and Actionbar
        setTTS(); //Setup Text-to-speech

        //connect to firebase
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

        //button to start audio
        startVoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        });

        //capture image and load the model remotely
        startDetection.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                cameraView.start();
                cameraView.captureImage(); //capture image and call onImage method
                fromRemoteModel(); //load the remote model in the application
            }
        });

        //Camera Kit Setup
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }
            @Override
            public void onError(CameraKitError cameraKitError) {
            }
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show(); //show Dialog

                //Setup format for the captured image
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);

                cameraView.stop(); //Stop the camera view
                runDetectore(bitmap); //call method to start process on image
            }
            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {}
        });
    }

    private void setTTS() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i){
                if(i == TextToSpeech.SUCCESS){
                    int lang = tts.setLanguage(Locale.ENGLISH);
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

    private void setnavigation() {
        nav = findViewById(R.id.navigationView);
        nav.setItemIconTintList(null);
        nav.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        int id = menu_item.getItemId() ;
        if (id == R.id.activity_start){
            Intent start = new Intent(MainActivity.this,MainActivity.class);
            startActivity(start);
        }else if (id == R.id.activity_Add_Sign){
            Intent add = new Intent(MainActivity.this,AddSign.class);
            startActivity(add);
        }else if (id == R.id.activity_Learn_Sign){
            Intent learn = new Intent(MainActivity.this,LearnSign.class);
            startActivity(learn);
        }else if (id == R.id.activity_test_knowledge) {
            Intent test = new Intent(MainActivity.this, testKnowledge.class);
            startActivity(test);
        }else if (id == R.id.viewFeedback){
            Intent view = new Intent(MainActivity.this,ViewFeedback.class);
            startActivity(view);
        }else if (id == R.id.activity_edit_info){
            Intent Edit = new Intent(MainActivity.this,EditInfo.class);
            startActivity(Edit);
        }else if (id == R.id.activity_About_Us){
            Intent about = new Intent(MainActivity.this,About_us.class);
            startActivity(about);
        }else if (id == R.id.Log_Out_Tab){
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent i = new Intent(MainActivity.this,LogIn.class);
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
        cameraView.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
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
                                Toast.makeText(MainActivity.this, "Ml exeception", Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(MainActivity.this, "Not downloaded", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: "+e );
                Toast.makeText(MainActivity.this, "Error "+e, Toast.LENGTH_SHORT).show();
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

                switch (lab){
                    case "ALEF":
                        convertedText.append("أ");
                        text = text + "a";
                        break;
                    case "BAA":
                        convertedText.append("ب");
                        text = text + "b";
                        break;
                    case "HAA":
                        convertedText.append("ح");
                        text = text + "h";
                        break;
                    case "RA":
                        convertedText.append("ر");
                        text = text + "r";
                        break;
                    case "FA":
                        convertedText.append("ف");
                        text = text + "f";
                        break;
                    case "KAF":
                        convertedText.append("ك");
                        text = text + "k";
                        break;
                    case "MEM":
                        convertedText.append("م");
                        text = text + "m";
                        break;
                    case "NON":
                        convertedText.append("ن");
                        text = text + "n";
                        break;
                    case "WAW":
                        convertedText.append("و");
                        text = text + "o";
                        break;
                    case "YA":
                        convertedText.append("ي");
                        text = text + "e";
                        break;
                }

                waitingDialog.cancel(); //stop the dialog
                cameraView.start(); //Start the camera

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(MainActivity.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
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

    //blocking back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.valueOf(android.os.Build.VERSION.SDK) < 7
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        return;
    }
}