package de.verdox.warplugin.model;

import de.verdox.vcore.VCore;
import de.verdox.warplugin.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
    private double maxLives = 0;
    private double currentLives = 0;

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
        this.cityBossBar = Bukkit.createBossBar(ChatColor.translateAlternateColorCodes('&',"&8[&a"+name+"&8]"), evaluateBarColor(), BarStyle.SEGMENTED_20);
        startChecker();
    }

    //TODO: HAUPTBALKEN FÜR GESAMTE CITY

    private void startChecker(){
        this.bossBarVisibleChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.core,() -> {

            this.cityBossBar.setColor(evaluateBarColor());

            Team redTeam = GameManager.getInstance().getTeam(TeamEnum.RED_TEAM);
            Team blueTeam = GameManager.getInstance().getTeam(TeamEnum.BLUE_TEAM);
            Team neutralTeam = GameManager.getInstance().getTeam(TeamEnum.NEUTRAL_TEAM);

            CapturePoint visiblePoint = getCapturePoints().stream().filter(capturePoint -> capturePoint.isVisible()).findAny().orElse(null);

            if(visiblePoint != null){
                cityBossBar.setVisible(true);
                Bukkit.getOnlinePlayers().forEach(player -> cityBossBar.addPlayer(player));
            }
            else {
                cityBossBar.removeAll();
                cityBossBar.setVisible(false);
            }

            this.currentLives = 0;
            for (CapturePoint capturePoint : getCapturePoints()) {
                if(capturePoint.getActiveTeam().equals(activeTeam))
                    currentLives+=capturePoint.getTimeLeftToCapture();
            }
            this.maxLives = 0;
            for (CapturePoint capturePoint : getCapturePoints()) { maxLives+=capturePoint.getMaxCaptureTime(); }

            double progress = 1;
            if(maxLives != 0)
                progress = (currentLives / maxLives);

            //VCore.getInstance().consoleMessage("ActualLives: "+actualLives+"  |  MaxLives: "+maxLives+"  |  Progress: "+progress);
            if(progress <= 0)
                progress = 0;
            if(progress >= 1)
                progress = 1;
            cityBossBar.setProgress(progress);

            // Attacker Team wins
            if(progress == 0) {
                if (this.activeTeam.equals(redTeam)) {
                    // Blau nimmt das hier ein
                    captureCity(blueTeam);
                } else {
                    // Rot nimmt das ein
                    captureCity(redTeam);
                }
            }
            // Defender Team Wins
            else if(progress == 100){
                // Blau nimmt diese Stadt ein
                if(activeTeam.equals(neutralTeam)){
                    captureCity(blueTeam);
                }
            }

        },0L,20L);
    }

    private void captureCity(Team team){
        this.activeTeam = team;
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aThe city &b"+name+" &ahas been captured by Team&7: "+team.getNameWithColor()));
        });
        GameManager.getInstance().getSaveFile().updateCity(this);
    }

    private BarColor evaluateBarColor(){
        if(activeTeam.getColor().equals(Color.RED))
            return BarColor.RED;
        else if(activeTeam.getColor().equals(Color.BLUE))
            return BarColor.BLUE;
        else
            return BarColor.WHITE;
    }

    public BossBar getCityBossBar() {
        return cityBossBar;
    }

    void stopChecker(){
        this.bossBarVisibleChecker.cancel();
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
