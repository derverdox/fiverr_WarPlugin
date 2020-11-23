package de.verdox.warplugin.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class CapturePoint {
    private Location location;
    private Team team;
    private City city;
    private CaptureType captureType;

    private int maxX;
    private int minX;
    private int maxY;
    private int minY;
    private int maxZ;
    private int minZ;

    public CapturePoint(Location location, Team team, City city, CaptureType captureType){
        this.location = location;
        this.team = team;
        this.city = city;
        this.captureType = captureType;

        this.maxX = this.location.add(captureType.getX(),0,0).getBlockX();
        this.minX = this.location.add(-captureType.getX(),0,0).getBlockX();

        this.maxY = this.location.add(0,captureType.getY(),0).getBlockX();
        this.minY = this.location.add(0,-captureType.getY(),0).getBlockX();

        this.maxZ = this.location.add(0,0,captureType.getZ()).getBlockX();
        this.minZ = this.location.add(0,0,-captureType.getZ()).getBlockX();
    }

    public boolean isInsideCaptureArea(Player player){
        Location location = player.getLocation();
        if(location.getBlockX() <= maxX
                && location.getBlockX() >= minX
                && location.getBlockY() <= maxY
                && location.getBlockY() >= minY
                && location.getBlockZ() <= maxZ
                && location.getBlockZ() >= minZ)
            return true;
        return false;
    }

    public enum CaptureType{
        SMALL(30, 5, 5, 5),
        MEDIUM(85,15,5,15),
        LARGE(120,25,5,25),
        ;
        private int timeToCapture;
        private int x;
        private int y;
        private int z;

        CaptureType(int timeToCapture, int x, int y, int z){
            this.timeToCapture = timeToCapture;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public int getTimeToCapture() {
            return timeToCapture;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }
}
