package com.estimote.examples.demos.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.List;
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
  private final String BACKEND_ENDPOINT = "http://symplcms.com/api/object/create";
  private final String USER_ID = "111";
  private RequestQueue mRequestQueue;
  private LocationMgr locMgr;
  //private Object currNearable;
  private String currNearableId;
  private String currNearableDistance;
  private Speaker speaker;
  private final int CHECK_CODE = 0x1;
  private final int LONG_DURATION = 5000;
  private final int SHORT_DURATION = 1200;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.location);
    L.enableDebugLogging(true);
    mRequestQueue = Volley.newRequestQueue(this);
    locMgr = new LocationMgr();
    //nearable = getIntent().getExtras().getParcelable(ListNearablesActivity.EXTRAS_NEARABLE);
    beaconManager = new BeaconManager(getApplicationContext());
//    checkTTS();
    speaker = new Speaker(this);
    //beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
    final Object[] availableNearables = locMgr.beaconMap.keySet().toArray();
    final String[] availableDistances = {"IMMEDIATE","10meter"};
    //for simulation
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            int rnd = new Random().nextInt(availableNearables.length);
            String pickedId = (String) availableNearables[rnd];
            int rnd2 = new Random().nextInt(availableDistances.length);
            processNearable(availableDistances[rnd2],pickedId);
          }
        });
      }
    }, new Date(),2000);

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
          //processNoNearable();
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
          //processNoNearable();
        }
      }
    });
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
//        beaconManager.startNearableDiscovery();
//        beaconManager.startEddystoneScanning();
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
      List<Location> nearbyLocs = locMgr.getNearbyLocations(loc);
      if (!id.equals(lastNearableId)) {// a different nearable
        Log.i("sending to backend", loc.getName());
        sendToBackend(loc.getName());
        display(distance, loc.getName(),
                loc.getText(), LocationMgr.listToString(nearbyLocs));
      }
      if (id.equals(lastNearableId) && !distance.equals(lastNearableDistance)) {
        updateDistance(distance, loc.getName());
      }
    }
  }
  private void processNoNearable(){
    sendToBackend("NULL");
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: - ");
    ((TextView) findViewById(R.id.lloc)).setText("Location: - " );
    ((TextView) findViewById(R.id.lloctxt)).setText("Description: - ");
    ((TextView) findViewById(R.id.lnearbylocs)).setText("Nearby Locations: - ");
    speaker.speak("You are now away from all registered locations", true);
  }

  private void display(String distance, String loc, String loctxt, String nearbyLocs){
    //((TextView) findViewById(R.id.macc)).setText("ID:" + mac);
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: " + distance);
    ((TextView) findViewById(R.id.lloc)).setText("Location: " + loc);
    ((TextView) findViewById(R.id.lloctxt)).setText("Description: "+loctxt);
    ((TextView) findViewById(R.id.lnearbylocs)).setText("Nearby Locations: \n" + nearbyLocs);

    speaker.speak(String.format("You are %s to %s.", distance, loc), true);
    speaker.pause(this.SHORT_DURATION);
    speaker.speak(loctxt, false);
    speaker.pause(this.SHORT_DURATION);
    speaker.speak(String.format("Nearby locations are %s", nearbyLocs), false);
  }
  private void updateDistance(String distance, String loc){
    ((TextView) findViewById(R.id.ldistance)).setText("Distance: " + distance);
    speaker.speak(String.format("You are %s to %s.", distance, loc), true);
  }
  private void sendToBackend(String locSlug) {
    Log.d("send to backend", locSlug);
    String jsonTemplateStr = "{\"country\": \"Singapore\", \"region\": \"\", \"city\": \"Singapore\",\n" +
            "\"userId\": 50, \"objectTypeId\": 341, \"appId\": 186, \"properties\": [\n" +
            "{ \"location_id\": \"1\" },{\"user_id\": \"2\" },\n" +
            "{ \"timestamp\": \"05-04-2016 12:00\" }" +
            "] }";
    try {
      JSONObject json = new JSONObject(jsonTemplateStr)
              .put("user_id",USER_ID)
              .put("location_id",locSlug)
              .put("timestamp", System.currentTimeMillis() / 1000);

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
    } catch (JSONException e) { e.printStackTrace(); }
  }
}
