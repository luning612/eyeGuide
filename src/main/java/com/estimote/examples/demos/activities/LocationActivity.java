package com.estimote.examples.demos.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.examples.demos.Location;
import com.estimote.examples.demos.LocationMgr;
import com.estimote.examples.demos.R;
import com.estimote.examples.demos.adapters.LocationReportAdapter;
import com.estimote.examples.demos.adapters.NearableListAdapter;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.estimote.sdk.eddystone.Eddystone;
import com.estimote.sdk.internal.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visualizes distance from beacon to the device.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
//public class LocationActivity extends BaseActivity {
public class LocationActivity extends AppCompatActivity {
  private static final String TAG = LocationActivity.class.getSimpleName();


  private BeaconManager beaconManager;
  private final String BACKEND_ENDPOINT = "";
  private final String USER_ID = "111";
  private RequestQueue mRequestQueue;
  private LocationMgr locMgr;
  private Nearable currNearable;
  private Eddystone currEddystone;

  //public final List<Nearable> nearables = new ArrayList<>();

//  @Override protected int getLayoutResId() {
//    return R.layout.location;
//  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.location);
    L.enableDebugLogging(true);
    locMgr = new LocationMgr();
    //nearable = getIntent().getExtras().getParcelable(ListNearablesActivity.EXTRAS_NEARABLE);
    beaconManager = new BeaconManager(getApplicationContext());
    //beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
    if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
      L.d("in first start scanning");
      startScanning();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    L.d("in an resume");
    if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
      startScanning();
    }

  }
  protected void startScanning() {
    //toolbar.setSubtitle("Scanning...");
    L.d("in start scanning");
    beaconManager.setNearableListener(new BeaconManager.NearableListener() {
      @Override
      public void onNearablesDiscovered(List<Nearable> nearables) {
        Log.d("prep", "Nearby nearables: " + nearables);
        if (nearables.size() > 0) {
          processNearable(nearables.get(0));
        }
      }
    });
    beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
      @Override
      public void onEddystonesFound(List eddystones) {
        Log.d("prep", "Nearby eddystones: " + eddystones);
        if (eddystones.size() > 0) {
          processNearable(eddystones.get(0));
        }
      }
    });
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        beaconManager.startNearableDiscovery();
        beaconManager.startEddystoneScanning();
      }
    });
  }

  @Override
  protected void onStop() {
    //stop beacon manager services
    beaconManager.disconnect();
    //purge http request queue
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(this);
    }
    super.onStop();
  }
  private void processNearable (Object beacon){
    if (beacon instanceof Nearable) {
      Nearable nearable = (Nearable)beacon;
      if (null==currNearable || !currNearable.equals(beacon)) {
        currNearable = nearable;
        Toast.makeText(getApplicationContext(), nearable.identifier, Toast.LENGTH_LONG).show();
        Log.w("ID", "nearable id " + nearable.identifier);
        Location loc = locMgr.getBeaconLocation(nearable.identifier);
        List<Location> nearbyLocs = locMgr.getNearbyLocations(loc);

        sendToBackend(loc.getName());
        L.d("nearable.identifier");
        ((TextView) findViewById(R.id.macc))
                .setText(String.format("ID: %s (%s)", nearable.identifier, Utils.computeProximity(nearable).toString()));
        ((TextView) findViewById(R.id.rssic))
                .setText("RSSI: " + nearable.rssi);
        ((TextView) findViewById(R.id.locc))
                .setText("Location: " + loc.getName());
        ((TextView) findViewById(R.id.loctxtc))
                .setText("MPower: " + loc.getText() + LocationMgr.listToString(nearbyLocs));
      }
    }else if (beacon instanceof Eddystone){
      Eddystone eddystone = (Eddystone)beacon;
      if (null==currEddystone || !currEddystone.equals(beacon)) {
        currEddystone = eddystone;
        String eddyStoneId = eddystone.instance;
        Log.w("ID", "eddystone id " + eddyStoneId);
        Toast.makeText(getApplicationContext(),eddyStoneId , Toast.LENGTH_LONG).show();
        Location loc = locMgr.getBeaconLocation(eddyStoneId);
        List<Location> nearbyLocs = locMgr.getNearbyLocations(loc);

        sendToBackend(loc.getName());
        L.d("nearable.identifier");
        ((TextView) findViewById(R.id.macc))
                .setText(String.format("ID: %s (%s)", eddyStoneId, Utils.computeProximity(eddystone).toString()));
        ((TextView) findViewById(R.id.rssic))
                .setText("RSSI: " + eddystone.rssi);
        ((TextView) findViewById(R.id.locc))
                .setText("Location: " + loc.getName());
        ((TextView) findViewById(R.id.loctxtc))
                .setText("MPower: " + loc.getText() + LocationMgr.listToString(nearbyLocs));
      }
    }
  }

  private void sendToBackend(String locSlug) {

    RequestQueue queue = Volley.newRequestQueue(this);
    String url =BACKEND_ENDPOINT+"?uid="+USER_ID+"&"+"locid"+locSlug;

// Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                L.d("get my response");
              }
            }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        L.d("Request didn't work");
      }
    });
// Add the request to the RequestQueue.
    queue.add(stringRequest);
  }
}
