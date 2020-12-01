package de.verdox.warplugin.model;

import de.verdox.vcore.VCore;
import de.verdox.vcore.playersession.SessionManager;
import de.verdox.warplugin.Core;
import de.verdox.warplugin.model.items.Diffuser;
import de.verdox.warplugin.model.items.LandMine;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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
    private Set<LandmineBlock> landMines;
    private Diffuser diffuser;
    private LandMine landMine;
    private BukkitTask landMineMarker;
    GameManager(){
        this.cityCache = new HashMap<>();
        this.teams = new HashSet<>();
        this.protectedBlocks = new HashSet<>();

        for (TeamEnum value : TeamEnum.values()) {
            teams.add(value.getTeam());
        }
        this.saveFile = new SaveFile(Core.core,"saveFile.yml","\\");
        this.diffuser = new Diffuser(saveFile);
        this.landMine = new LandMine(saveFile);
        this.landMines = new HashSet<>();
        landMineMarker = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.core,() -> {

            Map<Team,List<WarPlayerData>> users = Bukkit.getOnlinePlayers()
                    .stream()
                    .map(player -> (WarPlayerData) SessionManager.getInstance().getSession(player).getData(WarPlayerData.identifier))
                    .collect(Collectors.groupingBy(WarPlayerData::getTeam));

            for (LandmineBlock mine : landMines) {

                Team mineTeam = mine.getTeam();
                if(users.get(mineTeam) == null)
                    continue;
                for (WarPlayerData warPlayerData : users.get(mineTeam)) {
                    Bukkit.getScheduler().runTask(Core.core,() -> {
                        // Spawn Mine Particle
                        warPlayerData.getPlayer().spawnParticle(Particle.FLAME,mine.getLocation().getBlock().getRelative(0,2,0).getLocation(),10,0,2,0,0);
                    });
                }
            }
        },0L,20L);
    }

    void init(){
        this.saveFile.init();
        this.saveFile.load();
    }

    public void giveMine(Player player){
        player.getInventory().addItem(landMine.getItemStack());
    }

    public void giveDiffuser(Player player){
        player.getInventory().addItem(diffuser.getItemStack());
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

    void registerLandMineBlock(LandmineBlock landmineBlock){
        this.landMines.add(landmineBlock);
    }

    public void createLandMine(Player setter, Location location){
        WarPlayerData warPlayerData = (WarPlayerData) SessionManager.getInstance().getSession(setter).getData(WarPlayerData.identifier);

        LandmineBlock landmineBlock = new LandmineBlock(location,warPlayerData.getTeam());
        this.landMines.add(landmineBlock);
        this.saveFile.updateLandMine(landmineBlock);
    }

    public LandmineBlock getLandMineBlock(Location location){
        return landMines.stream().filter(landmineBlock -> {
            Location testLoc = location.getBlock().getRelative(0,-1,0).getLocation();
            if(testLoc.equals(landmineBlock.getLocation()))
                return true;
            return false;
        }).findAny().orElse(null);
    }

    public void explodeLandMine(LandmineBlock landmineBlock){
        landmineBlock.getLocation().getWorld().createExplosion(landmineBlock.getLocation().getBlock().getRelative(0,1,0).getLocation(),4,false,false);
        removeLandMineBlock(landmineBlock);
    }

    public void removeLandMineBlock(LandmineBlock landmineBlock){
        this.landMines.remove(landmineBlock);
        this.saveFile.removeLandMine(landmineBlock);
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
        capturePoint.stopChecker();
        capturePoint.getPointBossbar().removeAll();
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
        city.stopChecker();
        city.getCityBossBar().removeAll();
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
        TeamEnum teamEnum = Arrays.stream(TeamEnum.values()).filter(teamEnum1 -> teamEnum1.getName().equals(name)).findAny().orElse(null);
        if(teamEnum == null)
            return null;
        return teamEnum.getTeam();
    }

    public Team getTeam(TeamEnum teamEnum){
        return teamEnum.getTeam();
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
