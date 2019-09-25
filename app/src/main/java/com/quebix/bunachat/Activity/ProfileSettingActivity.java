package com.quebix.bunachat.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quebix.bunachat.MainActivity;
import com.quebix.bunachat.R;
import com.quebix.bunachat.Users;

import java.util.HashMap;

public class ProfileSettingActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "ProfileSettingActivity";
    private Toolbar toolbar;
    private Spinner spinner;
    private EditText displayName, ageText;
    private String name, age, selectedLookingFor, selectedLocation, currentUser, interestedIn
            , gender;
    private ImageButton saveButton, cancelButton;
    private RadioGroup lookingFor, location;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.profileSettingToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Setting");

        currentUserGender();

        spinner = findViewById(R.id.spinner);
        setSpinner();
        displayName = findViewById(R.id.displayName);
        ageText = findViewById(R.id.age);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        lookingFor = findViewById(R.id.lookingForRadioGroup);
        location = findViewById(R.id.locationRadioGroup);

        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        lookingFor.setOnCheckedChangeListener(this);
        location.setOnCheckedChangeListener(this);
    }

    private void setSpinner() {
        String[] arraySpinner = new String[] {"Select one", "Female", "Male"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                saveToDatabase();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
            case R.id.cancelButton:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i){
            case R.id.hookupRadioButton:
                selectedLookingFor = "Hookup";
                break;
            case R.id.RelationshipRadioButton:
                selectedLookingFor = "Relationship";
                break;
            case R.id.friendshipRadioButton:
                selectedLookingFor = "Friendship";
                break;
            case R.id.nearbyRadioButton:
                selectedLocation = "Nearby";
                break;
            case R.id.remoteRadioButton:
                selectedLocation = "Remote";
                break;
            case R.id.anywhereRadioButton3:
                selectedLocation = "Anywhere";
                break;
        }
    }

    private void saveToDatabase() {
        name = displayName.getText().toString();
        age = ageText.getText().toString();
        interestedIn = spinner.getSelectedItem().toString();

        Log.d(TAG, "Name: " + name + "\nage: " + age + "\nlooking for: " + selectedLookingFor
            + "\nlocation: " + selectedLocation + "\nInterestedIn: " + interestedIn);

        Users userProfile = new Users(
                name, age, selectedLookingFor, interestedIn, selectedLocation
        );
        HashMap<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("age", age);
        profile.put("lookingFor", selectedLookingFor);
        profile.put("interestedIn", interestedIn);
        profile.put("location", selectedLocation);
        profile.put("forToday", "false");

        databaseReference.child(gender).child(currentUser).updateChildren(profile)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "onComplete: success");
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException());
                    }
                }
            });
    }

    private void currentUserGender(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("Male").hasChild(firebaseUser.getUid())){
                        gender = "Male";
                        Log.d(TAG, "onDataChange: " + gender);
                    } else {
                        gender = "Female";
                        Log.d(TAG, "onDataChange: " + gender);
                    }
                } else {
                    Log.d(TAG, "dataSnapShot does not exist: ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
