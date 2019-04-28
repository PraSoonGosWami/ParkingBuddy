package com.invader.parkingbuddy.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.invader.parkingbuddy.Model.ParkingLocation;
import com.invader.parkingbuddy.R;

import java.util.ArrayList;

public class ViewLocationFragment extends Fragment {

    private String id;
    private EditText location_name,location_desc,location_tSlots,location_aSlots,location_cost;
    private TextView viewLoc;
    private Button location_add;
    private ImageView v_loc_image;
    private FloatingActionButton floatingActionButton,update;
    private ProgressBar progressBar;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private String loc_user_id,loc;
    private ScrollView scroll;
    private ParkingLocation parkingLocation;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_location, container, false);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Parking Details");

        //getting bundle id
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            id = bundle.getString("id");
        }


        //binding views

        viewLoc = view.findViewById(R.id.location);
        location_name = view.findViewById(R.id.location_name);
        location_desc = view.findViewById(R.id.location_desc);
        location_tSlots = view.findViewById(R.id.location_tSlots);
        location_aSlots = view.findViewById(R.id.location_aSlots);
        location_cost = view.findViewById(R.id.location_cost);
        location_add= view.findViewById(R.id.location_add);
        floatingActionButton = view.findViewById(R.id.navigate);
        progressBar = view.findViewById(R.id.progress);
        v_loc_image = view.findViewById(R.id.v_loc_image);
        scroll = view.findViewById(R.id.scroll);
        update = view.findViewById(R.id.update);


        scroll.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);

        location_add.setVisibility(View.GONE);







        //database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getDetails();

        floatingActionButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });




        update.setOnClickListener(v->{
            updateLocation();
        });




        return view;
    }


    public void getDetails(){

        databaseReference.child("Locations").orderByChild("locationid").equalTo(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                parkingLocation = data.getValue(ParkingLocation.class);
                            }
                            scroll.setVisibility(View.VISIBLE);
                            Glide.with(getContext())
                                    .asBitmap()
                                    .load(Uri.parse(parkingLocation.getMapURL()))
                                    .into(v_loc_image);
                            location_name.setText(parkingLocation.getName());
                            viewLoc.setText(parkingLocation.getAddress());
                            location_desc.setText(parkingLocation.getDescription());
                            location_tSlots.setText(String.valueOf(parkingLocation.getTotalSlots()));
                            location_aSlots.setText(String.valueOf(parkingLocation.getAvailableSlots()));
                            location_cost.setText(parkingLocation.getCost());
                            loc_user_id = parkingLocation.getUid();

                            loc = parkingLocation.getLatitude()+","+parkingLocation.getLongitude();

                            if(loc_user_id != null) {
                                if (!(loc_user_id.equals(firebaseUser.getUid()))) {
                                    location_name.setEnabled(false);
                                    location_name.setBackgroundResource(android.R.color.transparent);
                                    location_desc.setEnabled(false);
                                    location_desc.setBackgroundResource(android.R.color.transparent);
                                }
                            }

                        }else {
                            Toast.makeText(getContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
                            scroll.setVisibility(View.GONE);
                        }

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }

    public  void updateLocation(){
        ProgressDialog progressDialog = new ProgressDialog(getContext(),ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("Sit back relax!!");

        String address = viewLoc.getText().toString().trim();
        String name = location_name.getText().toString().trim();
        String desc = location_desc.getText().toString().trim();


        int tSlots = 0;
        int aSlots = 0;

        String pCost = location_cost.getText().toString();
        double lattitude = parkingLocation.getLatitude();
        double longitude = parkingLocation.getLongitude();
        //Toast.makeText(getContext(), ""+lattitude+","+longitude, Toast.LENGTH_SHORT).show();
        String uid = parkingLocation.getUid();
        String pos = lattitude+","+longitude;
        String id = parkingLocation.getLocationid();

        if(pos == null){
            Toast.makeText(getContext(), "Something went wrong\nTry selecting location again", Toast.LENGTH_SHORT).show();
            return;
        }
        String mapurl = parkingLocation.getMapURL();


        if(name.isEmpty() || name == null){
            location_name.setError("Name required");
            location_name.requestFocus();
            return;
        }
        if(desc.isEmpty() || desc == null) {
            desc = "Not available";
        }


        if(location_tSlots.getText().toString().isEmpty() || location_tSlots.getText().toString()==null){
            location_tSlots.setError("Required");
            location_tSlots.requestFocus();
            return;
        }

        if(location_aSlots.getText().toString().isEmpty() || location_aSlots.getText().toString()==null){
            location_aSlots.setError("Required");
            location_aSlots.requestFocus();
            return;
        }

        if(pCost.isEmpty() || pCost == null){
            location_cost.setError("Required");
            location_cost.requestFocus();
            return;
        }

        tSlots = Integer.parseInt(location_tSlots.getText().toString());

        aSlots = Integer.parseInt(location_aSlots.getText().toString());

        if(tSlots < aSlots){
            Toast.makeText(getContext(), "Total slots cannot be less than available slots", Toast.LENGTH_SHORT).show();
            return;
        }


        ParkingLocation parkingLocation = new ParkingLocation(
                name,
                lattitude,
                longitude,
                address,
                desc,
                tSlots,
                aSlots,
                pCost,
                uid,
                mapurl,
                id
        );

        progressDialog.show();
        databaseReference.child("Locations").child(id).setValue(parkingLocation)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Any changes will be reverted", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStackImmediate();

                    }


                });

    }



}
