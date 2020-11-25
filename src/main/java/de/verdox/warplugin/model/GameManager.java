package de.verdox.warplugin.model;

import de.verdox.warplugin.Core;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameManager {
    public static GameManager instance;

    public static GameManager getInstance() {
        if(instance == null){
            instance = new GameManager();
            instance.init();
        }

        return instance;
    }

    private Map<String, City> cityCache;
    private SaveFile saveFile;
    private Set<Team> teams;
    GameManager(){
        this.cityCache = new HashMap<>();
        this.teams = new HashSet<>();
        Team redTeam = new Team("red", Color.RED);
        Team blueTeam = new Team("blue", Color.BLUE);
        Team neutral = new Team("neutral", Color.WHITE);
        teams.add(redTeam);
        teams.add(blueTeam);
        teams.add(neutral);
        this.saveFile = new SaveFile(Core.core,"saveFile.yml","\\");
    }

    void init(){
        this.saveFile.load();
    }

    public City createCity(String name, Team standardTeam){
        if(cityCache.containsKey(name))
            return cityCache.get(name);
        City city = new City(name,standardTeam);
        cityCache.put(name,city);
        saveFile.updateCity(city);
        return city;
    }

    public CapturePoint createCapturePoint(City city, Location location, CapturePoint.CaptureType captureType){
        CapturePoint capturePoint = city.createCapturePoint(location,captureType);
        saveFile.updateCity(city);
        return capturePoint;
    }

    public Team getTeam(String name){
        return this.teams.stream().filter(team -> team.getName().equals(name)).findAny().orElse(null);
    }

}
