package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPass extends AppCompatActivity {

    //declare all variables
    EditText Ereset;
    Button RB;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass);

        //Getting values from screen
        Ereset = findViewById(R.id.emailReset);
        RB = findViewById(R.id.resetB);

        //connect to firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        //button reset password
        RB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Send password reset link to email
                mFirebaseAuth.sendPasswordResetEmail(Ereset.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    new KAlertDialog(ForgetPass.this, KAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("A reset link is sent to your Email").confirmButtonColor(R.color.darkBlue)
                                            .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                                @Override
                                                public void onClick(KAlertDialog kAlertDialog) {
                                                    Intent i = new Intent(ForgetPass.this,LogIn.class);
                                                    startActivity(i);
                                                }
                                            }).show();
                                }
                                else{
                                    new KAlertDialog(ForgetPass.this, KAlertDialog.ERROR_TYPE)
                                            .setTitleText(task.getException().getMessage()).confirmButtonColor(R.color.darkBlue)
                                            .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                                @Override
                                                public void onClick(KAlertDialog kAlertDialog) {
                                                    Intent i = new Intent(ForgetPass.this,LogIn.class);
                                                    startActivity(i);
                                                }
                                            }).show();
                                }
                            }
                        });
            }
        });
    }
}