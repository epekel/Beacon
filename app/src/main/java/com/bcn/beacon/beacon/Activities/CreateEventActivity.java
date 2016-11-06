package com.bcn.beacon.beacon.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bcn.beacon.beacon.CustomViews.WorkaroundMapFragment;
import com.bcn.beacon.beacon.Data.Models.Date;
import com.bcn.beacon.beacon.Data.Models.Event;
import com.bcn.beacon.beacon.Data.Models.Location;
import com.bcn.beacon.beacon.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.joanzapata.iconify.widget.IconTextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Locale;

import static android.support.design.R.styleable.View;

public class CreateEventActivity extends AuthBaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemSelectedListener {

    EditText eTime;
    EditText eDate;
    Date date = new Date();
    EditText eName;
    EditText eDescription;

    Location location = new Location();
    ImageButton eAddImage;
    Uri picUri;
    FloatingActionButton myFab;
    ScrollView mScrollView;
    Spinner categorySpinner;

    private double userLat, userLng;

    final int PIC_CROP = 2;
    final int REQUEST_IMAGE_CAPTURE = 1;
    final int PIC_SAVE = 0;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private WorkaroundMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Create Event");

        eTime = (EditText) findViewById(R.id.event_time);
        eDate = (EditText) findViewById(R.id.event_date);
        eName = (EditText) findViewById(R.id.input_name);
        eDescription = (EditText) findViewById(R.id.input_description);
        eAddImage = (ImageButton) findViewById(R.id.addImageButton);
        categorySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(this);
        myFab = (FloatingActionButton) findViewById(R.id.fab);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
                .build();

        initialzieDateandTime();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userLat = extras.getDouble("userlat");
            userLng = extras.getDouble("userlng");
            //The key argument here must match that used in the other activity
        }

        eDate.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        selectedmonth = selectedmonth + 1;
                        eDate.setText("" + selectedday + "/" + selectedmonth + "/" + selectedyear);
                        date.setDay(selectedday);
                        date.setMonth(selectedmonth);
                        date.setYear(selectedyear);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                //mDatePicker.getDatePicker().setMinDate(mcurrentDate.getTimeInMillis());
                mDatePicker.getDatePicker().setMinDate(mcurrentDate.getTimeInMillis());
                mDatePicker.show();
            }
        });

        eTime.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        boolean isPM = (selectedHour >= 12);
                        date.setHour(selectedHour);
                        date.setMinute(selectedMinute);
                        eTime.setText(String.format(Locale.US, "%02d:%02d %s",
                                (selectedHour == 12 || selectedHour == 0) ? 12 : selectedHour % 12, selectedMinute,
                                isPM ? "PM" : "AM"));
                    }
                }, hour, minute, false);//No to 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        eAddImage.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        myFab.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( eName.getText().toString().trim().equals("")){
                    eName.setError( "Your event needs a name!" );
                }else {
                    upload();
                }
            }
        });
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    Intent saveImageIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(saveImageIntent, PIC_SAVE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PIC_SAVE) {
                picUri = data.getData();
                performCrop(picUri);

            }
            else if (requestCode == PIC_CROP) {
                /*
                Bitmap pic = null;
                try {
                    pic = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), picUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                eAddImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                eAddImage.setImageBitmap(pic);*/
                Bundle extras = data.getExtras();
                Bitmap photo = extras.getParcelable("data");
                eAddImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                eAddImage.setImageBitmap(photo);
            }
            else if(requestCode == REQUEST_IMAGE_CAPTURE){
                //Bundle extras = data.getExtras();
                //Bitmap pic = (Bitmap) extras.get("data");
                picUri = data.getData();
                performCrop(picUri);
            }
        }
    }

    private Bitmap normalizeOrientation(Uri imgUri){
        String filename = getFileName(imgUri);
        try {
            ExifInterface ei = new ExifInterface(filename);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    1);
            System.out.println(orientation);
            Bitmap pic = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), picUri);

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateImage(pic, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateImage(pic, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateImage(pic, 270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
            return pic;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private void performCrop(Uri picUri) {
        //call the standard crop action intent (the user device may not support it)
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        //indicate image type and Uri
        cropIntent.setDataAndType(picUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1080);
        cropIntent.putExtra("aspectY", 1080);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 1080);
        cropIntent.putExtra("outputY", 1080);
        //retrieve data on return
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult

        //cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);

        startActivityForResult(cropIntent, PIC_CROP);
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void upload() {

    if(location == null) {
        location.setLatitude(49.2765);
        location.setLongitude(-123.2177);
    }
        Event toUpload = new Event();

        toUpload.setName(eName.getText().toString());
        toUpload.setDescription(eDescription.getText().toString());
        toUpload.setDate(date);
        toUpload.setLocation(location);
        toUpload.upload();

        kill_activity();

    }

    void kill_activity()
    {
        finish();
    }


    void initialzieDateandTime(){
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);

        int mYear = mCurrentTime.get(Calendar.YEAR);
        int mMonth = mCurrentTime.get(Calendar.MONTH);
        int mDay = mCurrentTime.get(Calendar.DAY_OF_MONTH);

        boolean isPM = (hour >= 12);

        mMonth = mMonth + 1;
        eDate.setText("" + mDay + "/" + mMonth + "/" + mYear);
        date.setDay(mDay);
        date.setMonth(mMonth);
        date.setYear(mYear);

        date.setHour(hour);
        date.setMinute(minute);

        eTime.setText(String.format(Locale.US, "%02d:%02d %s",
                (hour == 12 || hour == 0) ? 12 : hour % 12, minute,
                isPM ? "PM" : "AM"));
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

        if (mMap == null) {
            mScrollView = (ScrollView) findViewById(R.id.scroll_view);
            ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .setListener(new WorkaroundMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
            ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public void initMap(){
        if(mMap != null){

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker arg0) {
                    // TODO Auto-generated method stub
                    Log.d("System out", "onMarkerDragStart..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker arg0) {
                    // TODO Auto-generated method stub
                    Log.d("System out", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                    location.setLongitude(arg0.getPosition().longitude);
                    location.setLatitude(arg0.getPosition().latitude);
                }

                @Override
                public void onMarkerDrag(Marker arg0) {
                    // TODO Auto-generated method stub
                    Log.i("System out", "onMarkerDrag...");
                }
            });


            Marker marker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                    .position(new LatLng(userLat, userLng))
                    .title("Your Event's Location"));
            marker.setDraggable(true);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
            marker.showInfoWindow();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
