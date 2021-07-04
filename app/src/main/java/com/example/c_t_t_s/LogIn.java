package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity{

    //Declare all variables
    public EditText userEmail , userPassword;
    public Button loginButton;
    public TextView createAccount, aboutUs ,forgetPass;

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFireAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Get variables values
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.loginButton);
        createAccount = findViewById(R.id.createAccount);
        forgetPass = findViewById(R.id.forgetPassword);
        aboutUs = findViewById(R.id.aboutUs);

        //Connect to firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Auto login if email is verified and signed in before
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if(mFirebaseAuth.getCurrentUser().isEmailVerified()){
                Intent i = new Intent(LogIn.this,MainActivity.class);
                startActivity(i);
            }
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //get values form screen
           String email = userEmail.getText().toString();
           String pwd = userPassword.getText().toString();

           //check that the information entered are valid
            if(email.isEmpty() && pwd.isEmpty()) {
                new KAlertDialog(LogIn.this, KAlertDialog.ERROR_TYPE)
                        .setTitleText("Fields are Empty!").confirmButtonColor(R.color.darkBlue)
                        .show();
                userEmail.requestFocus();
                userPassword.requestFocus();
            }else if(email.isEmpty()){
                userEmail.setError("Please Enter Email");
                userEmail.requestFocus();
            }else if (!email.contains("@")){
                userEmail.setError("Incorrect Email");
                userEmail.requestFocus();
            }else if(pwd.isEmpty()){
                userPassword.setError("Please Enter Password");
                userPassword.requestFocus();
            }else{
                mFirebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        new KAlertDialog(LogIn.this, KAlertDialog.ERROR_TYPE)
                                .setTitleText("Account Login failed!").setContentText("Check your Login information or Create Account").confirmButtonColor(R.color.darkBlue)
                                .show();
                    }else{

                        //checking that email is verified
                        if(mFirebaseAuth.getCurrentUser().isEmailVerified()){
                            Intent i = new Intent(LogIn.this,MainActivity.class);
                            startActivity(i);
                        }else{
                            new KAlertDialog(LogIn.this, KAlertDialog.ERROR_TYPE)
                                    .setTitleText("Please Verify your Email!").confirmButtonColor(R.color.darkBlue)
                                    .show();
                        }
                    }
                    }
                });
            }
            }
        });

        //Redirect to aboutUs Screen
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LogIn.this, About_us.class);
                startActivity(i);
            }
        });

        //Redirect to createAccount Screen
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LogIn.this, CreateAcount.class);
                startActivity(i);
            }
        });

        //Redirect to forgetPass Screen
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LogIn.this, ForgetPass.class);
                startActivity(i);
            }
        });
    }
}
