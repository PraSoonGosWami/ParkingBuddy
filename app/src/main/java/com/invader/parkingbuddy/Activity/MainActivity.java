package com.invader.parkingbuddy.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.invader.parkingbuddy.Fragments.AddNewParkingFragment;
import com.invader.parkingbuddy.Fragments.AllParkingFragment;
import com.invader.parkingbuddy.Fragments.NearYouParkingFragment;
import com.invader.parkingbuddy.Fragments.UserContributionFragment;
import com.invader.parkingbuddy.R;
import com.invader.parkingbuddy.auth.LoginActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton add_new;
    private RelativeLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Parking Buddy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        add_new = findViewById(R.id.add_new);
        main = findViewById(R.id.main);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("mylog", "Getting Location Permission");
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS}, 1);
                return;
            }
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
                return;
            }

        }
        Boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps_enabled) {
            Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show();
            buildAlertMessageNoGps();

        }
        if (!network_enabled) {
            Toast.makeText(this, "Turn on Network", Toast.LENGTH_SHORT).show();
        }

        if (gps_enabled) {
            main.setVisibility(View.VISIBLE);

            add_new.setOnClickListener(v -> {
                swapFragments(new AddNewParkingFragment());
                main.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Add new parking");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            });

            SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
            ViewPager viewPager = findViewById(R.id.viewpager);

            //TabLayout Initialization
            FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                    getSupportFragmentManager(), FragmentPagerItems.with(this)
                    .add("Near you", NearYouParkingFragment.class)
                    .add("All", AllParkingFragment.class)
                    .add("Your Contribution", UserContributionFragment.class)
                    .create());

            //setting view pager adapter
            viewPager.setAdapter(adapter);
            viewPagerTab.setViewPager(viewPager);
        }

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount() <= 0) {
            main.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Parking Buddy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        }
    }

    //replaces the container with fragments and sends category id
    public void swapFragments(Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction;
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frame_container, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //performs logout
    public void logout() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage("Are you sure you want logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Toast.makeText(MainActivity.this, "Restart your app again", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
