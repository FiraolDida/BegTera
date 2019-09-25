package com.quebix.bunachat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView btnAlready = findViewById(R.id.btn_already);

        ImageButton btnCreate = findViewById(R.id.btn_create);

        mProgressDialog = new ProgressDialog(this);

        final EditText fieldName = findViewById(R.id.name_reg_field);
        final EditText fieldEmail = findViewById(R.id.email_reg_field);
        final EditText fieldPass = findViewById(R.id.pass_reg_field);
        final EditText fieldConfPass = findViewById(R.id.confPass_reg_field);

        final EditText fieldGender = findViewById(R.id.gender_reg_field);

        mAuth = FirebaseAuth.getInstance();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(RegisterActivity.this, StartActivity.class);
                startActivity(startIntent);
                finish();
            }
        });


        btnAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alreadyIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                alreadyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(alreadyIntent);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = fieldName.getText().toString();
                String email = fieldEmail.getText().toString();
                String password = fieldPass.getText().toString();
                String confPass = fieldConfPass.getText().toString();
                String gender = fieldGender.getText().toString();

                if(display_name.length()!=0 && email.length()!=0 && password.length()!=0 &&
                        confPass.length()!=0 && gender.length() != 0){
                    if(password.equals(confPass)){
                        mProgressDialog.setTitle(getString(R.string.reging_user));
                        mProgressDialog.setMessage(getString(R.string.reg_message));
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        registerUser(display_name, email, password, gender);
                    }else{
                        Toast.makeText(RegisterActivity.this, getString(R.string.pw_noMatch), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, getString(R.string.reg_error1), Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void registerUser(final String display_name, String email, String password, final String gender) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();


                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(gender).child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there, I'm using BunaChat");
                            userMap.put("image", "default");
                            userMap.put("online", "true");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });



                        } else {
                            mProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, getString(R.string.auth_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
