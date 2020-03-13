package com.quebix.bunachat.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quebix.bunachat.Model.Matchup;
import com.quebix.bunachat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MatchupAdapter extends ArrayAdapter<Matchup> {

    private final static String TAG = "MatchupAdapter";
    private Context context;
    private TextView nameView, locationView;
    private ImageView userImage;

    public MatchupAdapter(@NonNull Context context, int resource, @NonNull List<Matchup> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Matchup matchup = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.matchup_item, parent, false);
        }

        nameView = convertView.findViewById(R.id.fullName);
        locationView = convertView.findViewById(R.id.userLocation);
        userImage = convertView.findViewById(R.id.imageViewMUI);

        nameView.setText(matchup.getFullName() + ", " + matchup.getAge());
        locationView.setText(matchup.getLocation());

        if (!matchup.getImage().equals("default")){
            Picasso.with(context).load(matchup.getImage()).fit().centerCrop().into(userImage);
        }

        return convertView;
    }
}
