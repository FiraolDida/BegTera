package com.quebix.bunachat;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quebix.bunachat.Activity.AboutActivity;
import com.quebix.bunachat.Fragment.UsersFragment;
import com.quebix.bunachat.Model.Chat;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

//    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
//    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;

    private String userSex, oSex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth =FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            mUserRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("DeviceTokens").child(currentUserId);
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ViewPager mViewPager = findViewById(R.id.view_pager);
        final TabLayout mTabLayout = findViewById(R.id.tabLayout);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFrag(new MatchupFragment(),"");
        mSectionsPagerAdapter.addFrag(new UsersFragment(),"");
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.matches);
        mTabLayout.getTabAt(1).setIcon(R.drawable.chats);

//        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Chats");
//        databaseReference1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//                int unread = 0;
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Chat chat = snapshot.getValue(Chat.class);
//                    try {
//                        if (chat.getReceiver().equals(currentUser.getUid()) &&
//                                chat.getIsSeen().equals("false")){
//                            unread++;
//                        }
//                    }catch (Exception e){
//                        Log.d(TAG, "onDataChange: " + e.getMessage());
//                    }
//                }
//
//                mSectionsPagerAdapter.addFrag(new MatchupFragment(),"");
//                mSectionsPagerAdapter.addFrag(new UsersFragment(),"");
////                if (unread == 0){
////                    mSectionsPagerAdapter.addFrag(new UsersFragment(),"");
////                } else {
////                    mSectionsPagerAdapter.addFrag(new UsersFragment(),"("+unread+")");
////                }
//
//                mViewPager.setAdapter(mSectionsPagerAdapter);
//
//                mTabLayout.setupWithViewPager(mViewPager);
//
//                mTabLayout.getTabAt(0).setIcon(R.drawable.matches);
//                mTabLayout.getTabAt(1).setIcon(R.drawable.chats);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: " + databaseError);
//            }
//        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if(currentUser == null){
            sendToStart();
        }
        else{
//            mUserRef.child("online").setValue("true");
            usersGender("true");
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(currentUser!= null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//            usersGender("offline");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentUser!= null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            usersGender("false");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.btn_account_settings){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.aboutSetting){
            Intent usersIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(usersIntent);
        }
        if(item.getItemId() == R.id.btn_logout){
            setStatus(userSex, "false");
            FirebaseAuth.getInstance().signOut();
            removeDeviceToken();
            sendToStart();
        }


        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        finish();
    }

    private void removeDeviceToken(){
        String currentUserId = currentUser.getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("DeviceTokens").child(currentUserId);
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void usersGender(final String status){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            Log.d(TAG, "currentUserUid: " + firebaseUser.getUid());

            DatabaseReference maleReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Male");

            maleReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getKey().equals(firebaseUser.getUid())){
                        userSex = "Male";
                        oSex = "Female";
                        Log.d(TAG, "userSex: " + userSex + ", oSex: " + oSex);
                        setStatus(userSex, status);
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
                        setStatus(userSex, status);
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
        }catch (Exception e){
            Log.d(TAG, "usersGender: " + e.getMessage());
        }
    }

    private void setStatus(String gender, String status){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUserUid: " + firebaseUser.getUid());

        HashMap<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("online", status);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(gender).child(firebaseUser.getUid());
        reference.updateChildren(statusInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: online status updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: online status not updated " + e.getMessage());
                    }
                });
    }
}
