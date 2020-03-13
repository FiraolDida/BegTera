package com.quebix.bunachat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quebix.bunachat.Activity.MessageActivity;
import com.quebix.bunachat.Model.Chat;
import com.quebix.bunachat.Model.User;
import com.quebix.bunachat.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "UserAdapter";
    private Context context;
    private List<User> userList;
    private boolean isChat;
    private String _lastMessage, isSeen, senderID;

    public UserAdapter(Context context, List<User> userList, boolean isChat) {
        this.context = context;
        this.userList = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item, viewGroup,
                false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user = userList.get(i);
        viewHolder.username.setText(user.getUsername());

        if (user.getImage().equals("default")){
            viewHolder.profilePicture.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(user.getImage()).into(viewHolder.profilePicture);
        }

        if (isChat){
            lastMessage(user.getId(), viewHolder.lastMessage);
        } else {
            viewHolder.lastMessage.setVisibility(View.GONE);
        }

        setCounter(viewHolder.unreadMessage);

        Log.d(TAG, "user status: " + user.getUsername());
        Log.d(TAG, "user status: " + user.getStatus().equals("online"));
        if (isChat){
            if (user.getStatus().equals("true")){
                Log.d(TAG, "online");
                viewHolder.onlineStatusImage.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "offline");
                viewHolder.onlineStatusImage.setVisibility(View.GONE);
            }
        } else {
            viewHolder.onlineStatusImage.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userID", user.getId());
                intent.putExtra("name", user.getUsername());
                intent.putExtra("profileImage", user.getImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username, lastMessage, unreadMessage;
        public ImageView profilePicture;
        public ImageView onlineStatusImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.usernameUF);
            profilePicture = itemView.findViewById(R.id.profileImageUF);
            onlineStatusImage = itemView.findViewById(R.id.onlineStatusImage);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            unreadMessage = itemView.findViewById(R.id.unreadMessage);
        }
    }

    private void lastMessage(final String userID, final TextView lastMessage){
        _lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(userID) || chat.getReceiver().equals(userID) &&
                            chat.getSender().equals(firebaseUser.getUid())){
                                _lastMessage = chat.getMessage();
                                isSeen = chat.getIsSeen();
                                senderID = chat.getSender();
                        }
                    }
                    switch (_lastMessage){
                        case "default":
                            lastMessage.setText("Say hi");
                            lastMessage.setTextColor(Color.WHITE);
                            break;
                        default:
                            lastMessage.setText(_lastMessage);
                            if (isSeen.equals("false") && !senderID.equals(firebaseUser.getUid())){
                                lastMessage.setTextColor(Color.GRAY);
                            } else {
                                lastMessage.setTextColor(Color.WHITE);
                            }
                    }
                    _lastMessage = "default";
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }

    private void setCounter(final TextView unreadMessage){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    try {
                        if (chat.getReceiver().equals(currentUser.getUid()) &&
                                chat.getIsSeen().equals("false")){
                            unread++;
                        }
                    }catch (Exception e){
                        Log.d(TAG, "onDataChange: " + e.getMessage());
                    }
                }

                if (unread == 0){
                    Log.d(TAG, "unread: " + " == " + 0);
                    unreadMessage.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "unread: " + " == " + unread);
                    unreadMessage.setVisibility(View.VISIBLE);
                    unreadMessage.setText(String.valueOf(unread));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });
    }
}
