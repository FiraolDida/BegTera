package com.quebix.bunachat.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.HashMap;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "AlarmReceiver";
    private String userSex, oSex, currentUser;
    private DatabaseReference databaseReference;
    private LocalDate localDate;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        localDate = LocalDate.now();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().
                child("MatchupInfo");

        Log.d(TAG, "clearRejectedUser: " + localDate.minusDays(7));
        usersGender();
    }

    private void usersGender(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUserGender: " + firebaseUser.getUid());

        DatabaseReference maleReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Male");

        maleReference.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(firebaseUser.getUid())){
                    userSex = "Male";
                    oSex = "Female";
                    Log.d(TAG, "userSex: " + userSex + ", oSex: " + oSex);
                    handleRejectedUser(oSex);
                    handleForToday(userSex);
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(firebaseUser.getUid())){
                    userSex = "Female";
                    oSex = "Male";
                    Log.d(TAG, "userSex: " + userSex + ", oSex: " + oSex);
                    handleRejectedUser(oSex);
                    handleForToday(userSex);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleRejectedUser(String oSex){
        final LocalDate localDate = LocalDate.now();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(oSex);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if (snapshot.hasChild("MatchupInfo")){
                            String date = String.valueOf(snapshot.child("MatchupInfo")
                                    .child("Rejected").child(currentUser).child("date").getValue());
                            Log.d(TAG, "date: " + date);
                            Log.d(TAG, "date past 7: " + localDate.minusDays(7));
                            if (date.equals(String.valueOf(localDate.minusDays(7)))){
                                Log.d(TAG, "Removed: " + snapshot.child("MatchupInfo")
                                        .child("Rejected").child(currentUser).getRef().removeValue());
                            }
                        }
                    }

                } else {
                    Log.d(TAG, "datasnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleForToday(String gender){
        final LocalDate localDate = LocalDate.now();
        final HashMap<String, Object> forToady = new HashMap<>();
        forToady.put("forToday", "false");

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(gender).child(currentUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "handleForToday: " + localDate.minusDays(1));
                if (dataSnapshot.exists() && dataSnapshot.hasChild("date")){
                    Log.d(TAG, "in here: " + dataSnapshot.hasChild("date"));
                    String date = dataSnapshot.child("date").getValue(String.class);
                    Log.d(TAG, "handleForToday date: " + date);

                    if (date.equals(String.valueOf(localDate.minusDays(1)))){
                        Log.d(TAG, "handleForTodayyyy");

                        reference.updateChildren(forToady)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "handleForTodayyyy: success");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }
}
