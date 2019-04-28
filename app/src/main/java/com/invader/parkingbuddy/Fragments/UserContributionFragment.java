package com.invader.parkingbuddy.Fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.invader.parkingbuddy.Adapter.ParkingListAdapter;
import com.invader.parkingbuddy.Model.ParkingLocation;
import com.invader.parkingbuddy.R;
import com.location.aravind.getlocation.GeoLocator;

import java.util.ArrayList;


public class UserContributionFragment extends Fragment {

    private RecyclerView rview_user;
    private ArrayList<ParkingLocation> parkingLocationList = new ArrayList<>();
    private ParkingListAdapter parkingListAdapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private TextView user_error;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_contribution, container, false);


        //binding views---------------------------

        rview_user = view.findViewById(R.id.rview_user);
        parkingListAdapter = new ParkingListAdapter(parkingLocationList, getContext(),getActivity());

        rview_user.setLayoutManager(new LinearLayoutManager(getContext()));
        rview_user.setAdapter(parkingListAdapter);
        user_error = view.findViewById(R.id.user_error);
        user_error.setVisibility(View.GONE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //firebase Database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        progressBar= view.findViewById(R.id.uprogress);
        progressBar.setVisibility(View.VISIBLE);

        getNearByParkings();

        rview_user.setAdapter(parkingListAdapter);


        return view;
    }

    public void getNearByParkings(){
        databaseReference.child("Locations").orderByChild("uid").equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ParkingLocation parkingLocation;
                        if(dataSnapshot.exists()){
                            parkingLocationList.clear();
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                parkingLocation = data.getValue(ParkingLocation.class);
                                parkingLocationList.add(parkingLocation);
                            }

                            parkingListAdapter.notifyDataSetChanged();


                        }else
                            Toast.makeText(getContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);
                        if(parkingLocationList.size()<1)
                            user_error.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public long getDistance(double lat, double lon){
        GeoLocator geoLocator = new GeoLocator(getContext(),getActivity());
        LatLng pos = new LatLng(geoLocator.getLattitude(),geoLocator.getLongitude());
        float [] res = new float[1];

        Location.distanceBetween(pos.latitude,pos.longitude,
                lat,lon,res);

        long dis = Math.round(res[0]);

        return dis;

    }

}
