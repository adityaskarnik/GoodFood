package com.aditya.goodfood;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static com.aditya.goodfood.utils.AppUtils.isLocationEnabled;

/**
 * Created by Aditya PC on 12/25/2016.
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        View.OnClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapLongClickListener, AdapterView.OnItemClickListener {

    public static final String GPS = "gps";
    public static final String ANDROIDSETTINGS = "com.android.settings";
    public static final String SETTINGSPROVIDER = "com.android.settings.widget.SettingsAppWidgetProvider";
    public static final String URIPARSE = "3";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    Context mContext;
    AutoCompleteTextView autoCompView1;
    private GoogleMap mMap;
    private Location currentLocation;
    private static final String PLACES_API_BASE1 = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE1 = "/nearbysearch";
    private static final String OUT_JSON1 = "/json";
    private static final String API_KEY = "AIzaSyDwei3t9XMZ2YWb6O2wenEzLOgWSaTepZI";
    private ArrayList<PlaceObject> resultList;
    SpotsDialog progressDialog;
    List<Marker> markerList = new ArrayList<Marker>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (isLocationEnabled(MapsActivity.this) == false) {
            Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
            startActivity(settings);
        }
        turnGPSOn();
        mContext = this;
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkGooglePlayServices();

        autoCompView1 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView1.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView1.setOnItemClickListener(this);
    }

    private class BackgroundOperation extends AsyncTask<Object, Object, ArrayList<PlaceObject>> {


        @Override
        protected ArrayList<PlaceObject> doInBackground(Object... params) {
            publishProgress(params[0]);
            resultList = autocomplete("");
            Geocoder gc = new Geocoder(mContext);
            Double lat = null, lon = null;
            LatLng user;
            final Marker[] marker = new Marker[1];
            try {
                for (int i = 0; i < resultList.size(); i++) {

                    List<Address> addressList = gc.getFromLocationName(resultList.get(i).getPlaceName()+","+resultList.get(i).getPlaceVicinity(), 5);
                    if (addressList.size() > 0) {
                        lat = addressList.get(0).getLatitude();
                        lon = addressList.get(0).getLongitude();
                        user = new LatLng(lat, lon);
                        final MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title(resultList.get(i).getPlaceName())
                                .snippet(resultList.get(i).getPlaceRating())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant));
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                marker[0] = mMap.addMarker(markerOptions);
                            }
                        });
                        markerList.add(marker[0]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(final ArrayList<PlaceObject> result) {
            progressDialog.dismiss();
            Log.wtf("BackgroundOperationPostExecute","");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SpotsDialog(MapsActivity.this, "Finding places nearby..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            Log.d("MyAsyncTask","onProgressUpdate");
            super.onProgressUpdate(values);
        }
    }

    private void turnGPSOn() {
        String provider = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        if (!provider.contains(GPS)) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName(ANDROIDSETTINGS, SETTINGSPROVIDER);
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse(URIPARSE));
            sendBroadcast(poke);
            // startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private void checkGooglePlayServices() {
        if (checkPlayServices()) {
            if (!isLocationEnabled(mContext)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setMessage("Location Not Enabled");
                dialog.setPositiveButton("Open Location Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            buildGoogleApiClient();
        } else {
            Toast.makeText(mContext, "Location Not Supported in this device", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("onConnected", "onConnected");
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("onConnected", "onConnected Catch");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed", "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "onLocationChanged");
        try {
            currentLocation = location;
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                try {
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mGoogleApiClient, MapsActivity.this);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            new BackgroundOperation().execute(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("onLocationChanged", "onLocationChanged Catch");
        }
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "onMapReady");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings();
        mMap.setInfoWindowAdapter(new MakerInfoWindowAdapter());
    }

    class MakerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MakerInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            RatingBar mRatingBarSnippet = ((RatingBar)myContentsView.findViewById(R.id.ratingSnippet));
            if (marker.getSnippet()!=null && !marker.getSnippet().equals("")) {
                mRatingBarSnippet.setRating(Float.parseFloat(marker.getSnippet()));
            } else
                mRatingBarSnippet.setRating(0);

            return myContentsView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        LatLng latLong = null;
        Marker searchedMarker = null;
        Double lat, lon;
        String strNew = (String) adapterView.getItemAtPosition(position);

        LatLng user = null;
        Geocoder gc = new Geocoder(mContext);
        try {
            List<Address> addressList = gc.getFromLocationName(strNew.toString(), 5);
            if (addressList.size() > 0) {
                lat = addressList.get(0).getLatitude();
                lon = addressList.get(0).getLongitude();
                user = new LatLng(lat, lon);
                latLong = new LatLng(lat, lon);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(16f).build();
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }
            if (searchedMarker!=null) {
                searchedMarker.remove();
            }
            searchedMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLong)
                    .title(resultList.get(position).getPlaceName())
                    .snippet(resultList.get(position).getPlaceRating())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant)));
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {


        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            if(!resultList.get(index).getPlaceVicinity().equals("")) {
                return resultList.get(index).getPlaceName() + ", " + resultList.get(index).getPlaceVicinity();
            }else{
                return resultList.get(index).getPlaceName();
            }
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        ArrayList<PlaceObject> resultListTemp = new ArrayList<PlaceObject>();
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        //resultListTemp = resultList;
                        // Assign the data to the FilterResults

                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    class PlaceObject {
        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        private String placeName;

        public String getPlaceVicinity() {
            return placeVicinity;
        }

        public void setPlaceVicinity(String placeVicinity) {
            this.placeVicinity = placeVicinity;
        }

        private String placeVicinity;

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        private String placeId;

        public String getPlaceRating() {
            return placeRating;
        }

        public void setPlaceRating(String placeRating) {
            this.placeRating = placeRating;
        }

        private String placeRating;

        public PlaceObject(String pN, String pVicinity, String pID, String pRating) {
            placeId = pID;
            placeName = pN;
            placeVicinity = pVicinity;
            placeRating = pRating;
        }

    }

    public ArrayList<PlaceObject> autocomplete(String input) {
        ArrayList<PlaceObject> resultList = null;
        HttpURLConnection conn = null;
        HttpURLConnection conn1 = null;
        StringBuilder jsonResults1 = new StringBuilder();
        //Example url
        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=12.9971226,77.5862544&radius=5000
        // &type=cafe,restaurant,bakery,bar,meal_delivery,meal_takeaway,night_club&keyword=nan
        // &key=AIzaSyDwei3t9XMZ2YWb6O2wenEzLOgWSaTepZI
        try {
            //nearbyAPI
            StringBuilder sb1 = new StringBuilder(PLACES_API_BASE1 + TYPE_AUTOCOMPLETE1 + OUT_JSON1);
            sb1.append("?location=" + currentLocation.getLatitude()+","+currentLocation.getLongitude());
            sb1.append("&radius=" + 25000);
            sb1.append("&type=food,cafe,restaurant,bakery,bar,meal_delivery,meal_takeaway,night_club" );
            sb1.append("&keyword=" + URLEncoder.encode(input, "utf8"));
            sb1.append("&key=" + API_KEY);
            //sb.append("&components=country:gr");
            //sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url1 = new URL(sb1.toString());

            System.out.println("URL1: " + url1);
            conn1 = (HttpURLConnection) url1.openConnection();
            InputStreamReader in1 = new InputStreamReader(conn1.getInputStream());
            // Load the results into a StringBuilder
            int read1;
            char[] buff1 = new char[1024];
            while ((read1 = in1.read(buff1)) != -1) {
                jsonResults1.append(buff1, 0, read1);
            }

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in1.read(buff)) != -1) {
                jsonResults1.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("GoodFood", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("GoodFood", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (conn1 != null) {
                conn1.disconnect();
            }
        }

        try {
            //nearbyplaces api
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj1 = new JSONObject(jsonResults1.toString());
            JSONArray predsJsonArray1 = jsonObj1.getJSONArray("results");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<PlaceObject>();

            for (int j = 0; j < predsJsonArray1.length(); j++) {
                System.out.println(predsJsonArray1.getJSONObject(j).getString("name"));
                System.out.println("============================================================");
                PlaceObject pOBject1;
                if (!predsJsonArray1.getJSONObject(j).has("rating")) {
                    pOBject1 = new PlaceObject(predsJsonArray1.getJSONObject(j).getString("name"), predsJsonArray1.getJSONObject(j).getString("vicinity"), predsJsonArray1.getJSONObject(j).getString("reference"), "");
                } else {
                    pOBject1 = new PlaceObject(predsJsonArray1.getJSONObject(j).getString("name"), predsJsonArray1.getJSONObject(j).getString("vicinity"), predsJsonArray1.getJSONObject(j).getString("reference"), predsJsonArray1.getJSONObject(j).getString("rating"));
                }
                resultList.add(pOBject1);
            }

        } catch (JSONException e) {
            Log.e("GoodFood", "Cannot process JSON results", e);
        }
        return resultList;
    }
}
