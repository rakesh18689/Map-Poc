package map.biswojit.poc;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback , LocationListener {

    private static final long LOCATION_REFRESH_TIME = 3000;
    private static final float LOCATION_REFRESH_DISTANCE = 5000;
    private static final long MIN_TIME_BW_UPDATES = 3000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 3000;
    private GoogleMap mMap;
    private SharedPreferences sharedPreferences;
    private int locationCount = 0;
    private boolean isLocation = false;
    private HashMap<String, LatLng> selectedLatLng;
    private Spinner Sp_PostalCodes;
    private ArrayList<String> arrayPostalCodes = new ArrayList<String>();
    private ArrayList<String> finalarrayPostalCodes = new ArrayList<String>();
    private ArrayAdapter<String> spinnerArrayAdapter;
    private LocationManager mLocationManager;
    private List<Marker> mMarkers = new ArrayList<Marker>();
    private boolean canGetLocation;
    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    LocationManager locationManager;
    boolean isGPSEnabled ;
    boolean isNetworkEnabled;

    Location location;
    Double latitude;
    Double longitude;
    LatLng currentLatLng ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
       
        sharedPreferences = getSharedPreferences("location", 0);
        locationCount = sharedPreferences.getInt("locationCount", 0);
        locationCount = 0;
        Sp_PostalCodes = (Spinner) findViewById(R.id.sp_postal);
        selectedLatLng = new HashMap<>();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, finalarrayPostalCodes);
        Sp_PostalCodes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(parent.getItemAtPosition(pos).toString().equals("All")){
                    //if(cost_spinner_count > 0){
                    ShowAllMarker();

                    //}
                    //cost_spinner_count++;
                    //activity.setCost(parent.getItemAtPosition(pos).toString());
                }else{
                    ShowMarker(pos);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    private void LoadAllMarker() {  // Loads once from sharedpreference once the app starts
        String lat = "";
        String lng = "";
        String postalCode = "";
        LatLng latlng;
        if (arrayPostalCodes.size() > 0)
            arrayPostalCodes.clear();

        for(int i=0;i<locationCount;i++){
            lat = sharedPreferences.getString("lat"+i,"0");
            lng = sharedPreferences.getString("lng"+i,"0");
            postalCode = sharedPreferences.getString("Postalcode"+i,"0");

            latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            arrayPostalCodes.add(postalCode);
            selectedLatLng.put(postalCode,latlng);
            drawMarker(latlng,postalCode);
        }
        finalarrayPostalCodes.clear();
        finalarrayPostalCodes.addAll(arrayPostalCodes);
        finalarrayPostalCodes.add("All");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));

        Sp_PostalCodes.setAdapter(spinnerArrayAdapter);
    }
    private void ShowAllMarker() {    // shows all markers
        mMap.clear();
        String lat = "";
        String lng = "";
        String postalCode = "";
        LatLng latlng;
        if (arrayPostalCodes.size() > 0)
            arrayPostalCodes.clear();

        for(int i=0;i<locationCount;i++){
            lat = sharedPreferences.getString("lat"+i,"0");
            lng = sharedPreferences.getString("lng"+i,"0");
            postalCode = sharedPreferences.getString("Postalcode"+i,"0");

            latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            arrayPostalCodes.add(postalCode);
            selectedLatLng.put(postalCode,latlng);
            drawMarker(latlng,postalCode);
        }

    }

    private void ShowMarker(int pos) {
        mMap.clear();
        String postalcode = sharedPreferences.getString("Postalcode"+pos,"0");
        drawMarker(selectedLatLng.get(postalcode),postalcode);
        mMarkers.get(pos).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatLng.get(postalcode)));
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

            int i = 1;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            checkPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);


        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            this.canGetLocation = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                Log.d("activity", "LOC Network Enabled");
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Log.d("activity", "LOC by Network");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                    Log.d("activity", "RLOC: GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            Log.d("activity", "RLOC: loc by GPS");

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        }
        if( latitude != null){
            currentLatLng = new LatLng(latitude, longitude);
            addMarker( currentLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,17));
        }

                            //addMarker(point);
        //mMap.setOnInfoWindowClickListener(getInfoWindowClickListener());
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                addMarker(point);

            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                resetCache();

            }
        });
        startTimer();
        if(locationCount!=0){
            LoadAllMarker();
        }

    }

    private void addMarker(LatLng point){   //adds marker and saves it into sharedpreference
        locationCount++;
        String PostalCode ="";
        Geocoder geo = new Geocoder(getApplicationContext(),
                Locale.getDefault());
        List<Address> add;
        try {

            add = geo.getFromLocation(point.latitude, point.longitude, 1);
            PostalCode = add.get(0).getPostalCode(); //u'll get postal code in addstr

        }
        catch(Exception e){
            Log.e("Exception",e.toString());
        }
        drawMarker(point,PostalCode);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int locationCountPos = locationCount;

        if(arrayPostalCodes.indexOf(PostalCode) != -1){
            locationCountPos = arrayPostalCodes.indexOf(PostalCode);
             Toast.makeText(getBaseContext(), "Updated location for PostalCode", Toast.LENGTH_SHORT).show();
            locationCount-- ;
            locationCountPos++ ;
        }

        editor.putString("lat"+ Integer.toString((locationCountPos-1)), Double.toString(point.latitude));
        editor.putString("lng"+ Integer.toString((locationCountPos-1)), Double.toString(point.longitude));

        editor.putString("Postalcode"+ Integer.toString((locationCountPos-1)), PostalCode);
        editor.putInt("locationCount", locationCount);
        editor.commit();
        selectedLatLng.put(PostalCode,point);

        arrayPostalCodes.add((locationCountPos-1),PostalCode);
        finalarrayPostalCodes.clear();
        finalarrayPostalCodes.addAll(arrayPostalCodes);
        finalarrayPostalCodes.add("All");
        spinnerArrayAdapter.notifyDataSetChanged();

        if(arrayPostalCodes.indexOf(PostalCode) != -1){
            ShowAllMarker();
        }

        Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();
    }

    private void drawMarker(LatLng point, String markerText){   //draws marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point).title(markerText);

        mMarkers.add(mMap.addMarker(markerOptions));
    }




    private boolean checkPermission() {

        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
           // Toast.makeText(MapsActivity.this, getString(R.string.permission_agreement), Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 1) {
            Map<String, Integer> perms = new HashMap<String, Integer>();
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);

            if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isLocation = true;
            } else {
                isLocation = false;
                //Toast.makeText(MapsActivity.this,getString(R.string.loc_permission), Toast.LENGTH_SHORT).show();
            }
            if (isLocation) {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private void startTimer(){
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                       resetCache();
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 60000, 60000);    //Reset after 60 seconds
    }

    private void resetCache() {
        mMap.clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        locationCount=0;
        addMarker( currentLatLng);
        arrayPostalCodes.clear();
        finalarrayPostalCodes.clear();
        finalarrayPostalCodes.add("All");
        selectedLatLng.clear();
        mMarkers.clear();
        spinnerArrayAdapter.notifyDataSetChanged();
    }
}
