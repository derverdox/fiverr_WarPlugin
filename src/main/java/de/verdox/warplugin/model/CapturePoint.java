package de.verdox.warplugin.model;

import com.boydti.fawe.bukkit.wrapper.AsyncBlock;
import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.sk89q.worldedit.world.block.BaseBlock;
import de.verdox.vcore.playersession.SessionManager;
import de.verdox.warplugin.Core;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public class CapturePoint {
    private Location location;
    private Team defaultTeam;
    private Team activeTeam;
    private City city;
    private CaptureType captureType;
    private int points = 100;
    private int lives;

    private int maxX;
    private int minX;
    private int maxY;
    private int minY;
    private int maxZ;
    private int minZ;
    private boolean isVisible;

    private BukkitTask pointChecker;

     CapturePoint(Location location, Team defaultTeam, City city, CaptureType captureType){
         if(location == null || defaultTeam == null || city == null || captureType == null)
             throw new NullPointerException("Arguments can't be null&7!");
        this.location = location;
        this.defaultTeam = defaultTeam;
        this.activeTeam = defaultTeam;
        this.city = city;
        this.captureType = captureType;

        this.maxX = this.location.add(captureType.getX(),0,0).getBlockX();
        this.minX = this.location.add(-captureType.getX(),0,0).getBlockX();

        this.maxY = this.location.add(0,captureType.getY(),0).getBlockX();
        this.minY = this.location.add(0,-captureType.getY(),0).getBlockX();

        this.maxZ = this.location.add(0,0,captureType.getZ()).getBlockX();
        this.minZ = this.location.add(0,0,-captureType.getZ()).getBlockX();
        this.lives = captureType.getTimeToCapture();

        setBlocks();

        this.pointChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.core,() -> {
            // CHECKER FÜR SPIELER

            //TODO: AsyncWorld get Nearby Entites asynchronous machen!

            List<WarPlayerData> warPlayerDataList = getPlayersInArea().stream().map(player -> {
                WarPlayerData warPlayerData = (WarPlayerData) SessionManager.getInstance().getSession(player).getData(WarPlayerData.identifier);
                return warPlayerData;
            }).collect(Collectors.toList());

            if(warPlayerDataList.isEmpty()){
                this.isVisible = false;
                return;
            }
            this.isVisible = true;

            city.getCityBossBar().setVisible(true);

            List<WarPlayerData> defenderTeam = warPlayerDataList.stream().filter(warPlayerData -> warPlayerData.getTeam().equals(this.activeTeam)).collect(Collectors.toList());
            List<WarPlayerData> attackerTeam = warPlayerDataList.stream().filter(warPlayerData -> !warPlayerData.getTeam().equals(this.activeTeam)).filter(warPlayerData -> !warPlayerData.getTeam().getName().equals("neutral")).collect(Collectors.toList());

            if(defenderTeam.size() > attackerTeam.size()){
                if(lives+1 >= captureType.timeToCapture)
                    lives = captureType.timeToCapture;
                else
                    lives++;
            }
            else if(defenderTeam.size() < attackerTeam.size())
                lives--;

            double progress = ((captureType.getTimeToCapture() - lives) / (double) captureType.getTimeToCapture());
            city.getCityBossBar().setProgress(progress);
        },0L,20L);
    }

    public int getTimeLeftToCapture(){
         return lives;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public City getCity() {
        return city;
    }

    public CaptureType getCaptureType() {
        return captureType;
    }

    public Location getLocation() {
        return location;
    }

    public List<Player> getPlayersInArea(){
        return getLocation().getWorld().getNearbyEntities(getLocation(),captureType.getX(),captureType.getY(),captureType.getZ())
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
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

    private void setBlocks(){
         setBlock(0,0,0,Material.GOLD_BLOCK,true);
         for(int x = -1 ; x<=1; x++){
             for(int z = -1 ; z<=1 ; z++){
                 setBlock(x,0,z,Material.GOLD_BLOCK,true);
             }
         }
         setBlock(0,1,0,Material.BEACON,true);
        System.out.println(activeTeam);
         if(activeTeam.getColor().equals(Color.RED)){
             setBlock(0,2,0,Material.RED_STAINED_GLASS,true);
             return;
         }
         else if(activeTeam.getColor().equals(Color.BLUE)){
             setBlock(0,2,0,Material.BLUE_STAINED_GLASS,true);
             return;
         }
         else if(activeTeam.getColor().equals(Color.WHITE)){
             setBlock(0,2,0,Material.WHITE_STAINED_GLASS,true);
             return;
         }
    }

    void removeBlocks(){
         setBlock(0,1,0,Material.AIR, false);
         setBlock(0,0,0,Material.AIR, false);
         for(int x = -1; x<=1 ; x++){
             for(int z = -1; z<=1 ; z++){
                 setBlock(x,0,z,Material.AIR, false);
             }
         }
         setBlock(0,2,0,Material.AIR, false);
         if(pointChecker != null) this.pointChecker.cancel();
    }

    private void setBlock(int relX, int relY, int relZ, Material material, boolean save){
         Block baseBlock = location.getBlock().getRelative(relX,relY,relZ);
         baseBlock.setType(material);
         CaptureBlock captureBlock = new CaptureBlock(baseBlock,this);

         if(save)
             GameManager.getInstance().registerBlock(captureBlock);

         else
             GameManager.getInstance().removeBlock(captureBlock);
    }

    void setActiveTeam(Team activeTeam) {
        this.activeTeam = activeTeam;
    }

    public Team getActiveTeam() {
        return activeTeam;
    }

    public Team getDefaultTeam() {
        return defaultTeam;
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

    public static boolean exist(String type){
         try{
             CaptureType.valueOf(type);
             return true;
         }
         catch (Exception e){
             return false;
         }
    }

}
