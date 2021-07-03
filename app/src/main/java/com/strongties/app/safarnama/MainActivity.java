package com.strongties.app.safarnama;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.strongties.app.safarnama.dbhelper.DatabaseHelper;
import com.strongties.app.safarnama.services.LocationService;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "Main";

    private ArrayList<String> places_list;
    private ArrayList<String> places_id_list;

    AutoCompleteTextView actv_search;
    ArrayAdapter<String> adapter;


    private LocationService mService;
    FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          //      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Initialize Database
        DatabaseHelper dbhelper = new DatabaseHelper(this);

        // Initialize ArrayLists
        places_list = new ArrayList<>();
        places_id_list = new ArrayList<>();

        //Initialize Auto-Complete Text View
        actv_search = findViewById(R.id.main_menu_actv);
        CircleImageView search_icon = findViewById(R.id.main_menu_title_icon);
        CircleImageView back_btn = findViewById(R.id.main_menu_title_back);
        CircleImageView reset = findViewById(R.id.main_menu_title_reset);
        CircleImageView iv_profile = findViewById(R.id.main_menu_title_profile);

        // Set the Arraylists
        fetchlandmarkdetails();

        //Flag for Keyboard On Check
        AtomicReference<Boolean> keyboardOn = new AtomicReference<>();
        keyboardOn.set(Boolean.FALSE);

        actv_search.setOnTouchListener((view, motionEvent) -> {

            if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                keyboardOn.set(Boolean.TRUE);
                iv_profile.setVisibility(View.GONE);
                reset.setVisibility(View.VISIBLE);
                search_icon.setVisibility(View.INVISIBLE);
                back_btn.setVisibility(View.VISIBLE);
            }

            return false;

        });

        // Set Auto-Complete Text View
        actv_search.setOnItemClickListener((adapterView, view, i, l) -> {
            reset.setVisibility(View.GONE);
            iv_profile.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.GONE);
            search_icon.setVisibility(View.VISIBLE);


            view.startAnimation(new AlphaAnimation(1F, 0.7F));
            //Set Progressbar
            ProgressDialog mProgressDialog = ProgressDialog.show(MainActivity.this, "Searching", "Fetching Information from Server");
            mProgressDialog.setCanceledOnTouchOutside(false); // main method that force user cannot click outside
            Runnable progressRunnable = () -> {
                mProgressDialog.cancel();       //Cancel Progress Dialog
                actv_search.setText("");
                Toast.makeText(this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
            };
            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 5000);    //Run after 5 secs


            String inputPlaceID = places_id_list.get(places_list.indexOf(adapterView.getAdapter().getItem(i).toString()));
            Log.d(TAG, "PlaceID -> " + inputPlaceID);
        });

        back_btn.setOnClickListener(view -> {
            reset.setVisibility(View.GONE);
            iv_profile.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.GONE);
            search_icon.setVisibility(View.VISIBLE);
            actv_search.setText("");

            dismissKeyboard(this);
            keyboardOn.set(Boolean.FALSE);

        });

        reset.setOnClickListener(view -> {
            actv_search.setText("");
        });




        // Initialise FusionLocationProvider Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Start Service
        mService = new LocationService();
        Intent mServiceIntent = new Intent(this, mService.getClass());
        if (!isLocServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }

        //Start Initial Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Fragment_Wander(), "Wonder Fragment").commit();



        // Profile Icon Changes
        iv_profile.setOnClickListener(view -> {
            //Dialog Initiation
            Dialog myDialog = new Dialog(MainActivity.this);
            myDialog.setContentView(R.layout.dialog_main_profile);
            Objects.requireNonNull(myDialog.getWindow()).setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Objects.requireNonNull(myDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.getWindow().getAttributes().gravity = Gravity.TOP;

            myDialog.show();
        });


        // Main Menu Button Click UI changes
        MaterialButton mm_btn1 = findViewById(R.id.menu_item_1);
        MaterialButton mm_btn2 = findViewById(R.id.menu_item_2);
        MaterialButton mm_btn3 = findViewById(R.id.menu_item_3);
        MaterialButton mm_btn4 = findViewById(R.id.menu_item_4);

        {

            mm_btn1.setOnClickListener(view -> {
                //Change the btn which was clicked
                mm_btn1.setBackgroundColor(getColor(R.color.primeColor));
                mm_btn1.setTextColor(getColor(R.color.coloronPrimary));

                //Change for other buttons
                mm_btn2.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn2.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn3.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn3.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn4.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn4.setTextColor(getColor(R.color.coloronSeconary));
            });

            mm_btn2.setOnClickListener(view -> {
                //Change the btn which was clicked
                mm_btn1.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn1.setTextColor(getColor(R.color.coloronSeconary));

                //Change for other buttons
                mm_btn2.setBackgroundColor(getColor(R.color.primeColor));
                mm_btn2.setTextColor(getColor(R.color.coloronPrimary));

                mm_btn3.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn3.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn4.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn4.setTextColor(getColor(R.color.coloronSeconary));
            });

            mm_btn3.setOnClickListener(view -> {
                //Change the btn which was clicked
                mm_btn1.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn1.setTextColor(getColor(R.color.coloronSeconary));

                //Change for other buttons
                mm_btn2.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn2.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn3.setBackgroundColor(getColor(R.color.primeColor));
                mm_btn3.setTextColor(getColor(R.color.coloronPrimary));

                mm_btn4.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn4.setTextColor(getColor(R.color.coloronSeconary));
            });

            mm_btn4.setOnClickListener(view -> {
                //Change the btn which was clicked
                mm_btn1.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn1.setTextColor(getColor(R.color.coloronSeconary));

                //Change for other buttons
                mm_btn2.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn2.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn3.setBackgroundColor(getColor(R.color.coloronPrimary));
                mm_btn3.setTextColor(getColor(R.color.coloronSeconary));

                mm_btn4.setBackgroundColor(getColor(R.color.primeColor));
                mm_btn4.setTextColor(getColor(R.color.coloronPrimary));
            });

        }



    }

    private boolean isLocServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    private void fetchlandmarkdetails(){
        DatabaseHelper dbhelper = new DatabaseHelper(this);
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor cursor;

        cursor = database.rawQuery("SELECT name, place_id FROM LANDMARKS", new String[]{});

        if (cursor != null) {
            cursor.moveToFirst();
        } else {
            Toast.makeText(this, getString(R.string.error_fetching), Toast.LENGTH_SHORT).show();
        }
        do {
            assert cursor != null;
            places_list.add(cursor.getString(0));
            places_id_list.add(cursor.getString(1));
        } while (cursor.moveToNext());

        cursor.close();

        // Set actv adapter
        adapter = new ArrayAdapter<>(this, R.layout.custom_layout_actv_main_menu, R.id.custom_actv_text, places_list);
        actv_search.setAdapter(adapter);
    }

    public void dismissKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != activity.getCurrentFocus())
            imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getApplicationWindowToken(), 0);
    }


}