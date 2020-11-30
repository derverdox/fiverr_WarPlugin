package de.verdox.warplugin.model;

import de.verdox.vcore.playersession.SessionManager;
import de.verdox.warplugin.Core;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

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
    private Set<CaptureBlock> protectedBlocks;
    GameManager(){
        this.cityCache = new HashMap<>();
        this.teams = new HashSet<>();
        this.protectedBlocks = new HashSet<>();
        Team redTeam = new Team("red", Color.RED);
        Team blueTeam = new Team("blue", Color.BLUE);
        Team neutral = new Team("neutral", Color.WHITE);
        teams.add(redTeam);
        teams.add(blueTeam);
        teams.add(neutral);
        this.saveFile = new SaveFile(Core.core,"saveFile.yml","\\");
    }

    void init(){
        this.saveFile.init();
        this.saveFile.load();
    }

    void registerBlock(CaptureBlock captureBlock){
        this.protectedBlocks.add(captureBlock);
    }

    void removeBlock(CaptureBlock captureBlock){
        this.protectedBlocks.remove(captureBlock);
    }

    // PUBLIC METHODS

    public void setPlayerTeam(Player player, Team playerTeam){
        WarPlayerData warPlayerData = (WarPlayerData) SessionManager.getInstance().getSession(player).getData(WarPlayerData.identifier);
        warPlayerData.setTeam(playerTeam);
        saveFile.updatePlayerData(warPlayerData);
    }

    public boolean exist(String name){
        return cityCache.containsKey(name);
    }

    public CapturePoint getCapturePoint(Location location){
        return protectedBlocks.stream().filter(captureBlock -> captureBlock.getLocation().equals(location)).map(captureBlock -> captureBlock.getCapturePoint()).findAny().orElse(null);
    }

    public CapturePoint createCapturePoint(City city, Team activeTeam, Location location, CapturePoint.CaptureType captureType){
        CapturePoint capturePoint = city.registerCapturePoint(location,activeTeam,captureType);
        saveFile.updateCity(city);
        return capturePoint;
    }

    public void removeCapturePoint(CapturePoint capturePoint){
        capturePoint.getCity().deleteCapturePoint(capturePoint);
        capturePoint.removeBlocks();
        saveFile.updateCity(capturePoint.getCity());
    }

    public City createCity(String name, Team standardTeam, Team activeTeam){
        if(cityCache.containsKey(name))
            return cityCache.get(name);
        City city = new City(name,standardTeam,activeTeam);
        cityCache.put(name,city);
        saveFile.updateCity(city);
        return city;
    }

    public void removeCity(City city){
        this.cityCache.remove(city.getName());
        city.getCapturePoints().forEach(capturePoint -> removeCapturePoint(capturePoint));
        this.saveFile.removeCity(city);
    }

    City registerCity(String name, Team standardTeam, Team activeTeam){
        if(cityCache.containsKey(name))
            return cityCache.get(name);
        City city = new City(name,standardTeam,activeTeam);
        cityCache.put(name,city);
        return city;
    }

    public Team getTeam(String name){
        return this.teams.stream().filter(team -> team.getName().equals(name)).findAny().orElse(null);
    }

    public City getCity(String city){
        return cityCache.get(city);
    }

    public List<City> getCities(){
        return cityCache.values().stream().collect(Collectors.toList());
    }

    public List<Team> getTeams(){
        return teams.stream().collect(Collectors.toList());
    }

    public SaveFile getSaveFile() {
        return saveFile;
    }
}
