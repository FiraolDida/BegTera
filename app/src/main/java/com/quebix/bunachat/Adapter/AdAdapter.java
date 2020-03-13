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

import com.quebix.bunachat.Model.Ad;
import com.quebix.bunachat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdAdapter extends ArrayAdapter<Ad> {

    private final static String TAG = "AdAdapter";
    private Context context;
    private TextView nameView;
    private ImageView userImage;

    public AdAdapter(@NonNull Context context, int resource, @NonNull List<Ad> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ad ad = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.ad_item, parent, false);
        }

        nameView = convertView.findViewById(R.id.adName);
        userImage = convertView.findViewById(R.id.adImageView);

        nameView.setText(ad.getName());
        Picasso.with(context).load(ad.getImage()).fit().centerCrop().into(userImage);

        return convertView;
    }
}
