package com.quebix.bunachat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationManagerCompat;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.quebix.bunachat.Activity.ProfileSettingActivity;
import com.quebix.bunachat.Adapter.MatchupAdapter;
import com.quebix.bunachat.Model.Ad;
import com.quebix.bunachat.Model.Matchup;
import com.quebix.bunachat.Notification.Client;
import com.quebix.bunachat.Notification.Data;
import com.quebix.bunachat.Notification.MyResponse;
import com.quebix.bunachat.Notification.Sender;
import com.quebix.bunachat.Notification.Token;
import com.quebix.bunachat.Utility.APIService;
import com.quebix.bunachat.Utility.AlarmReceiver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchupFragment extends Fragment {

    private final static String TAG = "MatchupFragment";
    private CardView completeProfile;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String currentUser;

    //newMatchupFragment
    private SwipeFlingAdapterView flingContainer;
    private List<Matchup> matchupList, randomList;
    private Matchup matchup;
    private MatchupAdapter matchupAdapter;
    private String userSex, oSex, currentLocation, lookingFor;
    private int swipeCounter = 0, counter = 0;
    private TextView textView;
    private APIService apiService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RelativeLayout progressBar;

    private Ad ad;

//    private NotificationManagerCompat managerCompat;

    public MatchupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        managerCompat = NotificationManagerCompat.from(getContext());
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        usersGender();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_matchup, container, false);
        Log.d("TESTTEST", "onCreateView: ");
        flingContainer = view.findViewById(R.id.frame);
        completeProfile = view.findViewById(R.id.cardViewMF);
        textView = view.findViewById(R.id.dailyLimit);
        progressBar = view.findViewById(R.id.progressBarHolder);
        matchupList = new ArrayList<>();
        randomList = new ArrayList<>();
        matchup = new Matchup();
        matchupAdapter = new MatchupAdapter(getContext(), R.layout.matchup_item, matchupList);

        sharedPreferences = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
//        editor.putString("swipeCounter", String.valueOf(0));
//        editor.apply();

        view.findViewById(R.id.profileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileSettingActivity.class);
                startActivity(intent);
            }
        });

//        flingContainer.setAdapter(matchupAdapter);
//        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
//            @Override
//            public void removeFirstObjectInAdapter() {
//                // this is the simplest way to delete an object from the Adapter (/AdapterView)
//                Log.d("LIST", "removed object!");
//                matchupList.remove(0);
//                matchupAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onLeftCardExit(Object dataObject) {
//                swipeCounter++;
//                editor.putString("swipeCounter", String.valueOf(swipeCounter));
//                editor.apply();
//                Toast.makeText(getContext(), "swipe counter: " + swipeCounter
//                        , Toast.LENGTH_SHORT).show();
//                Matchup rejection = (Matchup) dataObject;
//                Toast.makeText(getContext(), "Left!", Toast.LENGTH_SHORT).show();
//                rejection(rejection);
//            }
//
//            @Override
//            public void onRightCardExit(Object dataObject) {
//                swipeCounter++;
////                editor.putString("swipeCounter", String.valueOf(swipeCounter));
////                editor.apply();
////                Matchup acceptance = (Matchup) dataObject;
////                Toast.makeText(getContext(), "Right!", Toast.LENGTH_SHORT).show();
////                acceptance(acceptance);
////                isConnectionMatch(acceptance.getUserId());
//            }
//
//            @Override
//            public void onAdapterAboutToEmpty(int itemsInAdapter) {
//                Log.d(TAG, "items in adapter: " + itemsInAdapter);
////                if (itemsInAdapter == 0){
////                    Log.d(TAG, "EMPTY");
////                    Toast.makeText(getContext(), "EMPTY", Toast.LENGTH_SHORT).show();
////                    dailyLimit.setVisibility(View.VISIBLE);
////                } else {
////                    setVisibility();
////                    Log.d(TAG, "NOT EMPTY");
////                    Toast.makeText(getContext(), "NOT EMPTY", Toast.LENGTH_SHORT).show();
////                }
//                Log.d(TAG, "swipe counter: " + swipeCounter);
//                if (swipeCounter == 10){
//                    swipeCounter = 0;
//                    editor.putString("swipeCounter", String.valueOf(swipeCounter));
//                    editor.apply();
//                    Log.d(TAG, "EMPTY: " + swipeCounter);
//                    textView.setVisibility(View.VISIBLE);
//                    completeProfile.setVisibility(View.GONE);
//                    flingContainer.setVisibility(View.GONE);
//                    setDailyLimit();
//                } else if (itemsInAdapter == 0) {
////                    dailyLimit.setVisibility(View.VISIBLE);
//                    textView.setVisibility(View.VISIBLE);
//                    completeProfile.setVisibility(View.GONE);
//                    flingContainer.setVisibility(View.GONE);
//                } else {
//                    setVisibility();
//                    Log.d(TAG, "NOT EMPTY");
//                }
//            }
//
//            @Override
//            public void onScroll(float scrollProgressPercent) {
//            }
//        });

        Log.d(TAG, "swipe counter: " + sharedPreferences.getString("swipeCounter", "empty"));
        return view;
    }

    private void usersGender(){
        Log.d("TESTTEST", "usersGender: ");
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
                            Log.d(TAG, "onDataChange: " + dataSnapshot.child("forToday").getValue(String.class));

                            if (dataSnapshot.child("forToday").getValue(String.class).equals("false")){
                                currentLocation = dataSnapshot.child("location").getValue(String.class);
                                Log.d(TAG, "currentLocation: " + currentLocation);
                                lookingFor = dataSnapshot.child("lookingFor").getValue(String.class);
                                Log.d(TAG, "lookingFor: " + lookingFor);
                                oppositeSexUsers();
                                setVisibility();
                            } else {
                                textView.setVisibility(View.VISIBLE);
                                completeProfile.setVisibility(View.GONE);
                                flingContainer.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d(TAG, "onDataChange: profile not completed");
                            completeProfile.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.GONE);
                            flingContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError);
                    }
                });
    }

    private void oppositeSexUsers(){
//        Query oppositeSexUser = FirebaseDatabase.getInstance().getReference()
//                .child("Users").child(oSex).orderByChild("name").limitToFirst(10);
        Query oppositeSexUser = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(oSex);

        oppositeSexUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Add hasChildValidation
                if (dataSnapshot.hasChild("name") &&
                        dataSnapshot.hasChild("age") &&
                        dataSnapshot.hasChild("interestedIn") &&
                        dataSnapshot.hasChild("lookingFor") &&
                        dataSnapshot.hasChild("location")){

                    if (dataSnapshot.exists() &&
                            dataSnapshot.child("lookingFor").getValue(String.class).equals(lookingFor) &&
                            dataSnapshot.child("location").getValue(String.class).equals(currentLocation) &&
                            !dataSnapshot.child("MatchupInfo").child("Rejected").hasChild(currentUser) &&
                            !dataSnapshot.child("MatchupInfo").child("Accepted").hasChild(currentUser)){

                        matchup = new Matchup(
                                dataSnapshot.getKey(),
                                dataSnapshot.child("image").getValue(String.class),
                                dataSnapshot.child("name").getValue(String.class),
                                dataSnapshot.child("age").getValue(String.class),
                                dataSnapshot.child("location").getValue(String.class)
                        );

                        matchupList.add(matchup);
//                        randomList.add(matchup);
                        Log.d(TAG, "random list length: " + randomList.size());
                        matchupAdapter.notifyDataSetChanged();
                    }

//                    randomListCheck(randomList);
//                    progressBar.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    flingContainer.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "err");
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
        progressBar.setVisibility(View.GONE);
        setFlingContainer();
    }

    private void randomListCheck(List<Matchup> usersList) {
        Collections.shuffle(usersList);
//        matchupList = usersList;
//        matchupAdapter.notifyDataSetChanged();
        matchupList.clear();
        matchupAdapter.clear();
//        for (int i = usersList.size(); i > 10; i--){
//            usersList.remove(i-1);
//        }

        for (Matchup lists: usersList) {
            Log.d(TAG, "users list length " + usersList.size());
            Log.d(TAG, "lists full name: " + lists.getFullName());
            Log.d(TAG, "counter: " + counter);
            counter++;
            matchupAdapter.add(lists);
            matchupAdapter.notifyDataSetChanged();
        }
    }

    private void setVisibility(){
        completeProfile.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        flingContainer.setVisibility(View.VISIBLE);
    }

    private void rejection(Matchup rejection){
//        databaseReference.child(oSex).child(rejection.getUserId()).child("MatchupInfo")
//                .child("Rejected").child(currentUser).setValue(true);
        LocalDate localDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
        }

//        Matchup setDate = new Matchup(String.valueOf(localDate));
        HashMap<String, Object> matchupDate = new HashMap<>();
        matchupDate.put("date", String.valueOf(localDate));
        matchupDate.put("flag", true);

        databaseReference.child(oSex).child(rejection.getUserId()).child("MatchupInfo")
                .child("Rejected").child(currentUser).updateChildren(matchupDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: success");
                            setAlarmForRejection();
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
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

    private void storeNotification(final String receiverID){
        String message = "You have a match request";
        Map<String, Object> notificationInfo = new HashMap<>();
        notificationInfo.put("from", currentUser);
        notificationInfo.put("to", receiverID);
        notificationInfo.put("message", message);

        databaseReference.child("MatchupNotifications").child(receiverID).push()
                .updateChildren(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onComplete: notification stored");
                    sendNotification(receiverID);
                }else {
                    Log.d(TAG, "onComplete: notification not stored");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        Log.d("swipecounter", "swipe counter: " + sharedPreferences.getString("swipeCounter", "empty"));
        try {
            swipeCounter = Integer.parseInt(sharedPreferences.getString("swipeCounter", "empty"));
        } catch (Exception e){
            Log.d(TAG, "onResume: " + e.getMessage());
            swipeCounter = 0;
        }
        randomList.clear();
//        usersGender();
    }

    private void setAlarmForRejection(){
        AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent  = PendingIntent.getBroadcast(getActivity(), 1, intent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                60*1000,
                pendingIntent);
    }

    private void setDailyLimit(){
        LocalDate localDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
        }
        HashMap<String, Object> forToady = new HashMap<>();
        forToady.put("forToday", "true");
        forToady.put("date", String.valueOf(localDate));

        databaseReference.child(userSex).child(currentUser).updateChildren(forToady)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "setDailyLimit: success");
                            setAlarmForRejection();
                        } else {
                            Log.d(TAG, "setDailyLimit: " + task.getException());
                        }
                    }
                });
    }

    private void sendNotification(final String receiver) {
        Log.d(TAG, "sendNotification: in here");
        DatabaseReference tokenReference = FirebaseDatabase.getInstance()
                .getReference("Users").child("DeviceTokens");
        Query query = tokenReference.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUser, "You have a match request",
                            "Match Request", receiver, R.mipmap.ic_launcher);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: " + t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void setFlingContainer(){
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
                swipeCounter++;
                editor.putString("swipeCounter", String.valueOf(swipeCounter));
                editor.apply();
                Toast.makeText(getContext(), "swipe counter: " + swipeCounter
                        , Toast.LENGTH_SHORT).show();
                Matchup rejection = (Matchup) dataObject;
                Toast.makeText(getContext(), "Left!", Toast.LENGTH_SHORT).show();
                rejection(rejection);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                swipeCounter++;
                editor.putString("swipeCounter", String.valueOf(swipeCounter));
                editor.apply();
                Matchup acceptance = (Matchup) dataObject;
                Toast.makeText(getContext(), "Right!", Toast.LENGTH_SHORT).show();
                acceptance(acceptance);
                isConnectionMatch(acceptance.getUserId());
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.d(TAG, "items in adapter: " + itemsInAdapter);
                Log.d(TAG, "swipe counter: " + swipeCounter);
                if (swipeCounter == 10){
                    swipeCounter = 0;
                    editor.putString("swipeCounter", String.valueOf(swipeCounter));
                    editor.apply();
                    Log.d(TAG, "EMPTY: " + swipeCounter);
                    textView.setVisibility(View.VISIBLE);
                    completeProfile.setVisibility(View.GONE);
                    flingContainer.setVisibility(View.GONE);
                    setDailyLimit();
                } else if (itemsInAdapter == 0) {
                    textView.setVisibility(View.VISIBLE);
                    completeProfile.setVisibility(View.GONE);
                    flingContainer.setVisibility(View.GONE);
                } else {
                    setVisibility();
                    Log.d(TAG, "NOT EMPTY");
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });
    }

}
