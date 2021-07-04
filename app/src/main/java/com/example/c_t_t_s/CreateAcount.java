package com.example.c_t_t_s;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class CreateAcount extends AppCompatActivity {

    //Declare all variables
    TextView birthdate;
    EditText fname, lname, email, phone, username, pass, cpass;
    Spinner gender;
    Button cAccount, choose;
    RadioGroup account;
    RadioButton admin, user;
    AccountDB AccountDB;
    FirebaseAuth mFirebaseAuth;

    private static final int CHOOSE_IMAGE = 1;
    private ImageView img;
    private Uri imgUrl;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private DatabaseReference mRef;
    private static final int LOCATION_REQUEST = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acount);

        //Get variables values
        birthdate = findViewById(R.id.userBirthdate);
        fname = findViewById(R.id.firstName);
        lname = findViewById(R.id.lastName);
        email = findViewById(R.id.userEmail);
        phone = findViewById(R.id.userPhone);
        username = findViewById(R.id.username);
        pass = findViewById(R.id.userPassword);
        cpass = findViewById(R.id.userConfirmPassword);
        gender = findViewById(R.id.userGender);
        cAccount = findViewById(R.id.addUserButton);
        admin = findViewById(R.id.adminAccount);
        account = findViewById(R.id.accountCategory);
        user = findViewById(R.id.userAccount);
        choose = findViewById(R.id.choosePhoto);
        img = findViewById(R.id.personalImg);

        //Connect to firebase
        mRef = FirebaseDatabase.getInstance().getReference().child("Accounts");
        AccountDB = new AccountDB();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("Accounts");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Accounts");

        //Setup Calendar
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);
        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(CreateAcount.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        birthdate.setText(date);
                    }
                },year,month,day);

                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
        });

        //choosing image to set as profile picture
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChoose();
            }
        });

        cAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //get user values
                final String firstName= fname.getText().toString();
                final String lastName= lname.getText().toString();
                final String UserEmail= email.getText().toString();
                final String userPhone= phone.getText().toString();
                final String Bdate= birthdate.getText().toString();
                final String userGender= gender.getSelectedItem().toString();
                final String UserName = username.getText().toString();
                final String password = pass.getText().toString();
                final String Cpassword = cpass.getText().toString();

                //check if account is made for Admin or User
                String Account="";
                int selectedRadioButtonID = account.getCheckedRadioButtonId();
                if (selectedRadioButtonID != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonID);
                    Account = selectedRadioButton.getText().toString();
                }
                final String aAccount = Account;

                //checking if all values entered from the user are valid
                if (firstName.isEmpty() && lastName.isEmpty() && UserEmail.isEmpty() && userPhone.isEmpty() &&
                        Bdate.isEmpty() && UserName.isEmpty() && password.isEmpty() && Cpassword.isEmpty()){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Fields are Empty!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    fname.requestFocus();
                    lname.requestFocus();
                    email.requestFocus();
                    phone.requestFocus();
                    birthdate.requestFocus();
                    username.requestFocus();
                    pass.requestFocus();
                    cpass.requestFocus();
                }else if (firstName.isEmpty() || lastName.isEmpty() || UserEmail.isEmpty() || userPhone.isEmpty() ||
                        Bdate.isEmpty() || UserName.isEmpty() || password.isEmpty() || Cpassword.isEmpty()){
                    if(firstName.isEmpty())
                        fname.requestFocus();
                    if(lastName.isEmpty())
                        lname.requestFocus();
                    if(UserEmail.isEmpty())
                        email.requestFocus();
                    if (userPhone.isEmpty())
                        phone.requestFocus();
                    if(Bdate.isEmpty())
                        birthdate.requestFocus();
                    if(UserName.isEmpty())
                        username.requestFocus();
                    if (password.isEmpty())
                        pass.requestFocus();

                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Fields are Not Complete!")
                            .setContentText("Please Fill your information").confirmButtonColor(R.color.darkBlue)
                            .show();
                }else if(!UserEmail.contains("@")){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Please Enter Valid Email!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    email.requestFocus();
                }else if ((userPhone.length()!=9 && userPhone.length()!=10) || (userPhone.length()==10 && userPhone.charAt(0)!='0') ||
                        (userPhone.length()==10 && userPhone.charAt(1)!='7') || (userPhone.length()==9 && userPhone.charAt(0)!='7')){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Incorrect Phone Number!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    phone.requestFocus();
                }else if (userGender.equals("Gender")){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Please Select your Gender!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    gender.requestFocus();
                }else if(password.length() <= 8 || password.length() >= 15 || password.contains(" ")){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Password Isn't Valid!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    pass.requestFocus();
                }else if (!(password.equals(Cpassword))){
                    new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Password Doesn't Match!").confirmButtonColor(R.color.darkBlue)
                            .show();
                    cpass.requestFocus();
                }else {
                    //create new Account on Database
                    mFirebaseAuth.createUserWithEmailAndPassword(UserEmail, password).addOnCompleteListener
                            (CreateAcount.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checking that the account is successfully created and the email doesn't exist
                            if (!task.isSuccessful()) {
                                new KAlertDialog(CreateAcount.this, KAlertDialog.ERROR_TYPE)
                                        .setTitleText("Account Creation failed").setContentText("the email already existed!")
                                        .confirmButtonColor(R.color.darkBlue).show();
                            } else {
                                //send email verification link
                                final FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();
                                mFirebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                        }else{
                                            Toast.makeText(CreateAcount.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                //uploading the profile picture to database
                                if (imgUrl != null) {
                                    final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imgUrl));

                                    mUploadTask = fileReference.putFile(imgUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    //storing all account information
                                                    AccountDB.setfName(firstName);
                                                    AccountDB.setlName(lastName);
                                                    AccountDB.setEmail(UserEmail);
                                                    AccountDB.setPhone(userPhone);
                                                    AccountDB.setBirthdate(Bdate);
                                                    AccountDB.setGender(userGender);
                                                    AccountDB.setUsername(UserName);
                                                    AccountDB.setAccount(aAccount);
                                                    AccountDB.setImage(uri.toString());
                                                    mRef.child(userInfo.getUid()).setValue(AccountDB);

                                                    new KAlertDialog(CreateAcount.this, KAlertDialog.SUCCESS_TYPE)
                                                            .setTitleText("Account Created Successfully").setContentText("A Verification Link has been sent to your Email")
                                                            .confirmButtonColor(R.color.darkBlue).setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                                        @Override
                                                        public void onClick(KAlertDialog kAlertDialog) {
                                                            Intent i = new Intent(CreateAcount.this, LogIn.class);
                                                            startActivity(i);
                                                        }
                                                    }).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(CreateAcount.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    //If client didn't choose a profile picture, storing the other information
                                    AccountDB.setfName(firstName);
                                    AccountDB.setlName(lastName);
                                    AccountDB.setEmail(UserEmail);
                                    AccountDB.setPhone(userPhone);
                                    AccountDB.setBirthdate(Bdate);
                                    AccountDB.setGender(userGender);
                                    AccountDB.setUsername(UserName);
                                    AccountDB.setAccount(aAccount);
                                    AccountDB.setImage(" ");

                                    mRef.child(userInfo.getUid()).setValue(AccountDB);

                                    new KAlertDialog(CreateAcount.this, KAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Account Created Successfully").setContentText("A Verification Link has been sent to your Email")
                                            .confirmButtonColor(R.color.darkBlue).setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                                        @Override
                                        public void onClick(KAlertDialog kAlertDialog) {
                                            Intent i = new Intent(CreateAcount.this, LogIn.class);
                                            startActivity(i);
                                        }
                                    }).show();
                                }
                            }
                        }
                    });
                }
            }
        });
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

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}