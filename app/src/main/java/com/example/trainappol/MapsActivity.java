package com.example.trainappol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , ActivityCompat.OnRequestPermissionsResultCallback {

    //?????? ?????????
    private AppCompatButton Check_Start;
    private AppCompatButton Check_Termin;

    //????????? ????????? ??????
    private InputMethodManager imm;

    //???????????? ?????????
    private long backBtnTime = 0;

    //????????? ??????
    private boolean speed_notZero = true;
    private TextView Time;
    private EditText et_TrainNo;
    private TextView Slbtn;
    private TextView Elbtn;
    private AppCompatButton Setbtn;
    private TextView Trkind;
    private FloatingActionButton myFAB;
    private long MillisecondTime = 0L;  // ?????? ?????? ????????? ????????? ?????? ??????
    private long StartTime = 0L;        // ?????? ?????? ?????? ????????? ??? ?????? ????????? ??????
    long TimeBuff = 0L;         // ?????? ?????? ?????? ????????? ?????? ??? ??????
    long UpdateTime = 0L;       // ?????? ?????? ?????? ????????? ?????? ??? ?????? + ?????? ?????? ????????? ??? ?????? ????????? ?????? = ??? ??????
    private int num;              // ????????? Sequence
    private double latitude;
    private double longitude;
    private double s, s1;
    private boolean isRunning = false; // ????????? ?????????
    private boolean isEstimate = false; // ?????? ?????????
    private double lt;
    private double lg;

    private Handler handler;
    private int Sec, Seconds, Minutes, Hour;

    //????????? ???????????? ?????????
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private LocationManager lm;
    private LocationListener ll;
    double mySpeed;

    //?????????
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5???
    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // ?????? ????????? ?????? ??????
    boolean needRequest = false;


    // ?????? ???????????? ?????? ????????? ???????????? ???????????????.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????

    Location mCurrentLocatiion;
    LatLng currentPosition;



    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;
    //Date Types of in
    private long mNow;
    private Date mDate;
    private String mDB;
    private String database_name;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat mFormat2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private SimpleDateFormat mFormat3 = new SimpleDateFormat("yyMMddHHmmss");

    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.
    // (????????? Toast????????? Context??? ??????????????????.)


    //SQLite ?????????
    private DBOpenHelper mDBOpenHelper;
    private DTlogHelper mDTLogHelper;
    private Cursor mCursor;
    private InfoClass mInfoClass;
//    private ArrayList<InfoClass> mInfoArray;
    private ArrayList<String> mInfoArray_s;
    private ArrayList<String> mInfoArray_e;
    private String[] arrs;
    private String[] arre;
    private String trkind;


    private SQLiteDatabase db;
    private DTlogHelper logDB;

    // ??????????????????
    private ProgressBar progressBar;
    private BackTasking task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        askForPermissions();
        checkPermission2();
        task = new BackTasking();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mDBOpenHelper = new DBOpenHelper(this);
        mDTLogHelper = new DTlogHelper(this);
        mDBOpenHelper.open();
        mDTLogHelper.open();
        mInit();
        myFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SubActivity.class);
                startActivity(intent);
            }
        });
        mInfoArray_s = new ArrayList<String>();
        mInfoArray_e = new ArrayList<String>();
        trkind = "";
        Slbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                dlg.setTitle("??????").setMessage("??????????????? ???????????????.");
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dlg.show();
            }
        });
        Elbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                dlg.setTitle("??????").setMessage("??????????????? ???????????????.");
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dlg.show();
            }
        });
        Setbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                trkind = "";
                mInfoArray_s = new ArrayList<String>();
                mInfoArray_e = new ArrayList<String>();
                String TrainNumber = et_TrainNo.getText().toString();
                if (TrainNumber.getBytes().length <= 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("??????").setMessage("??????????????? ???????????????.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(),"Please, Set TrainNo.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    doWhileCursorToArray();
                    getStringCursor();
                    AlertDialog.Builder alg = new AlertDialog.Builder(MapsActivity.this);

                    Trkind.setText(trkind);
                    Collections.reverse(mInfoArray_e); // ????????????, ????????? ????????? ????????? ?????????, ???????????? ?????? ?????? ????????? ?????????.
                    arrs = mInfoArray_s.toArray(new String[mInfoArray_s.size()]);// ????????? ?????????(????????? ???)???
                    arre = mInfoArray_e.toArray(new String[mInfoArray_e.size()]); // ????????? ?????????(????????? ???)???
                    Slbtn.setText(arrs[0]);
                    Elbtn.setText(arre[0]);
                    Slbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                            dlg.setTitle("????????? ??????").setItems(arrs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Slbtn.setText(arrs[i]);
                                }
                            });
                            dlg.show();

                        }
                    });
                    Elbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                            dlg.setTitle("????????? ??????").setItems(arre, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Elbtn.setText(arre[i]);
                                }
                            });
                            dlg.show();
                        }
                    });
                    Slbtn.setText("?????????");
                    Elbtn.setText("?????????");
                }


                }catch (ArrayIndexOutOfBoundsException e){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                    dlg.setTitle("??????").setMessage("???????????? ?????? ?????????????????????.");
                    dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dlg.show();
                }
            }
        });



        Time = findViewById(R.id.Time);
        Time.setText("00:00:00");
        et_TrainNo = findViewById(R.id.edit2);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_main);

        //????????? ?????? ??????
        Check_Start = findViewById(R.id.btn1);
        Check_Termin = findViewById(R.id.btn2);
        Check_Termin.setEnabled(false);
        handler = new Handler();
        num = 1; //????????? seq 1????????? ??????
        Check_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String TrainNum = et_TrainNo.getText().toString();
                String TrainKid = Trkind.getText().toString();
                String Startlocation = Slbtn.getText().toString();
                String EndLocation = Elbtn.getText().toString();
                if (Startlocation.equals("?????????")  || EndLocation.equals("?????????")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("??????").setMessage("??????,???????????? ???????????????.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(),
                                    "Please, Set Start and End Station Name.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else if(mInfoArray_s.indexOf(Elbtn.getText().toString()) <= mInfoArray_s.indexOf(Slbtn.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("??????").setMessage("????????? ???????????? ????????????, " +
                            "\n???????????? ???????????? ???????????? ??????????????? ?????? ?????? ??? ????????????. \n???????????? ???????????? ?????????.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                    dlg.setTitle("??????").setMessage("- ????????????: " + TrainNum + "\n" +
                            "- ????????????: " + TrainKid + "\n" + "- ????????????: " + Startlocation + "\n" +
                            "- ????????????: " + EndLocation + "\n??? ????????? ????????? ?????????????????????????");
                    dlg.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                hideFAB();
                                SQLiteHelper.DATABASE_NAME = mFormat2.format(System.currentTimeMillis()) + ".db";
                                SQLiteHelper myDBHelper = new SQLiteHelper(MapsActivity.this);
                                database_name = SQLiteHelper.DATABASE_NAME.replace(".db","");
                                mDB = SQLiteHelper.TABLE_NAME;
                                Check_Termin.setText("????????????");
                                isRunning = !isRunning;
                                // SystemClock.uptimeMillis()??? ??????????????? ???????????? ?????? ??? ????????? ????????? ???????????? ??????
                                StartTime = SystemClock.uptimeMillis();
                                handler.postDelayed(runnable, 0);
                                Check_Start.setEnabled(false);
                                Check_Termin.setEnabled(true);
                                Setbtn.setEnabled(false);
//                              Timer timer = new Timer();
                                et_TrainNo.setEnabled(false);
                                Slbtn.setEnabled(false);
                                Elbtn.setEnabled(false);
                                latitude = Double.parseDouble(String.format("%.5f", location.getLatitude()));
                                longitude = Double.parseDouble(String.format("%.5f", location.getLongitude()));
                                db = myDBHelper.getWritableDatabase();
                                mTimerTask = createTimertask();
                                mTimer.schedule(mTimerTask, 0, 1000);
                                Toast.makeText(getApplicationContext(),
                                        "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            }
                            catch (NullPointerException ne){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                builder.setTitle("??????").setMessage("GPS ?????? ????????? ?????? ????????????. ?????? ?????? ???, GPS??? ?????? ?????? ??? ?????????.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                    }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog = dlg.create();
                    alertDialog.show();

                }

            }
        });
        Check_Termin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Date = mFormat3.format(System.currentTimeMillis());
                String TrainNum = et_TrainNo.getText().toString();
                String TrainKid = Trkind.getText().toString();
                String Startlocation = Slbtn.getText().toString();
                String EndLocation = Elbtn.getText().toString();
                isRunning = !isRunning;
                Check_Start.setEnabled(true);
                if(isRunning){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                    dlg.setTitle("??????").setMessage("????????? ?????????????????????????");
                    dlg.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    arrs = new String[0];
                                    arre = new String[0];
                                    Slbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                                            dlg.setTitle("??????").setMessage("??????????????? ???????????????.");
                                            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                            dlg.show();
                                        }
                                    });
                                    Elbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                                            dlg.setTitle("??????").setMessage("??????????????? ???????????????.");
                                            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                            dlg.show();
                                        }
                                    });
                                    task = new BackTasking();
                                    task.execute();
                                    Toast.makeText(MapsActivity.this,
                                            "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                    Check_Termin.setText("????????????");
                                    Time.setText("00:00:00");
                                    Trkind.setText("");
                                    TimeBuff = 0L;
                                    isEstimate = false;
                                    et_TrainNo.setText(null);
                                    Check_Termin.setEnabled(false);
                                    et_TrainNo.setEnabled(true);
                                    Slbtn.setEnabled(true);
                                    Elbtn.setEnabled(true);
                                    Setbtn.setEnabled(true);
                                    speed_notZero = true;
                                    Slbtn.setText("?????????");
                                    Elbtn.setText("?????????");
                                    num = 1;
                                    Hour = 0;
                                    Minutes = 0;
                                    Sec = 0;
                                    Seconds = 0;
                                    showFAB();
                                }
                            });
                    dlg.show();
                    isRunning = !isRunning;
                }
                else{
                    if(Time == null) {
                        return;
                    }
                    else{
                        mTimerTask.cancel();
                        TimeBuff += MillisecondTime;
                        Check_Termin.setText("????????????");
                        Toast.makeText(MapsActivity.this,
                                "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                }

                // Runnable ?????? ??????
                handler.removeCallbacks(runnable);

            }
        });



        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mySpeed = 0;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new SpeedoActionListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);


    }
    private void mInit(){
        Slbtn = findViewById(R.id.slbtn);
        Elbtn = findViewById(R.id.elbtn);
        Setbtn = findViewById(R.id.btn3);
        Trkind = findViewById(R.id.trkind);
        myFAB = findViewById(R.id.floatingActionButton);
    }
    private void showFAB(){
        myFAB.show();
    }
    private void hideFAB(){
        myFAB.hide();
    }


    // ???????????? ?????? ????????? ?????? ?????????
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this,
                    "??????????????? ????????? ????????? ?????? ???????????????.",Toast.LENGTH_SHORT).show();
        }


    }

    // dp to px
    private float dpToPx(Context context, float dp){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    //????????? ????????? ?????????
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
    //??????DB??? ????????? ???????????? ???, ??????,???????????? ???????????? ?????? ?????? ?????????
    private void doWhileCursorToArray(){
        mCursor = null;
        mCursor = mDBOpenHelper.getStName(et_TrainNo.getText().toString());

        while (mCursor.moveToNext()){
            int index = mCursor.getColumnIndex("info_st_nm");
            mInfoArray_s.add(mCursor.getString(index));
            mInfoArray_e.add(mCursor.getString(index));
            //System.out.println(mCursor.getString(index));
        }
        mCursor.close();
    }
    //??????DB??? ????????? ???????????? ???, ??????????????? ???????????? ?????? ?????? ?????????
    private void getStringCursor(){
        mCursor = null;
        mCursor = mDBOpenHelper.getTrKind(et_TrainNo.getText().toString());
        while (mCursor.moveToNext()){
            int index = mCursor.getColumnIndex("train_kind");
            trkind = mCursor.getString(index);
        }
        mCursor.close();
    }
    //????????? SQLite ????????? ?????????
    @NonNull
    private TimerTask createTimertask(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mNow = System.currentTimeMillis();
                mDate = new Date(mNow);
                String getTrainNo = et_TrainNo.getText().toString();
                String getStartLocation = Slbtn.getText().toString();
                String getEndLocation = Elbtn.getText().toString();
                double getLatitude = Double.parseDouble(String.format("%.5f", location.getLatitude()));
                double getLongitude = Double.parseDouble(String.format("%.5f", location.getLongitude()));
                double getAltitude = Double.parseDouble(String.format("%.5f", location.getAltitude()));
                String resulttime = String.format("%02d",Hour) + ":" + String.format("%02d",Minutes) + ":" + String.format("%02d", Sec);
                // lat??? long??? ???????????? ?????? 0????????? ?????? 0?????? ??????
                if (lt - getLatitude <= 0.00001 && lg - getLongitude <= 0.00001){
                    s = 0;
                    s1 = 0;
                } else {
                    s = Double.parseDouble(String.format("%.5f", 3.6 * location.getSpeed()));
                    s1 = location.getSpeed() * 3.6;
                }

                double getSpeed = s;
                String getTimes = resulttime;
                // ????????? ?????? ????????? ????????????
                double getDistance_per_sec = Double.parseDouble(String.format("%.5f", s1 * 10 / 36));
                String getDatetime = mFormat.format(mDate);
                HashMap<String, Object> result = new HashMap<>();
                //o1_trainNo, o2_startLoc, o3_latitude, o4_longitude, o5_speed, o6_times, o7_distance_per_sec
                if (speed_notZero){
                    result.put("trainNo", getTrainNo);
                    result.put("startLoc", getStartLocation);
                    result.put("endLoc", getEndLocation);
                    result.put("latitude", getLatitude);
                    result.put("longitude", getLongitude);
                    result.put("altitude", getAltitude);
                    result.put("speed", getSpeed);
                    result.put("distance_per_sec", getDistance_per_sec);
                    result.put("times", getTimes);
                    result.put("datetime", getDatetime);//?????? ????????? ??????!!
                    writeNewUser(db, mDB, num, getTrainNo, getStartLocation, getEndLocation, getLatitude,
                            getLongitude, getAltitude, getSpeed, getTimes, getDistance_per_sec, getDatetime);
                    num++;
                }

                if (getSpeed < 1){
                    speed_notZero = false;
                }else{
                    if (!speed_notZero){
                        result.put("trainNo", getTrainNo);
                        result.put("startLoc", getStartLocation);
                        result.put("endLoc", getEndLocation);
                        result.put("latitude", getLatitude);
                        result.put("longitude", getLongitude);
                        result.put("altitude", getAltitude);
                        result.put("speed", getSpeed);
                        result.put("distance_per_sec", getDistance_per_sec);
                        result.put("times", getTimes);
                        result.put("datetime", getDatetime);//?????? ????????? ??????!!
                        writeNewUser(db, mDB, num, getTrainNo, getStartLocation, getEndLocation, getLatitude,
                                getLongitude, getAltitude, getSpeed, getTimes, getDistance_per_sec, getDatetime);
                        num++;
                    }
                    speed_notZero = true;
                }
                lt = location.getLatitude();
                lg = location.getLongitude();


            }
        };
        return timerTask;
    }


    //????????? ????????? SQLite??? ??????
    private void writeNewUser (SQLiteDatabase db, String mDB, int num, String trainNo, String startLocation, String endLocation,
                                double latitude, double longitude, double altitude, double speed, String times, double distance_per_sec, String datetime){
        String id = Integer.toString(num);
        String lati = Double.toString(latitude);
        String logi = Double.toString(longitude);
        String alti = Double.toString(altitude);
        String spd = Double.toString(speed);
        String dps = Double.toString(distance_per_sec);
        String INSERT_INTO =  "INSERT INTO " + mDB +
                "(id, ????????????, ?????????, ?????????, ??????, ??????, ??????, ??????, ????????????, ??????????????????, ????????????) "
                + "VALUES(" + id
                + " ,'" + trainNo + "'"
                + " ,'" + startLocation + "'"
                + " ,'" + endLocation + "'"
                + " ,'" + lati + "'"
                + " ,'" + logi + "'"
                + " ,'" + alti + "'"
                + " ,'" + spd + "'"
                + " ,'" + times + "'"
                + " ,'" + dps + "'"
                + " ,'" + datetime + "');";
        db.execSQL(INSERT_INTO);
    }
    //???????????? ?????????
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            // ???????????? ???????????? ?????? ????????? ?????? ??? ?????? + ?????? ?????? ????????? ??? ?????? ????????? ?????? = ??? ??????
            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Sec = Seconds % 60;

            Minutes = Seconds / 60 % 60;

            Hour = Seconds / 3600;

            // TextView??? UpdateTime??? ???????????????
            String result = String.format("%02d",Hour) + ":" + String.format("%02d",Minutes) +
                    ":" + String.format("%02d", Sec);
            Time.setText(result);

            handler.postDelayed(this, 0);

        }
    };
    //????????? ?????? ???, ?????? ?????? ?????????
    private class SpeedoActionListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location){
            TextView V = findViewById(R.id.V);
            TextView D = findViewById(R.id.D);
            if (location != null){
                mySpeed = 3.6 * location.getSpeed();
                Double Distance = mySpeed * 10 / 36 ;
                //??????
                V.setText(String.format("%.2f", mySpeed) + " km/h");
                D.setText(String.format( "%.2f",Distance) +" m");
                V.setGravity(Gravity.CENTER);
                D.setGravity(Gravity.CENTER);

            }
        }
        //????????? ????????? ??????
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            // low_indetion of priod.

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
        //????????? ??????????????? ????????? ??????
        setDefaultLocation();



        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ??????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ?????????.)


            startLocationUpdates(); // 3. ?????? ???????????? ??????


        }else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ?????????. 2?????? ??????(3-1, 4-1)??? ??????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ????????? ?????????.
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. ??????????????? ????????? ??????. ?????? ????????? onRequestPermissionResult?????? ?????????.
                        ActivityCompat.requestPermissions( MapsActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ?????? ?????? ??????.
                // ?????? ????????? onRequestPermissionResult?????? ?????????.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // ?????? ???????????? ?????? ????????????
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            //map collection period
            TextView LatLong = findViewById(R.id.latlong);
            TextView Longi = findViewById(R.id.longi);
            TextView Alti = findViewById(R.id.alti);

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "??????:" + location.getLatitude()
                        + "\n??????:" + location.getLongitude() + "\n??????:" + location.getAltitude();

                Log.d(TAG, "onLocationResult : " + markerSnippet);



                LatLong.setText(String.format("%.3f",location.getLatitude()));
                Longi.setText(String.format("%.3f",location.getLongitude()));
                Alti.setText(String.format("%.3f",location.getAltitude()));


                        //?????? ????????? ?????? ???????????? ??????
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;
            }


        }

    };



    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }




    public String getCurrentAddress(LatLng latlng) {

        //GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this,
                    "????????? ????????????", Toast.LENGTH_LONG).show();
            return "????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this,
                    "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this,
                    "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }


    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;

    }



    /*
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ?????????.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;


            // ?????? ???????????? ??????????????? ??????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // ???????????? ??????????????? ?????? ???????????? ??????.
                startLocationUpdates();
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ??? ??????.2 ?????? ????????? ??????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ??? ?????? ??????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }
    public void checkPermission2(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    // ?????? ???????????? ????????? ?????? CSV ????????? ???????????? ?????? ?????????
    public class BackTasking extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
        private Date mDate2;

        // Export CSV??? ???????????? ???????????? ProgressBar
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("????????? ???????????? ???...");
            this.dialog.show();
        }
        // ????????? ?????? ????????? DB??? ?????? ????????? ?????? CSV??? ?????? ??? ???, ????????? ????????? ??????
        //??????: /storage/emulated/0/export
        protected Boolean doInBackground(final String... args) {
            String sltext = Slbtn.getText().toString();
            String eltext = Elbtn.getText().toString();
            String tktext = Trkind.getText().toString();
            String tntext = et_TrainNo.getText().toString();
            mDate2 = new Date(mNow);
            String date = mFormat3.format(mDate2);
            SQLiteHelper myDBHelper = new SQLiteHelper(MapsActivity.this);
            mDTLogHelper.dbInsert(database_name, date, tntext, tktext, sltext, eltext);
            final String CSV = ".csv";
            String currentDBPath = "/data/com.example.trainappol/databases/" + myDBHelper.DATABASE_NAME;
            File dbFile = getDatabasePath(currentDBPath);
            System.out.println(dbFile);
            File exportDir = new File("/storage/emulated/0/export/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }


            File file = new File(exportDir, date + "-" + tntext + "-" + tktext + "-" + sltext + "-" + eltext + CSV);
            try {
                file.createNewFile();
                // ????????? FileWriter ???????????? ??????????????????, ????????? ????????? ????????? ????????????, BufferedWriter??? ????????????, euc-kr ??? ??????????????????.
                CSVWriter csvWrite = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "euc-kr")));
                db = myDBHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("select * from " + myDBHelper.TABLE_NAME, null);
                csvWrite.writeNext(curCSV.getColumnNames());
                //????????? ???????????? DB??? ???????????? ????????? ???, CSV??? ???????????? ??????
                while (curCSV.moveToNext()) {
                    String arrStr[] = null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for (int i = 0; i < curCSV.getColumnNames().length; i++) {
                       mySecondStringArray[i] = curCSV.getString(i);
                       //System.out.println(curCSV.getString(i));
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                dbFile.delete();
                return true;
            } catch (IOException e) {
                Log.e("MapsActivity", e.getMessage(), e);
                return false;
            }
        }
        //doInBackground ?????? ???????????? true?????? ?????? ??????, false?????? ?????? ??????.
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                //Toast.makeText(MapsActivity.this, "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mLayout,"????????????(export)??? ????????? ????????? ?????????????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            } else {
                //Toast.makeText(MapsActivity.this, "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mLayout,"????????? ????????? ?????????????????????. \n???????????? ?????? ??? ??????????????? ????????? ???, ??????????????? ??????????????? ?????? ??????????????? ??????????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            }
        }
    }



    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }



}