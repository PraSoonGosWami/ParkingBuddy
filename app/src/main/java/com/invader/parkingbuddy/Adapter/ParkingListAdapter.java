package com.invader.parkingbuddy.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.invader.parkingbuddy.Fragments.ViewLocationFragment;
import com.invader.parkingbuddy.Model.ParkingLocation;
import com.invader.parkingbuddy.R;
import com.location.aravind.getlocation.GeoLocator;

import java.util.ArrayList;

public class ParkingListAdapter extends RecyclerView.Adapter<ParkingListAdapter.ViewHolder> {
    private ArrayList<ParkingLocation> LocationAdapterList;
    private Context context;
    private Activity activity;



    public ParkingListAdapter(ArrayList<ParkingLocation> LocationAdapterList, Context context,Activity activity) {
        this.LocationAdapterList = LocationAdapterList;
        this.context = context;
        this.activity = activity;
        }


    @NonNull
    @Override
    public ParkingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.model_areas, viewGroup, false);
        return new ParkingListAdapter.ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull final ParkingListAdapter.ViewHolder holder, int i) {
        final ParkingLocation list = LocationAdapterList.get(i);


        Glide.with(context).
                asBitmap().
                load(list.getMapURL())
                .into(holder.location_image);



        holder.location_title.setText(list.getName());
        holder.location_distance.setText(getDistance(list.getLatitude(),list.getLongitude()));
        holder.location_address.setText(list.getAddress());
        holder.location_slots.setText("Slot Available: "+list.getAvailableSlots()+"/"+list.getTotalSlots());
        holder.location_cost.setText(list.getCost());
        holder.itemView.setOnClickListener(v->{
            swapFragments(new ViewLocationFragment(),list.getLocationid());
        });
        String pos = "geo:"+list.getLatitude()+","+list.getLongitude()+"?q="+list.getLatitude()+","+list.getLongitude();

        holder.location_image.setOnClickListener(v->{
            Uri gmmIntentUri = Uri.parse(pos);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });


    }

    @Override
    public int getItemCount() {
        return LocationAdapterList.size();
        }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView location_image;
        public TextView location_title,location_distance,location_address,location_slots,location_cost;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location_image = itemView.findViewById(R.id.location_image);

            location_title = itemView.findViewById(R.id.location_title);
            location_distance = itemView.findViewById(R.id.location_distance);
            location_address = itemView.findViewById(R.id.location_address);
            location_slots = itemView.findViewById(R.id.location_slots);
            location_cost = itemView.findViewById(R.id.location_cost);


        }
    }


    //replaces the container with fragments and sends category id
    public void swapFragments(Fragment fragment, String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        fragment.setArguments(bundle);
        android.support.v4.app.FragmentTransaction fragmentTransaction;
        fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frame_container, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }



    public String getDistance(double lat, double lon){
        GeoLocator geoLocator = new GeoLocator(context,activity);
        LatLng pos = new LatLng(geoLocator.getLattitude(),geoLocator.getLongitude());
        float [] res = new float[1];

        Location.distanceBetween(pos.latitude,pos.longitude,
                lat,lon,res);

        long dis = Math.round(res[0]);
        if(dis <1000)
            return String.valueOf(dis)+"M away";
        else {
            double f = res[0]/1000;
            return String.valueOf(Math.round(f)) + "KM away";
        }

    }

}