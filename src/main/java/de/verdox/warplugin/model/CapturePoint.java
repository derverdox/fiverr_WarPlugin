package de.verdox.warplugin.model;

import com.boydti.fawe.bukkit.wrapper.AsyncBlock;
import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.sk89q.worldedit.world.block.BaseBlock;
import de.verdox.vcore.VCore;
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
import java.util.Map;
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
    private BossBar pointBossbar;


    //TODO: Beim erobern Team wechseln -> 50% der Leben zurück geben, damit man es trotzdem noch aufladen muss
    //TODO: Bei neutralen Gebieten bei 50% starten -> Teams streiten sich drum -> Angreifer muss Leben auf 0 bringen, Verteidiger auf 100

     CapturePoint(Location location, Team defaultTeam, City city, CaptureType captureType){
         if(location == null || defaultTeam == null || city == null || captureType == null)
             throw new NullPointerException("Arguments can't be null&7!");
        this.location = location;
        this.defaultTeam = defaultTeam;
        this.activeTeam = defaultTeam;
        this.city = city;
        this.captureType = captureType;

        BarStyle barStyle;
        switch (captureType){
            case SMALL: barStyle = BarStyle.SEGMENTED_6;break;
            case MEDIUM: barStyle = BarStyle.SEGMENTED_10;break;
            case LARGE: barStyle = BarStyle.SEGMENTED_12;break;
            default:barStyle = BarStyle.SEGMENTED_20;break;
        }

        BarColor barColor = evaluateBarColor();

        this.pointBossbar = Bukkit.createBossBar(ChatColor.translateAlternateColorCodes('&',"&8[&c"+captureType.name()+"&8] &a"+city.getName()),barColor, barStyle);

        this.maxX = this.location.add(captureType.getX(),0,0).getBlockX();
        this.minX = this.location.add(-captureType.getX(),0,0).getBlockX();

        this.maxY = this.location.add(0,captureType.getY(),0).getBlockX();
        this.minY = this.location.add(0,-captureType.getY(),0).getBlockX();

        this.maxZ = this.location.add(0,0,captureType.getZ()).getBlockX();
        this.minZ = this.location.add(0,0,-captureType.getZ()).getBlockX();

        if(this.activeTeam.getName().equals("neutral"))
            this.lives = captureType.getTimeToCapture()/2;
        else
            this.lives = captureType.getTimeToCapture();

        setBlocks();

        //TODO: BOSSBAR FÜR SPIELER INNERHALB! -> SEHEN FORTSCHRITT

        this.pointChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.core,() -> {
            this.pointBossbar.setColor(evaluateBarColor());

            Team redTeam = GameManager.getInstance().getTeam(TeamEnum.RED_TEAM);
            Team blueTeam = GameManager.getInstance().getTeam(TeamEnum.BLUE_TEAM);
            Team neutralTeam = GameManager.getInstance().getTeam(TeamEnum.NEUTRAL_TEAM);

            // CHECKER FÜR SPIELER

            // Setzen der Attacker und Defender

            Team attackerTeam;
            Team defenderTeam;
            if(!this.activeTeam.equals(neutralTeam)){
                defenderTeam = activeTeam;
                if(activeTeam.equals(redTeam))
                    attackerTeam = blueTeam;
                else
                    attackerTeam = redTeam;
            }
            else {
                defenderTeam = blueTeam;
                attackerTeam = redTeam;
            }

            List<Player> playerList = getPlayersInArea().stream().collect(Collectors.toList());

            Map<Team,List<WarPlayerData>> playersNearby = getPlayersInArea().stream().map(player -> {
                WarPlayerData warPlayerData = (WarPlayerData) SessionManager.getInstance().getSession(player).getData(WarPlayerData.identifier);
                return warPlayerData;
            }).collect(Collectors.groupingBy(WarPlayerData::getTeam));

            int attackersTeamSize = 0; if(playersNearby.get(attackerTeam) != null) attackersTeamSize = playersNearby.get(attackerTeam).size();
            int defendersTeamSize = 0; if(playersNearby.get(defenderTeam) != null) defendersTeamSize = playersNearby.get(defenderTeam).size();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if(playerList.isEmpty()){
                    this.isVisible = false;
                    pointBossbar.removeAll();
                    return;
                }
                if(!playerList.contains(player))
                    pointBossbar.removePlayer(player);
                else
                    pointBossbar.addPlayer(player);
            }

            this.isVisible = true;
            pointBossbar.setVisible(true);

            if(defendersTeamSize > attackersTeamSize){
                if(lives+1 >= captureType.timeToCapture) {
                    city.getCityBossBar().setTitle(ChatColor.translateAlternateColorCodes('&',"&8[&a"+city.getName()+"&8]"));
                    lives = captureType.timeToCapture;
                }
                else {
                    city.getCityBossBar().setTitle(ChatColor.translateAlternateColorCodes('&',"&7>>>> &8[&a"+city.getName()+"&8]"));
                    lives++;
                }

            }
            else if(defendersTeamSize < attackersTeamSize){
                if(lives-1 <= 0) {
                    city.getCityBossBar().setTitle(ChatColor.translateAlternateColorCodes('&',"&8[&a"+city.getName()+"&8]"));
                    lives = 0;
                }
                else {
                    lives--;
                    city.getCityBossBar().setTitle(ChatColor.translateAlternateColorCodes('&',"&7<<<< &8[&a"+city.getName()+"&8]"));
                }
            }
            else
                city.getCityBossBar().setTitle(ChatColor.translateAlternateColorCodes('&',"&7>><< &8[&a"+city.getName()+"&8]"));


            double progress = (lives / (double) captureType.getTimeToCapture());
            if(progress <= 0)
                progress = 0;
            if(progress >= 1)
                progress = 1;
            pointBossbar.setProgress(progress);

            // Attacker Team Wins
            if(progress == 0){
                // It has been captured
                capture(attackerTeam);
            }
            // Defender Team wins
            else if(progress == 100){
                // Active Team war neutral -> Blau nimmt diese Basis ein
                if(!defenderTeam.equals(activeTeam))
                    capture(defenderTeam);
            }
        },0L,20L);
    }

    private void capture(Team team){
         this.activeTeam = team;
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aA capturepoint of the city &b"+city.getName()+" &ahas been captured by Team&7: "+team.getNameWithColor()));
        });
        Bukkit.getScheduler().runTask(Core.core,() -> {
            setBlocks();
        });
        GameManager.getInstance().getSaveFile().updateCity(city);
    }

    private BarColor evaluateBarColor(){
        if(activeTeam.getColor().equals(Color.RED))
            return BarColor.RED;
        else if(activeTeam.getColor().equals(Color.BLUE))
            return BarColor.BLUE;
        else
            return BarColor.WHITE;
    }

    public BossBar getPointBossbar() {
        return pointBossbar;
    }

    void stopChecker(){
         this.pointChecker.cancel();
    }

    public int getTimeLeftToCapture(){
         return lives;
    }

    public int getMaxCaptureTime(){
         return captureType.getTimeToCapture();
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
        AsyncWorld asyncWorld = AsyncWorld.create(new WorldCreator(this.location.getWorld().getName()));
        return asyncWorld.getNearbyEntities(getLocation(),captureType.getX(),captureType.getY(),captureType.getZ()).stream()
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
