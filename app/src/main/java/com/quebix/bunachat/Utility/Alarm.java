package com.quebix.bunachat.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;

public class Alarm extends BroadcastReceiver {
    public static final String TAG = "Alarm";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        LocalDate localDate = LocalDate.now();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                child("MatchupInfo");

        Log.d(TAG, "clearRejectedUser: " + localDate.minusDays(7));

        Query query = databaseReference.child(currentUser).child("Rejected")
                .orderByChild("date")
                .equalTo(String.valueOf(localDate.minusDays(7)));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Log.d(TAG, "onDataChange: can delete");
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        ds.getRef().removeValue();
                    }
                } else {
                    Log.d(TAG, "onDataChange: no data to delete");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }
}
