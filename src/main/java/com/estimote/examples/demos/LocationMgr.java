package com.estimote.examples.demos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 3/11/2016.
 */
public class LocationMgr {
    static int TYPE_ROOM = 0;
    static int TYPE_OBSTACLE = 1;
    static int TYPE_LIFT = 2;
    boolean dstIsSet = false;
    static String defaultFloor ="lvl 1";
    static double VERCINITY_THRESHOLD = 1.0;
    List<Location> locList;
    public Map<String, Integer> beaconMap = new HashMap<>();
    public LocationMgr(){
        locList = Arrays.asList(
                new Location(1, "Meeting room", "Meeting room is awesome",0,0),
                new Location(2, "Dining room", "Dining room is neat",1,0),
                new Location(3, "Toilet", "Toilet is clean",0,1)
        );
        beaconMap.put("f59f5117fd5f", 1);
        beaconMap.put("15b4f0861909be11", 2);
        //beaconMap.put("15b4f0861909be11", 3);
    }
    public static String listToString(List<Location> nearbyLoc){
        StringBuilder sb = new StringBuilder();
        for (Location loc: nearbyLoc){
            sb.append(loc);
        }
        return sb.toString();
    }
    public List<Location> getNearbyLocations(Location origin){
        List<Location> nearbyList = new ArrayList<>();
        for (Location loc: locList){
            if (euclideanDistance(loc, origin)<=VERCINITY_THRESHOLD) nearbyList.add(loc);
        }
        return nearbyList;
    }
    private double euclideanDistance(Location loc1, Location loc2){
        return Math.sqrt(Math.pow((loc1.getX() - loc2.getX()),2) + Math.pow((loc1.getY() - loc2.getY()),2));
    }
    public Location getBeaconLocation(String mac){
        if (beaconMap.containsKey(mac)){
            return getLoc(beaconMap.get(mac));
        }
        return null;
    }
    public Location getLoc(int locId){
        for (Location loc: locList){
            if (loc.getId() == locId) return loc;
        }
        return null;
    }
}
