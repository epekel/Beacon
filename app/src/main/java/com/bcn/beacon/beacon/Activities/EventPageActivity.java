package com.bcn.beacon.beacon.Activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bcn.beacon.beacon.Data.Models.Event;
import com.bcn.beacon.beacon.Data.Models.ListEvent;
import com.bcn.beacon.beacon.Adapters.EventImageAdapter;
import com.bcn.beacon.beacon.Data.Models.Date;
import com.bcn.beacon.beacon.Data.Models.Event;
import com.bcn.beacon.beacon.Data.Models.Location;
import com.bcn.beacon.beacon.R;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;

import com.bcn.beacon.beacon.Utility.DataUtil;
import com.bcn.beacon.beacon.Utility.UI_Util;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.joanzapata.iconify.widget.IconButton;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;

public class EventPageActivity extends AppCompatActivity {

    private Event mEvent;
    private String mEventId;
    private Date mDate;
    private Context mContext;

    private ArrayList<Drawable> mImageDrawables;
    private TextView mTitle;
    private TextView mDescription;
    private IconTextView mFavourite;
    private RecyclerView mImageScroller;
    private TextView mStartTime;
    private TextView mStartMonth;
    private TextView mStartDay;
    private TextView mAddress;
    private TextView mTags;

    private boolean favourited = false;

    private int from;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        Intent intent = getIntent();
        from = intent.getIntExtra("from", -1);

        Window window = this.getWindow();
        //set the status bar color if the API version is high enough
        UI_Util.setStatusBarColor(window, this.getResources().getColor(R.color.colorPrimary));

        //initialize array of image drawables, we will retrieve this from the event
        mImageDrawables = new ArrayList<>();

        //set the context for use in callback methods
        mContext = this;

        //get the event id
        mEventId = getIntent().getStringExtra("Event");

        System.out.println(mEventId);

        //fetch the event from the firebase database
        getEvent(mEventId);

        //retrieve all the views from the view hierarchy
        mTitle = (TextView) findViewById(R.id.event_title);
        mDescription = (TextView) findViewById(R.id.event_description);
        mFavourite = (IconTextView) findViewById(R.id.favourite_button);
        mImageScroller = (RecyclerView) findViewById(R.id.image_scroller);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mStartDay = (TextView) findViewById(R.id.start_day);
        mStartMonth = (TextView) findViewById(R.id.start_month);
        mAddress = (TextView) findViewById(R.id.address);
        mTags = (TextView) findViewById(R.id.tags);

        //change look of favourite icon when user presses it
        //filled in means favourited, empty means not favourited
        mFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int duration = Toast.LENGTH_SHORT;

                if (!favourited) {
                    ((IconTextView) view).setText("{fa-star}");
                    CharSequence text = getString(R.string.favourited);

                    addFavourite();

                    Toast toast = Toast.makeText(mContext, text, duration);
                    toast.show();
                    favourited = true;
//                    ((IconTextView) view).setBackgroundColor(getBaseContext()
//                            .getResources().getColor(R.color.colorPrimary));
                } else {
                    CharSequence text = getString(R.string.un_favourited);

                    removeFavourite();

                    Toast toast = Toast.makeText(mContext, text, duration);
                    toast.show();
                    favourited = false;
                    ((IconTextView) view).setText("{fa-star-o}");
//                    ((IconTextView) view).setBackgroundColor(getBaseContext()
//                            .getResources().getColor(R.color.colorPrimary));
                }
            }

        });

        //Add fake images to the event page
        mImageDrawables.add(getResources().getDrawable(R.drawable.no_pic_icon));
        mImageDrawables.add(getResources().getDrawable(R.drawable.no_pic_icon));
        mImageDrawables.add(getResources().getDrawable(R.drawable.no_pic_icon));

        //create adapter between image list and recycler view
        EventImageAdapter eventImageAdapter = new EventImageAdapter(mImageDrawables);

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mImageScroller.setLayoutManager(horizontalLayoutManagaer);

        mImageScroller.setAdapter(eventImageAdapter);


        //TODO THE FOLLOWING CODE IS SIMPLY TEST CODE TO CHECK THE XML LAYOUT, NEEDS TO BE REPLACED WITH CODE TO RETRIEVE FIELDS OF REAL EVENT OBJECT

        //create a fake date object and set all the fields to the current time
//        mDate = new Date();
//        Calendar mcurrentDate = Calendar.getInstance();
//        int defaultWeekDay = mcurrentDate.get(Calendar.DAY_OF_WEEK);
//        int defaultYear = mcurrentDate.get(Calendar.YEAR);
//        int defaultMonth = mcurrentDate.get(Calendar.MONTH);
//        int defaultDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
//        int defaultHour = mcurrentDate.get(Calendar.HOUR_OF_DAY);
//        int defaultMinute = mcurrentDate.get(Calendar.MINUTE);
//
//        mDate.setYear(defaultYear);
//        mDate.setMonth(defaultMonth);
//        mDate.setDay(defaultDay);
//        mDate.setHour(defaultHour);
//        mDate.setMinute(defaultMinute);
//
//        //create dummy event to populate the event page UI
//        mEvent = new Event("blabla", "Cool Party", "blabla", 5, 100, null, "Come out for the Donald Trump Election Celebration Party!");
//        mEvent.setDate(mDate);
//
//        //set the description
//
////        DataUtil.textViewFormatter(mDescription, mEvent.getDescription(), 30);
//        mDescription.setText(mEvent.getDescription());
//
//        int hour = mDate.getHour();
//        int minute = mDate.getMinute();
//        int day = mDate.getDay();
//        int month = mDate.getMonth();
//        int year = mDate.getYear();

//


        //TODO END OF TEST BLOCK


        // GetEvent();
    }

    /**
     * This method fetches the Event data from the firebase database
     *
     * @param eventId The event Id as a string
     */
    private void getEvent(String eventId) {


        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events/" + eventId);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get the event
                mEvent = dataSnapshot.getValue(Event.class);
                //populate the views in the view hierarchy with actual event data
                populate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                //TODO add error catch
            }

        });
    }


    /**
     * This method populates the views in the event page
     */
    private void populate() {
        //setTitle(mEvent.getName());
//        TextView description = (TextView) findViewById(R.id.description);
        assert mEvent != null;
        assert mDescription != null;

        mDescription.setText(mEvent.getDescription());
        mTitle.setText(mEvent.getName());

        //display the tags for this event
        mTags.setText(" #Party\n #Awesome\n #Fun");

        boolean isPM = false;

        //get Location
        Location location = mEvent.getLocation();
        Geocoder coder = new Geocoder(this);
        List<Address> addresses;
        Address address;

        //convert address to a readable string if possible
        try{
            addresses = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(!addresses.isEmpty()) {
                address = addresses.get(0);

                //set the address text to the retrieved address
                mAddress.setText(address.getAddressLine(0));
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        Date date = mEvent.getDate();

        if (date.getHour() >= 12) {
            isPM = true;
        }
        //set the
        mStartDay.setText("" + date.getDay());
        mStartMonth.setText("" + DataUtil.convertMonthToString(date.getMonth()));
        mStartTime.setText(" " + String.format(Locale.US, "%02d:%02d %s",
                (date.getHour() == 12 || date.getMinute() == 0) ? 12 : date.getHour() % 12, date.getMinute(),
                isPM ? "PM" : "AM"));

//        mAddress.setText("666 Trump Ave.");

    }

    /**
     * Function for adding the event to user's favourites
     *
     * @return true if successful, otherwise return false
     */

    private boolean addFavourite() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("Users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // TODO: We should add a "true" check for if an event is favourited, so the button can remove the event from favourites on click
        users.child(userId).child("favourites").child(mEventId).setValue(true);
//        Toast.makeText(EventPageActivity.this, "Event added to favourites!", Toast.LENGTH_SHORT).show();

        return true;
    }

    /**
     * Function to remove this event from the user's favourites
     *
     * @return true if successful, otherwise return false
     */
    private boolean removeFavourite(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("Users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        users.child(userId).child("favourites").child(mEventId).removeValue();

        return true;
    }

    @Override
    public void onBackPressed() {
        MainActivity.setEventPageClickedFrom(from);
        super.onBackPressed();
    }


//    private boolean initFavourite(){
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference users = database.getReference("Users");
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        users.child(userId).child("favourites").child(mEventId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot)
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }
}

   /* private void on_directions_click(Button Directions) {

        double Lat = event.getLocation().getLatitude();
        double Long = event.getLocation().getLongitude();
        String Latitude = String.valueOf(Lat);
        String Longitude = String.valueOf(Long);

        Uri intent_uri = Uri.parse("google.navigation:q=" + Latitude + Longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intent_uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }*/


    /*private void showAlert(String toShow){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Event Name")
                .setMessage(toShow)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }*/

