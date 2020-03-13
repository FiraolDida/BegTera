package com.quebix.bunachat;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserDatabase;
    private EditText fieldEmail, fieldPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        fieldEmail = findViewById(R.id.login_email_field);
        fieldPass = findViewById(R.id.login_pass_field);
        Button btnLogin = findViewById(R.id.login_btn);
        Button registerBtn = findViewById(R.id.registerBtn);
        Button backBtn = findViewById(R.id.backButton);
        mProgressDialog = new ProgressDialog(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                createIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(createIntent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String email = fieldEmail.getText().toString();
            String pass = fieldPass.getText().toString();

            if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(pass)){
                mProgressDialog.setTitle(getString(R.string.logging_in));
                mProgressDialog.setMessage(getString(R.string.wait_login_message));
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                authenticate(email, pass);
            }else{
                Toast.makeText(LoginActivity.this, getString(R.string.login_error1), Toast.LENGTH_SHORT).show();
            }
            }
        });

    }

    private void authenticate(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String currentUser = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child("DeviceTokens").child(currentUser).child("device_token").setValue(deviceToken)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.auth_error),
                            Toast.LENGTH_SHORT).show();
                    fieldEmail.setText(null);
                    fieldPass.setText(null);
                    fieldPass.clearFocus();
                    mProgressDialog.dismiss();
                }
            }
        });
    }
}
