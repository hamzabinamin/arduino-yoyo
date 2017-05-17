package com.hamzabinamin.yoyo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Hamza on 12/14/2016.
 */

public class CustomAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    Context context;
    public static  List<Player> list = null;

    public CustomAdapter(Context context, List<Player> l) {

        this.context = context;
        this.list = l;
     //   this.filtered_list = l;
        mInflater = LayoutInflater.from(context);
    }

    public void add(Player player) {
       // filtered_list.add(alarm);
        list.add(player);
        notifyDataSetChanged();
    }



    public double getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x + y);

        return screenInches;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public static Player getAlarmItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            if(getScreenSize() <= 4)
                convertView = mInflater.inflate(R.layout.list_view_item_small, parent, false);
            else
                convertView = mInflater.inflate(R.layout.list_view_item, parent, false);
            holder = new ViewHolder();
            holder.playerPlace = (TextView) convertView.findViewById(R.id.placeTextView);
            holder.deviceName = (TextView) convertView.findViewById(R.id.deviceNameTextView);
            holder.playerTimeStamp = (TextView) convertView.findViewById(R.id.timeStampTextView);
            holder.playerScore = (TextView) convertView.findViewById(R.id.scoreTextView);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }
            holder.playerPlace.setText(String.valueOf(list.get(position).getPlace()));
            holder.deviceName.setText(list.get(position).getDeviceName());
            holder.playerTimeStamp.setText(list.get(position).getTimeStamp());
            holder.playerScore.setText(String.valueOf(list.get(position).getScore()));
        return convertView;
    }

    private class ViewHolder {
        TextView playerPlace;
        TextView deviceName;
        TextView playerTimeStamp;
        TextView playerScore;
    }



}
