package de.verdox.warplugin.model;

import de.verdox.warplugin.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class City {

    Map<Location, CapturePoint> cache;
    private Team standardTeam;
    private Team activeTeam;
    private String name;
    BossBar cityBossBar;
    private BukkitTask bossBarVisibleChecker;

    City(String name, Team standardTeam){
        if(name == null || standardTeam == null || activeTeam == null)
            throw new NullPointerException("Arguments can't be null!");
        this.name = name;
        this.standardTeam = standardTeam;
        this.activeTeam = standardTeam;
        this.cache = new HashMap<>();
        this.cityBossBar = Bukkit.createBossBar("Test", BarColor.RED, BarStyle.SEGMENTED_20);
        startChecker();
    }

    City(String name, Team standardTeam, Team activeTeam){
        if(name == null || standardTeam == null || activeTeam == null)
            throw new NullPointerException("Arguments can't be null!");
        this.name = name;
        this.standardTeam = standardTeam;
        this.activeTeam = activeTeam;
        this.cache = new HashMap<>();
        this.cityBossBar = Bukkit.createBossBar("Test", BarColor.RED, BarStyle.SEGMENTED_20);
        startChecker();
    }

    private void startChecker(){
        this.bossBarVisibleChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.core,() -> {
            for (CapturePoint capturePoint : getCapturePoints()) {
                if(capturePoint.isVisible()){
                    cityBossBar.setVisible(true);
                    return;
                }
            }
            cityBossBar.setVisible(false);
        },0L,20L);
    }

    public BossBar getCityBossBar() {
        return cityBossBar;
    }

    @Deprecated
    public CapturePoint createCapturePoint(Location location, CapturePoint.CaptureType captureType){
        if(cache.containsKey(location))
            return this.cache.get(location);
        CapturePoint capturePoint = new CapturePoint(location,standardTeam,this,captureType);
        this.cache.put(location,capturePoint);

        return capturePoint;
    }

    public int getCityLives(){
        return getCapturePoints().stream().map(capturePoint -> capturePoint.getTimeLeftToCapture()).reduce((integer, integer2) -> integer+integer2).get();
    }

    public CapturePoint registerCapturePoint(Location location, Team activeTeam, CapturePoint.CaptureType captureType){
        if(cache.containsKey(location))
            return this.cache.get(location);
        CapturePoint capturePoint = new CapturePoint(location,activeTeam,this,captureType);
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
