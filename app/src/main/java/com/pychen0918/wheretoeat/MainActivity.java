package com.pychen0918.wheretoeat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kobakei.ratethisapp.RateThisApp;
import com.pychen0918.wheretoeat.adapter.IndexedHashMapAdapter;
import com.pychen0918.wheretoeat.common.Constants;
import com.pychen0918.wheretoeat.common.Credential;
import com.pychen0918.wheretoeat.common.GlobalVariables;
import com.pychen0918.wheretoeat.common.IndexedHashMap;
import com.pychen0918.wheretoeat.common.QueryResults;
import com.pychen0918.wheretoeat.common.Util;
import com.pychen0918.wheretoeat.googleplace.GooglePlaceListQueryTask;
import com.pychen0918.wheretoeat.googleplace.QueryTaskResponse;
import com.pychen0918.wheretoeat.view.InstantAutoCompleteTextView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mLocationAvailable = false;
    private int mDistance = 500;
    private String mType = "food";
    private String mKeyword = "";
    private LinkedHashMap<String, String> mHistory = null;
    private boolean mOpenNow = false;
    private boolean mUseIu = false;
    private QueryResults queryResults = null;
    private ProgressDialog progressDialog = null;
    QueryTaskResponse startSingleResultActivity = new QueryTaskResponse() {
        @Override
        public void finishListQueryTask(boolean status, String output) {
            queryResults.setQueryInProgress(false);
            progressDialog.cancel();
            if (status && queryResults.parseListResult(output)) {
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();

                queryResults.setLastQueryTimeToNow();
                queryResults.markAsClean();
                queryResults.updateRestaurantDistance(lat, lon);

                Util.startSingleResultActivity(MainActivity.this, queryResults.getRandomIndex(), mUseIu);
            } else {
                Util.showGooglePlaceApiErrorMessage(MainActivity.this);
            }
        }
    };
    QueryTaskResponse startListResultActivity = new QueryTaskResponse() {
        @Override
        public void finishListQueryTask(boolean status, String output) {
            queryResults.setQueryInProgress(false);
            progressDialog.cancel();
            if (status && queryResults.parseListResult(output)) {
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();

                queryResults.setLastQueryTimeToNow();
                queryResults.markAsClean();
                queryResults.updateRestaurantDistance(lat, lon);

                Util.startListResultActivity(MainActivity.this, queryResults.getTypeMap().get(mType), mUseIu);
            } else {
                Util.showGooglePlaceApiErrorMessage(MainActivity.this);
            }
        }
    };
    private SpinnerEventListener spinnerEventListener = new SpinnerEventListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Enable AD
        Util.initAdView((AdView) findViewById(R.id.main_adview));

        // Try to get google api client for current location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // If query result is empty, this is the first time we start App
        // Initialize some variables
        if (queryResults == null) {
            queryResults = ((GlobalVariables) this.getApplicationContext()).queryResults;
            queryResults.setTypeMap(IndexedHashMap.readXmlToIndexedHashMap(this, R.xml.restaurant_type_map));
            queryResults.setDistanceSiMap(IndexedHashMap.readXmlToIndexedHashMap(this, R.xml.distance_si_map));
            queryResults.setDistanceIuMap(IndexedHashMap.readXmlToIndexedHashMap(this, R.xml.distance_iu_map));
            queryResults.setOpenNowMap(IndexedHashMap.readXmlToIndexedHashMap(this, R.xml.yes_no_map));
            queryResults.initLocalLanguage();
        }

        // Rate this App config
        Util.initialRateThisApp(this);
        RateThisApp.onStart(this);
        RateThisApp.showRateDialogIfNeeded(this);

        // Initialize search config preference
        PreferenceManager.setDefaultValues(this, R.xml.settings_preference, false);
        loadPreference(this, getString(R.string.preference_key));
        if (mHistory == null) {
            mHistory = new LinkedHashMap<>(Constants.maxKeywordHistory, 0.75f, true);
        }
        setAutoCompleteSource();

        // Initialize search view elements
        if(!mUseIu) {
            createSpinnerAndAdapter(R.id.spinner_distance, queryResults.getDistanceSiMap(),
                    queryResults.getDistanceSiMap().getIndex(String.valueOf(mDistance)));
        }
        else{
            createSpinnerAndAdapter(R.id.spinner_distance, queryResults.getDistanceIuMap(),
                    queryResults.getDistanceIuMap().getIndex(String.valueOf(mDistance)));
        }
        createSpinnerAndAdapter(R.id.spinner_type, queryResults.getTypeMap(),
                queryResults.getTypeMap().getIndex(mType));
        createSpinnerAndAdapter(R.id.spinner_open_now, queryResults.getOpenNowMap(),
                queryResults.getOpenNowMap().getIndex(String.valueOf(mOpenNow)));
        InstantAutoCompleteTextView autoCompleteTextView = (InstantAutoCompleteTextView) findViewById(R.id.autotext_keyword);
        autoCompleteTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        toggleCustomSearch();
    }

    private void setAutoCompleteSource() {
        InstantAutoCompleteTextView textView = (InstantAutoCompleteTextView) findViewById(R.id.autotext_keyword);
        ArrayList<String> arrayList = new ArrayList<>(mHistory.values());
        Collections.reverse(arrayList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, arrayList);
        textView.setAdapter(adapter);
    }

    private void addSearchInput(String input) {
        String upperInput = input.toUpperCase();
        if (!mHistory.containsKey(upperInput)) {
            mHistory.put(upperInput, input);
        } else {
            // Make it the most recently used one
            mHistory.get(upperInput);
        }
        setAutoCompleteSource();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Util.startSettingsActivity(this);
                return true;
            case R.id.action_rate_app:
                RateThisApp.showRateDialog(this);
                return true;
            case R.id.action_share_app:
                Util.startShareAppActivity(this);
                return true;
            case R.id.action_about:
                Util.showAboutDialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createSpinnerAndAdapter(int spinnerId, IndexedHashMap data, int position) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        if (spinner == null)
            return;
        if(!(spinner.getAdapter() instanceof IndexedHashMapAdapter)) {
            IndexedHashMapAdapter adapter = new IndexedHashMapAdapter(this, android.R.layout.simple_spinner_item, android.R.layout.select_dialog_singlechoice, data);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(spinnerEventListener);
            spinner.setOnTouchListener(spinnerEventListener);
            spinner.setSelection(position);
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        savePreference(this, getString(R.string.preference_key));
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean oldUseIu = mUseIu;

        loadPreference(this, getString(R.string.preference_key));
        setAutoCompleteSource();

        if(oldUseIu != mUseIu) {
            queryResults.markAsDirty();
            Spinner spinner = (Spinner) findViewById(R.id.spinner_distance);
            IndexedHashMapAdapter adapter = (IndexedHashMapAdapter) spinner.getAdapter();
            if (!mUseIu) {
                adapter.update(queryResults.getDistanceSiMap());
                spinner.setSelection(0);
                mDistance = 500;
            } else {
                adapter.update(queryResults.getDistanceIuMap());
                spinner.setSelection(0);
                mDistance = 805;
            }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLocationAvailable = true;
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_no_location_info), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        mLocationAvailable = true;
                    }

                } else {
                    // permission denied
                    mLocationAvailable = false;
                    if (mGoogleApiClient != null)
                        mGoogleApiClient.disconnect();
                    Toast.makeText(this, getResources().getString(R.string.error_need_location_permission), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onClick(View view) {
        int radius = mDistance;
        String type = mType;
        InstantAutoCompleteTextView autoTextKeyword = (InstantAutoCompleteTextView) findViewById(R.id.autotext_keyword);
        String keyword = autoTextKeyword.getText().toString();
        if (type.equals("custom_search")) {
            if (keyword.length() == 0) {
                autoTextKeyword.setError(getString(R.string.info_no_keyword));
                Toast.makeText(this, getString(R.string.info_no_keyword), Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!keyword.equals(mKeyword)) {
                    queryResults.markAsDirty();
                    mKeyword = keyword;
                }
            }
        }

        if (view.getId() == R.id.button_random) {
            if (!isNetworkConnected()) {
                Toast.makeText(this, getResources().getString(R.string.error_no_network_connection), Toast.LENGTH_SHORT).show();
            }
            if (!mLocationAvailable) {
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.reconnect();
                }
                return;
            }
            if (queryResults.isDataValid()) {
                Util.startSingleResultActivity(this, queryResults.getRandomIndex(), mUseIu);
            } else if (!queryResults.isQueryInProgress()) {
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();
                startListQueryTask(lat, lon, radius, type, keyword, startSingleResultActivity);
            }
        } else if (view.getId() == R.id.button_list) {
            if (!isNetworkConnected()) {
                Toast.makeText(this, getResources().getString(R.string.error_no_network_connection), Toast.LENGTH_SHORT).show();
            }
            if (!mLocationAvailable) {
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.reconnect();
                }
                return;
            }
            if (queryResults.isDataValid()) {
                Util.startListResultActivity(this, queryResults.getTypeMap().get(mType), mUseIu);
            } else if (!queryResults.isQueryInProgress()) {
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();
                startListQueryTask(lat, lon, radius, type, keyword, startListResultActivity);
            }
        }
    }

    private void startListQueryTask(double lat, double lon, int radius, String type, String keyword, QueryTaskResponse callback) {
        String params = "location=" + lat + "," + lon
                + "&radius=" + radius
                + "&language=" + queryResults.getLocaleLanguage()
                + "&key=" + Credential.googleApiKey;
        if (mOpenNow) {
            params = params + "&opennow";
        }
        if (mType.equals("custom_search")) {
            addSearchInput(keyword);
            try {
                params = params + "&keyword=" + URLEncoder.encode(keyword, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                params = params + "&types=food";  // fall back to default search
            }
        } else {
            params = params + "&types=" + type;
        }
        String postUrl = Constants.googleApiUrl + "?" + params;

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getResources().getString(R.string.loading));
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
        }
        progressDialog.show();

        queryResults.setQueryInProgress(true);
        GooglePlaceListQueryTask queryTask = new GooglePlaceListQueryTask();
        queryTask.setTaskResponse(callback);
        queryTask.execute(postUrl);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private class SpinnerEventListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        boolean userSelect = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            IndexedHashMap indexedHashMap;
            if (userSelect) {
                if (adapterView.getId() == R.id.spinner_distance) {
                    queryResults.markAsDirty();
                    if(!mUseIu)
                        indexedHashMap = queryResults.getDistanceSiMap();
                    else
                        indexedHashMap = queryResults.getDistanceIuMap();

                    mDistance = Integer.parseInt(indexedHashMap.getKey(i));
                    if (mDistance == 0) {
                        mDistance = 500;
                    }
                } else if (adapterView.getId() == R.id.spinner_type) {
                    queryResults.markAsDirty();
                    indexedHashMap = queryResults.getTypeMap();
                    mType = indexedHashMap.getKey(i);
                    if (mType == null || mType.isEmpty()) {
                        mType = "food";
                    }
                    toggleCustomSearch();
                } else if (adapterView.getId() == R.id.spinner_open_now) {
                    queryResults.markAsDirty();
                    indexedHashMap = queryResults.getOpenNowMap();
                    mOpenNow = Integer.parseInt(indexedHashMap.getKey(i)) > 0;
                }
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private void toggleCustomSearch(){
        LinearLayout container = (LinearLayout) findViewById(R.id.custom_search_container);
        if (mType.equals("custom_search")) {
            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void loadPreference(Context context, String preferenceFile) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUseIu = defaultSharedPreferences.getBoolean(getString(R.string.use_iu_key), false);

        SharedPreferences sharedPref = context.getSharedPreferences(
                preferenceFile, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        if(!mUseIu)
            mDistance = sharedPref.getInt("DISTANCE", 500);
        else
            mDistance = sharedPref.getInt("DISTANCE", 805);
        mType = sharedPref.getString("TYPE", "food");
        mOpenNow = sharedPref.getBoolean("OPENNOW", false);
        String s = sharedPref.getString("HISTORY", "{}");
        Type type = new TypeToken<LinkedHashMap<String, String>>() {
        }.getType();
        mHistory = gson.fromJson(s, type);
    }

    private void savePreference(Context context, String preferenceFile) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                preferenceFile, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("DISTANCE", mDistance);
        editor.putString("TYPE", mType);
        editor.putBoolean("OPENNOW", mOpenNow);
        editor.putString("HISTORY", gson.toJson(mHistory));
        editor.apply();
    }
}
