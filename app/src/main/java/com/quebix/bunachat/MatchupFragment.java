package com.quebix.bunachat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.quebix.bunachat.Activity.ProfileSettingActivity;
import com.quebix.bunachat.Adapter.MatchupAdapter;
import com.quebix.bunachat.Model.Matchup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchupFragment extends Fragment {

    private final static String TAG = "MatchupFragment";
    private LinearLayout bottomNavbar;
    private CardView completeProfile, dailyLimit;
    private DatabaseReference databaseReference, matchDbReference;
    private FirebaseAuth firebaseAuth;
    private String currentUser;
    boolean flag = false;

    //newMatchupFragment
    private SwipeFlingAdapterView flingContainer;
    private List<Matchup> matchupList;
    private Matchup matchup;
    private MatchupAdapter matchupAdapter;
    private String userSex, oSex;

    private NotificationManagerCompat managerCompat;

    public MatchupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        managerCompat = NotificationManagerCompat.from(getContext());

        usersGender();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_matchup, container, false);
        flingContainer = view.findViewById(R.id.frame);
        bottomNavbar = view.findViewById(R.id.bottomNavBarLayout);
        completeProfile = view.findViewById(R.id.cardViewMF);
        dailyLimit = view.findViewById(R.id.enoughForTodayMF);
        matchupList = new ArrayList<>();
        matchup = new Matchup();
        matchupAdapter = new MatchupAdapter(getContext(), R.layout.matchup_item, matchupList);

        view.findViewById(R.id.profileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileSettingActivity.class);
                startActivity(intent);
            }
        });

        flingContainer.setAdapter(matchupAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                matchupList.remove(0);
                matchupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Matchup rejection = (Matchup) dataObject;
                Toast.makeText(getContext(), "Left!", Toast.LENGTH_SHORT).show();
                rejection(rejection);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Matchup acceptance = (Matchup) dataObject;
                Toast.makeText(getContext(), "Right!", Toast.LENGTH_SHORT).show();
                acceptance(acceptance);
                isConnectionMatch(acceptance.getUserId());
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.d(TAG, "items in adapter: " + itemsInAdapter);
                if (itemsInAdapter == 0){
                    Toast.makeText(getContext(), "EMPTY", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "NOT EMPTY", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void usersGender(){
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
                    checkProfileCompletion();
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
                    checkProfileCompletion();
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

    private void checkProfileCompletion() {
        databaseReference.child(userSex).child(currentUser)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "name: " + dataSnapshot.hasChild("name"));
                Log.d(TAG, "age: " + dataSnapshot.hasChild("age"));
                Log.d(TAG, "interest: " + dataSnapshot.hasChild("interestedIn"));
                Log.d(TAG, "lookingFor: " + dataSnapshot.hasChild("lookingFor"));
                Log.d(TAG, "location: " + dataSnapshot.hasChild("location"));
                if (dataSnapshot.hasChild("name") &&
                        dataSnapshot.hasChild("age") &&
                        dataSnapshot.hasChild("interestedIn") &&
                        dataSnapshot.hasChild("lookingFor") &&
                        dataSnapshot.hasChild("location")){
                    Log.d(TAG, "onDataChange: fully completed profile");
                    Log.d(TAG, "onDataChange: " + dataSnapshot.hasChild("forToday"));

                    if (dataSnapshot.child("forToday").getValue(String.class).equals("false")){
                        oppositeSexUsers();
                        setVisibility();
                    } else {
                        dailyLimit.setVisibility(View.VISIBLE);
                        completeProfile.setVisibility(View.GONE);
                        bottomNavbar.setVisibility(View.GONE);
                        flingContainer.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "onDataChange: profile not completed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void oppositeSexUsers(){
        DatabaseReference oppositeSexUser = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(oSex);

        oppositeSexUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() &&
                    !dataSnapshot.child("MatchupInfo").child("Rejected").hasChild(currentUser) &&
                    !dataSnapshot.child("MatchupInfo").child("Accepted").hasChild(currentUser)){
                    matchup = new Matchup(
                            dataSnapshot.getKey(),
                            dataSnapshot.child("image").getValue(String.class),
                            dataSnapshot.child("name").getValue(String.class),
                            dataSnapshot.child("age").getValue(String.class),
                            "Ethiopia"
                    );
                    matchupList.add(matchup);
                    matchupAdapter.notifyDataSetChanged();
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

    private void setVisibility(){
        completeProfile.setVisibility(View.GONE);
        dailyLimit.setVisibility(View.GONE);
        bottomNavbar.setVisibility(View.VISIBLE);
        flingContainer.setVisibility(View.VISIBLE);
    }

    private void rejection(Matchup rejection){
        databaseReference.child(oSex).child(rejection.getUserId()).child("MatchupInfo")
                .child("Rejected").child(currentUser).setValue(true);
    }

    private void acceptance(Matchup acceptance){
        databaseReference.child(oSex).child(acceptance.getUserId()).child("MatchupInfo")
                .child("Accepted").child(currentUser).setValue(true);
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference reference = databaseReference.child(userSex).child(currentUser)
                .child("MatchupInfo").child("Accepted").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(getContext(), "new connection", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "new connection: ");
                    Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                    databaseReference.child(oSex).child(dataSnapshot.getKey()).child("MatchupInfo")
                            .child("Matched").child(currentUser).setValue(true);

                    databaseReference.child(userSex).child(currentUser).child("MatchupInfo")
                            .child("Matched").child(dataSnapshot.getKey()).setValue(true);

                    storeNotification(dataSnapshot.getKey());
                }else {
                    Log.d(TAG, "is connection match dataSnapShot does not exist: ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void storeNotification(String receiverID){
        String message = "You have a match request";
        Map<String, Object> notificationInfo = new HashMap<>();
        notificationInfo.put("from", currentUser);
        notificationInfo.put("to", receiverID);
        notificationInfo.put("message", message);

        databaseReference.child("MatchupNotifications").child(receiverID)
                .setValue(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onComplete: notification stored");
                }else {
                    Log.d(TAG, "onComplete: notification not stored");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        matchupList.clear();
        Log.d(TAG, "onResume: ");
    }

}
