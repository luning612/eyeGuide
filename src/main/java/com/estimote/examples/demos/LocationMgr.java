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
    public static final int NEARER = 1;
    public static final int FARTHER = 2;
    public static final int NEITHER = 0;
    public static final int REACHED = -1;

    List<Location> locList;
    public Map<String, Integer> beaconMap = new HashMap<>();
    public LocationMgr(){
        locList = Arrays.asList(
                new Location(1, "Meeting room", "Meeting room is awesome",0,0),
                new Location(2, "Dining room", "Dining room is neat",1,0),
                new Location(3, "Toilet", "Toilet is clean",0,2),
                new Location(4, "Sitting area", "There are VH accessible benches.",1,2)
        );
        beaconMap.put("f59f5117fd5f", 1);
        beaconMap.put("15b4f0861909be11", 2);
        beaconMap.put("[F5:9F:51:17:FD:5F]", 3);//eddystone
        beaconMap.put("[E9:65:4A:DB:91:32]", 4);//eddystone
    }
    public static String listToString(List<Location> nearbyLoc){
        StringBuilder sb = new StringBuilder();
        for (Location loc: nearbyLoc){
            sb.append(loc.getName()+"\n");
        }
        return sb.toString();
    }
    public static int movementAgainstDest(Location loc1, Location loc2, Location dest){
        double dist1d = euclideanDistance(loc1, dest);
        double dist2d = euclideanDistance(loc2, dest);
        if (dist1d< dist2d) return FARTHER;
        else if (dist1d< dist2d) return NEARER;
        else return NEITHER;
    }
    public List<Location> getNearbyLocations(Location origin){
        List<Location> nearbyList = new ArrayList<>();
        for (Location loc: locList){
            if (loc.getId()!= origin.getId() && euclideanDistance(loc, origin)<=VERCINITY_THRESHOLD) {
                nearbyList.add(loc);
            }
        }
        return nearbyList;
    }
    private static double euclideanDistance(Location loc1, Location loc2){
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
