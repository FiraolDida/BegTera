package com.quebix.bunachat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mEditStatus;
    private ImageButton btnUpdate;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
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
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingIntent);
                            finish();
                        }else{
                            Toast.makeText(StatusActivity.this, getString(R.string.update_error_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }
}
