package com.estimote.examples.demos.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.examples.demos.Location;
import com.estimote.examples.demos.LocationMgr;
import com.estimote.examples.demos.R;
import com.estimote.examples.demos.Speaker;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.MacAddress;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.estimote.sdk.eddystone.Eddystone;
import com.estimote.sdk.eddystone.EddystoneTelemetry;
import com.estimote.sdk.internal.utils.L;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Visualizes distance from beacon to the device.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
//public class LocationActivity extends BaseActivity {
public class LocationActivity extends Activity {
  private static final String TAG = LocationActivity.class.getSimpleName();
  private BeaconManager beaconManager;
  //private final String BACKEND_ENDPOINT = "http://symplcms.com:9001/api/object/create";
  private final String BACKEND_ENDPOINT = "https://burning-torch-746.firebaseio.com/1.json";
  private final String USER_ID = "111";
  private final int CHECK_CODE = 0x1;
  private final int LONG_DURATION = 5000;
  private final int SHORT_DURATION = 1200;
  private final int OUT_OF_RANGE_THRESHOLD = 10;


  private RequestQueue mRequestQueue;
  private LocationMgr locMgr;
  //private Object currNearable;
  private String currNearableId;
  private String currNearableDistance;
  private Speaker speaker;

  private int noNearableCounter = 0;

  private Location destination = null;
  private String state = "INITIAL";
  private Location[] lastLoc = new Location[2];

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.location);
    L.enableDebugLogging(true);
    mRequestQueue = Volley.newRequestQueue(this);
    locMgr = new LocationMgr();
    //nearable = getIntent().getExtras().getParcelable(ListNearablesActivity.EXTRAS_NEARABLE);
    beaconManager = new BeaconManager(getApplicationContext());
    Location destinationLoc = (Location) getIntent().getSerializableExtra("location");
    if(destinationLoc!=null){
      destination = destinationLoc;
    }
//    checkTTS();
    speaker = new Speaker(this);
    //beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
  }

  @Override
  protected void onResume() {
    super.onResume();
    L.d("in an resume");
    if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
      setListeners();
    }

  }
  protected void setListeners() {
    //toolbar.setSubtitle("Scanning...");
    beaconManager.setNearableListener(new BeaconManager.NearableListener() {
      @Override
      public void onNearablesDiscovered(List<Nearable> nearables) {
        if (nearables.size() > 0) {
          //debug
          Log.i("prep", "Nearby nearables: " + nearables);
          if (nearables.size() > 1 && nearables.get(1).rssi > nearables.get(0).rssi) {
            Log.e("nearable list", "Nearby eddystones: " + nearables);
          }
          //end debug
          Nearable nearestNearable = nearables.get(0);// get nearest nearable
          String distance = Utils.computeProximity(nearestNearable).toString();
          String id = nearestNearable.identifier;
          Log.i("prep", "nearable: " + id+ " "+distance);
          processNearable(distance, id);
        } else {
          processNoNearable();
        }
      }
    });
    beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
      @Override
      public void onEddystonesFound(List<Eddystone> eddystones) {
        Log.i("prep", "Nearby eddystones: " + eddystones);
        if (eddystones.size() > 0) {
          Eddystone nearestEddy = eddystones.get(0);
          String distance = Utils.computeProximity(nearestEddy).toString();
          String id = nearestEddy.instance;
          processNearable(distance, id);
        } else {
          processNoNearable();
        }
      }
    });
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        //beaconManager.startNearableDiscovery();
        beaconManager.startEddystoneScanning();
      }
    });
  }
  @Override
  protected void onPause(){

    super.onPause();
  }
  @Override
  protected void onStop() {
    //stop beacon manager services
    beaconManager.disconnect();
    //purge http request queue
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(this);
    }
    speaker.destroy();
    super.onStop();
  }
//  private void checkTTS() {
//    Intent check = new Intent();
//    check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//    startActivityForResult(check, CHECK_CODE);
//  }
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == CHECK_CODE){
      if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
        speaker = new Speaker(this);
      }else {
        Intent install = new Intent();
        install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(install);
      }
    }
  }

  private void processNearable (String distance, String id) {
    String lastNearableId = currNearableId;
    String lastNearableDistance = currNearableDistance;
    if ( !id.equals(lastNearableId) || !distance.equals(lastNearableDistance)) {
      currNearableId = id;
      currNearableDistance = distance;
      Location loc = locMgr.getBeaconLocation(id);
      if (null == loc) {
        Log.i("process nearable", "location not found");
        return;
      }
      lastLoc[0] = lastLoc[1];
      lastLoc[1] = loc;
      List<Location> nearbyLocs = locMgr.getNearbyLocations(loc);
      if (!id.equals(lastNearableId)) {// a different nearable
        Log.i("sending to backend", loc.getName());
        state = "NOMAL";
        sendToBackend(loc.getName());
        display(distance,loc.getFloor(), loc.getName(), loc.getText(), LocationMgr.listToString(nearbyLocs));
      }
      if (id.equals(lastNearableId) && !distance.equals(lastNearableDistance)) {
        if (state.equals("NOMAL")){
          updateDistance(distance, loc.getName());
        }else{
          display(distance,loc.getFloor(), loc.getName(), loc.getText(), LocationMgr.listToString(nearbyLocs));
        }
      }
    }
  }
  private void processNoNearable(){
    noNearableCounter +=1;
    if(noNearableCounter>=OUT_OF_RANGE_THRESHOLD){
      displayNoNearable();
      state = "NO_NEARABLE";
      noNearableCounter =0;
    }
  }
  private void displayNoNearable(){
    sendToBackend("NULL");
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: - ");
    ((TextView) findViewById(R.id.llevel)).setText("Level: - ");
    ((TextView) findViewById(R.id.lloc)).setText("Location: - " );
    ((TextView) findViewById(R.id.lloctxt)).setText("Description: - ");
    ((TextView) findViewById(R.id.lnearbylocs)).setText("Nearby Locations: - ");
    speaker.speak("You are now away from all registered locations", true);

  }

  private void display(String distance,String floor, String loc, String loctxt, String nearbyLocs){
    //((TextView) findViewById(R.id.macc)).setText("ID:" + mac);
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: " + distance);
    ((TextView) findViewById(R.id.llevel)).setText("Floor: " + floor);
    ((TextView) findViewById(R.id.lloc)).setText("Location: " + loc);
    ((TextView) findViewById(R.id.lloctxt)).setText("Description: "+loctxt);
    ((TextView) findViewById(R.id.lnearbylocs)).setText("Nearby Locations: \n" + nearbyLocs);
    if(destination==null) {
      speaker.speak(String.format("You are %s to %s.", distance, loc), true);
      speaker.pause(this.SHORT_DURATION);
      speaker.speak(loctxt, false);
      speaker.pause(this.SHORT_DURATION);
      speaker.speak(String.format("Nearby locations are %s", nearbyLocs), false);
    }
    if (destination!=null){
      if (lastLoc[1].equals(destination)){
        speaker.speak(String.format("You have reached %s",destination.getName()), true);
      }else{
        int movement = LocationMgr.movementAgainstDest(lastLoc[0],lastLoc[1],destination);
        if(movement ==1){
          //changeBgColor(Color.GREEN);
          speaker.speak(String.format("You are proceeding towards %s",destination.getName()), true);
        }else if(movement ==-1){
          //changeBgColor(Color.RED);
          speaker.speak(String.format("You are moving away from %s",destination.getName()), true);
        }
      }
    }
  }
  private void updateDistance(String distance, String loc){
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: " + distance);
    speaker.speak(String.format("You are %s to %s.", distance, loc), true);
  }
  private void sendToBackend(String locSlug) {
    Map<String, String> jsonMap = new HashMap<>();
    jsonMap.put("location_id", locSlug);
    jsonMap.put("user_id",USER_ID );
    jsonMap.put("timestamp", (System.currentTimeMillis() / 1000)+"");
    JSONObject json = new JSONObject(jsonMap);
    // Request a string response from the provided URL.
    Log.d("send to backend", json.toString());
    JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST, BACKEND_ENDPOINT,json,
            new Response.Listener<JSONObject>() {
              @Override
              public void onResponse(JSONObject response) {
                L.d(response.toString());
              }
            }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                L.d(error.toString());
              }
            });
    // Add the request to the RequestQueue.
    mRequestQueue.add(request);
  }
}
