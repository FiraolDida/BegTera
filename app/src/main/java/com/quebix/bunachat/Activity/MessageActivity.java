package com.quebix.bunachat.Activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.quebix.bunachat.Adapter.MessageAdapter;
import com.quebix.bunachat.Model.Chat;
import com.quebix.bunachat.Model.User;
import com.quebix.bunachat.Notification.Client;
import com.quebix.bunachat.Notification.Data;
import com.quebix.bunachat.Notification.MyResponse;
import com.quebix.bunachat.Notification.Sender;
import com.quebix.bunachat.Notification.Token;
import com.quebix.bunachat.R;
import com.quebix.bunachat.Utility.APIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private CircleImageView profileImage;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;
    private ImageButton sendMessage;
    private EditText sendText;
    private MessageAdapter messageAdapter;
    private List<Chat> chatList;
    private RecyclerView recyclerView;
    private String userSex, oSex, userID;
    private ValueEventListener seenEventListener;
    private APIService apiService;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbarMA);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recyclerViewMA);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        profileImage = findViewById(R.id.profileImageMA);
        username = findViewById(R.id.usernameMA);
        sendMessage = findViewById(R.id.sendMessage);
        sendText = findViewById(R.id.sendText);

        intent = getIntent();
        userID = intent.getStringExtra("userID");
        String userPhoto = intent.getStringExtra("profileImage");
        String fullname = intent.getStringExtra("name");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        username.setText(fullname);
        if (userPhoto.equals("default")){
            profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(userPhoto).into(profileImage);
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = sendText.getText().toString();
                if (!message.equals("")){
                    sendMessage(firebaseUser.getUid(), userID, message);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message",
                            Toast.LENGTH_SHORT).show();
                }
                sendText.setText("");
            }
        });

        //TODO: Read from database
        readMessage(firebaseUser.getUid(), userID, userPhoto);
        seenMessage(userID);
    }

    private void seenMessage(final String userID){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                                chat.getSender().equals(userID)){
                            Log.d(TAG, "seenMessage: " + chat.getReceiver());
                            HashMap<String, Object> seenInfo = new HashMap<>();
                            seenInfo.put("isSeen", "true");
                            snapshot.getRef().updateChildren(seenInfo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("receiver", receiver);
        body.put("message", message);
        body.put("isSeen", "false");

        reference.child("Chats").push().setValue(body);
        checkReceiver(sender, receiver);

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userSex)
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = new User(
                        dataSnapshot.getKey(),
                        dataSnapshot.child("name").getValue(String.class),
                        dataSnapshot.child("image").getValue(String.class),
                        dataSnapshot.child("online").getValue(String.class)
                );
                if (notify){
                    Log.d(TAG, "notify: ");
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void sendNotification(String receiver, final String username, final String msg) {
        DatabaseReference tokenReference = FirebaseDatabase.getInstance()
                .getReference("Users").child("DeviceTokens");
        Query query = tokenReference.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), username + ": " + msg,
                            "New Message", userID, R.mipmap.ic_launcher);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
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

    private void readMessage(final String myID, final String userID, final String imageURL){
        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chat chat = snapshot.getValue(Chat.class);
                        if (Objects.requireNonNull(chat).getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                                chat.getReceiver().equals(userID) && chat.getSender().equals(myID)){
                            chatList.add(chat);
                        }
                        messageAdapter = new MessageAdapter(MessageActivity.this, chatList,
                                imageURL);
                        recyclerView.setAdapter(messageAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void checkReceiver(final String sender, final String receiverID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child("MatchupNotifications").child(receiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            saveReceiver(sender, receiverID);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void saveReceiver(String sender, String receiverID){
        String message = "You have a match request";
        Map<String, Object> notificationInfo = new HashMap<>();
        notificationInfo.put("from", sender);
        notificationInfo.put("to", receiverID);
        notificationInfo.put("message", message);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child("MatchupNotifications").child(receiverID).push()
                .updateChildren(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void onStart(){
        super.onStart();
        usersGender("true");
        currentUser(userID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenEventListener);
        usersGender("false");
        currentUser("none");
    }

    private void usersGender(final String status){
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

    private void currentUser(String userID){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userID);
        editor.apply();

    }
}
