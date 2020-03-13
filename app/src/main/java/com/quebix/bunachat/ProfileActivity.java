package com.quebix.bunachat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

public class ProfileActivity extends AppCompatActivity {

    private final static String TAG = "ProfileActivity";
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus;
    private Button mProfileSendRequest;

    private DatabaseReference mUsersDatabase, mFriendReqDatabase, mRootUserRef;
    private DatabaseReference mNotificationDatabase, mRootRef;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;
    private String gender, userSex, oSex;

    private int mCurrent_state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = mRootRef.child("Users").child(firebaseUser.getUid());
        mNotificationDatabase = mRootRef.child("notifications");
        mFriendReqDatabase = mRootRef.child("Friend_req");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootUserRef = mRootRef.child(mCurrentUser.getUid());

        mProfileImage = findViewById(R.id.current_user_image);
        mProfileName = findViewById(R.id.current_user_name);
        mProfileStatus = findViewById(R.id.current_user_status);
        mProfileSendRequest = findViewById(R.id.btn_request);



        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while user data loads");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ");
                final String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).into(mProfileImage);
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });


        mProfileSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendRequest.setEnabled(false);

                //----------------SEND REQUEST STATE-------------------
                if(mCurrent_state == 0){

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationsData = new HashMap<>();
                    notificationsData.put("from", mCurrentUser.getUid());
                    notificationsData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() +"/"+user_id  + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id+ "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationsData);
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this, getString(R.string.error_try_again), LENGTH_SHORT).show();
                            }

                            mProfileSendRequest.setEnabled(true);
                            mCurrent_state = 1;

                        }
                    });
                }

                //----------------CANCEL REQUEST STATE-------------------
                if(mCurrent_state == 1){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequest.setEnabled(true);
                                    mCurrent_state = 0;

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.failed_cancelling), Toast.LENGTH_LONG).show();
                            mProfileSendRequest.setEnabled(true);
                        }
                    });
                }

                //----------------REQUEST RECEIVED STATE-------------------
                if(mCurrent_state == 3){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/"+ mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendMap.put("Friends/"+ user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendMap.put("Friend_req/" + mCurrentUser.getUid()+ "/" + user_id, null);
                    friendMap.put("Friend_req/" + user_id+ "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this, getString(R.string.error_try_again), LENGTH_SHORT).show();
                                mProfileSendRequest.setEnabled(true);
                            }

                            mProfileSendRequest.setEnabled(true);
                            mCurrent_state = 4;
                        }
                    });
                }

                //----------------UNFRIEND A PERSON-------------------
                if(mCurrent_state == 4){
                    AlertDialog.Builder unfriend = new AlertDialog.Builder(ProfileActivity.this);
                    unfriend.setTitle(getString(R.string.unfriend))
                            .setMessage(getString(R.string.sure_to_unfriend))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Map unfriendMap = new HashMap();
                                    unfriendMap.put("Friends/"+ mCurrentUser.getUid() + "/" + user_id, null);
                                    unfriendMap.put("Friends/"+ user_id + "/" + mCurrentUser.getUid(), null);

                                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if(databaseError != null){
                                                Toast.makeText(ProfileActivity.this, getString(R.string.error_try_again), LENGTH_SHORT).show();
                                                mProfileSendRequest.setEnabled(true);
                                            }

                                            mProfileSendRequest.setEnabled(true);
                                            mCurrent_state = 0;
                                        }
                                    });
                                }
                            }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog unfriedDialog = unfriend.create();
                    unfriedDialog.show();

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootUserRef.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRootUserRef.child("online").setValue("false");
    }
}
