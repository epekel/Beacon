package com.bcn.beacon.beacon.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import com.android.camera.CropImageIntentBuilder;
import com.bcn.beacon.beacon.Data.Models.Date;
import com.bcn.beacon.beacon.Data.Models.Event;
import com.bcn.beacon.beacon.Data.Models.Location;
import com.bcn.beacon.beacon.R;
import com.bcn.beacon.beacon.Utility.ImageUtil;
import com.bcn.beacon.beacon.Utility.UI_Util;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AuthBaseActivity implements
        AdapterView.OnItemSelectedListener, View.OnClickListener{

    private EditText eTime;
    private EditText eDate;
    private Date date = new Date();
    private EditText eName;
    private EditText eDescription;
    private EditText eAddress;

    private int from;

    private Location location = new Location();
    private ImageButton eAddImage;
    private Uri picUri;
    private FloatingActionButton myFab;
    private ScrollView mScrollView;
    private Spinner categorySpinner;
    private ImageUtil imgUtil = new ImageUtil();

    private File tempfile;

    private double currentLat, currentLng;
    private double userLat, userLng;

    final int LOCATION_SELECTED = 3;
    final int PIC_CROP = 2;
    final int REQUEST_IMAGE_CAPTURE = 1;
    final int PIC_SAVE = 0;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);



        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        Window window = this.getWindow();
        //set the status bar color if the API version is high enough
        UI_Util.setStatusBarColor(window, this.getResources().getColor(R.color.colorPrimary));

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
        myFab = (FloatingActionButton) findViewById(R.id.fab);
        eAddress = (EditText) findViewById(R.id.input_address);

        eDate.setOnClickListener(this);
        eTime.setOnClickListener(this);
        eAddImage.setOnClickListener(this);
        eAddress.setOnClickListener(this);
        categorySpinner.setOnItemSelectedListener(this);
        myFab.setOnClickListener(this);

        initialzieDateandTime();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userLat = extras.getDouble("userlat");
            userLng = extras.getDouble("userlng");
            currentLat = userLat;
            currentLng = userLng;
            from = extras.getInt("from");
            location.setLatitude(userLat);
            location.setLongitude(userLng);
            eAddress.setText(findLocationName(userLat, userLng));
            //The key argument here must match that used in the other activity
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.event_time): {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, R.style.PickerDialogTheme, new TimePickerDialog.OnTimeSetListener() {
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
                break;
            }
            case (R.id.event_date): {
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(CreateEventActivity.this, R.style.PickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
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
                break;
            }

            case (R.id.addImageButton): {
                selectImage();
                break;
            }
            case (R.id.fab): {
                if( eName.getText().toString().trim().equals("")){
                    eName.setError( "Your event needs a name!" );
                }else {
                    upload();
                }

                break;
            }
            case(R.id.input_address) : {
                Intent intent = new Intent(this, SelectLocationActivity.class);
                intent.putExtra("userlat", userLat);
                intent.putExtra("userlng", userLng);
                intent.putExtra("curlat", currentLat);
                intent.putExtra("curlng", currentLng);
                startActivityForResult(intent, LOCATION_SELECTED);

                break;
            }
        }

    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkExternalMemoryPermissions();
                    }
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    tempfile = getTempFile();
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempfile));
                        takePictureIntent.putExtra("return-data", true);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkExternalMemoryPermissions();
                    }
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

    private File getTempFile() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File file = new File(Environment.getExternalStorageDirectory(),"temp.jpg");
            try {
                file.createNewFile();
            } catch (IOException e) {}

            return file;
        } else {

            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        File croppedImageFile = new File(getFilesDir(), "test.jpg");

        if (resultCode == RESULT_OK) {
            if (requestCode == PIC_SAVE) {

                Uri croppedImage = Uri.fromFile(croppedImageFile);

                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(300, 300, 1080, 1080, croppedImage);
                cropImage.setOutlineColor(0xFF03A9F4);
                cropImage.setSourceImage(data.getData());

                startActivityForResult(cropImage.getIntent(getApplicationContext()), PIC_CROP);
            }
            else if (requestCode == PIC_CROP) {
                /* ViewGroup.LayoutParams imglayout = eAddImage.getLayoutParams();
                if(imglayout.height < 300) {
                    imglayout.height = imglayout.height + 400;
                }
                eAddImage.setLayoutParams(imglayout); */
                eAddImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                eAddImage.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
            }
            else if(requestCode == REQUEST_IMAGE_CAPTURE){
                Uri croppedImage = Uri.fromFile(croppedImageFile);
                Log.d("System out", "orient " + getImageOrientation());

                try {
                    imgUtil.handleSamplingAndRotationBitmap(this, Uri.fromFile(croppedImageFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(300, 300, 1080, 1080, croppedImage);

                    cropImage.setOutlineColor(0xFF03A9F4);
                    cropImage.setSourceImage(Uri.fromFile(tempfile));
                    startActivityForResult(cropImage.getIntent(getApplicationContext()), PIC_CROP);

            }
            else if(requestCode == LOCATION_SELECTED){
                if (data.getExtras() != null) {
                    currentLat = data.getExtras().getDouble("lat");
                    currentLng = data.getExtras().getDouble("lng");
                    String name = data.getExtras().getString("name");
                    eAddress.setText(name);
                    location.setLatitude(currentLat);
                    location.setLongitude(currentLng);
                    //The key argument here must match that used in the other activity
                }
            }
        }
    }
    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


    private int getImageOrientation() {
        final String[] imageColumns = {MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageColumns, null, null, imageOrderBy);

        if (cursor.moveToFirst()) {
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
            cursor.close();
            return orientation;
        } else {
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkExternalMemoryPermissions(){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant

            return;
        }
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

        // for temporary fix on back pressed
        MainActivity.setEventPageClickedFrom(from);

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

    String findLocationName(double lat, double lng){
        String name = new String();

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat,lng, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses != null) {
            if (!addresses.isEmpty()) {
                name = addresses.get(0).getAddressLine(0);
            } else {
                name = "";
            }
        }

        return name;
    }

    @Override
    public void onBackPressed() {
        // for temporary fix on back pressed
        MainActivity.setEventPageClickedFrom(from);
        super.onBackPressed();
    }
}
