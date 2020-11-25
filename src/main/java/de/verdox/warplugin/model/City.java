package de.verdox.warplugin.model;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class City {

    Map<Location, CapturePoint> cache;
    private Team standardTeam;
    private Team activeTeam;
    private String name;

    City(String name, Team standardTeam){
        this.name = name;
        this.standardTeam = standardTeam;
        this.activeTeam = standardTeam;
        this.cache = new HashMap<>();
    }

    City(String name, Team standardTeam, Team activeTeam){
        this.name = name;
        this.standardTeam = standardTeam;
        this.activeTeam = activeTeam;
        this.cache = new HashMap<>();
    }

    public CapturePoint createCapturePoint(Location location, CapturePoint.CaptureType captureType){
        if(cache.containsKey(location))
            return this.cache.get(location);
        CapturePoint capturePoint = new CapturePoint(location,standardTeam,this,captureType);
        this.cache.put(location,capturePoint);

        return capturePoint;
    }

    CapturePoint registerCapturePoint(Location location, Team activeTeam, CapturePoint.CaptureType captureType){
        if(cache.containsKey(location))
            return this.cache.get(location);
        CapturePoint capturePoint = new CapturePoint(location,standardTeam,this,captureType);
        capturePoint.setActiveTeam(activeTeam);
        this.cache.put(location,capturePoint);

        return capturePoint;
    }

    public boolean deleteCapturePoint(CapturePoint capturePoint){
        if(!cache.containsKey(capturePoint.getLocation()))
            return false;
        cache.remove(capturePoint.getLocation());
        return true;
    }

    public String getName() {
        return name;
    }

    public Team getStandardTeam() {
        return standardTeam;
    }

    public Team getActiveTeam() {
        return activeTeam;
    }

    public List<CapturePoint> getCapturePoints(){
        return cache.values().stream().collect(Collectors.toList());
    }
}
