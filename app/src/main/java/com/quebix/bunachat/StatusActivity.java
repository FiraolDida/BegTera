package com.quebix.bunachat;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private final static String TAG = "StatusActivity";
    private Toolbar mToolbar;
    private EditText mEditStatus;
    private ImageButton btnUpdate;
    private FirebaseUser mCurrentUser;

    private ProgressDialog progressDialog;
    private String userSex, oSex, uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mCurrentUser.getUid();

        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.account_status));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditStatus = findViewById(R.id.status_input);
        btnUpdate = findViewById(R.id.btn_update);

        String status_value = getIntent().getStringExtra("status_value");
        mEditStatus.setText(status_value);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle(getString(R.string.saving_changes));
                progressDialog.setMessage(getString(R.string.wait_update_message));
                progressDialog.show();
                String status = mEditStatus.getText().toString();
                usersGender(status);
            }
        });


    }

    private void usersGender(final String status){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUserGender: " + firebaseUser.getUid());

        DatabaseReference maleReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Male");

        maleReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(firebaseUser.getUid())){
                    userSex = "Male";
                    oSex = "Female";
                    Log.d(TAG, "userSex: " + userSex + ", oSex: " + oSex);
                    updateStatus(status);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        DatabaseReference femaleReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Female");

        femaleReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(firebaseUser.getUid())){
                    userSex = "Female";
                    oSex = "Male";
                    Log.d(TAG, "userSex: " + userSex + ", oSex: " + oSex);
                    updateStatus(status);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void updateStatus(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userSex).child(uid);
        reference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }else{
                    Toast.makeText(StatusActivity.this, getString(R.string.update_error_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
