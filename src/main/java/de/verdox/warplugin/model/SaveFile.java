package de.verdox.warplugin.model;

import de.verdox.vcore.files.Configuration;
import de.verdox.vcore.utils.Serializer;
import de.verdox.warplugin.Core;
import de.verdox.warplugin.model.items.LandMine;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class SaveFile extends Configuration {

    public SaveFile(Plugin plugin, String fileName, String pluginDirectory) {
        super(plugin, fileName, pluginDirectory);
    }

    @Override
    public void setupConfig() {
        config.options().copyDefaults(true);
        save();
    }

    void load(){
        if(config.getConfigurationSection("Cities") == null)
            return;
        for (String cityName : config.getConfigurationSection("Cities").getKeys(false)) {
            Team defaultTeam = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+".StandardTeam"));
            Team activeTeam = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+".ActiveTeam"));
            City city = GameManager.getInstance().registerCity(cityName,defaultTeam,activeTeam);
            Core.vcore.consoleMessage("City "+cityName+" loaded");
            if(config.getConfigurationSection("Cities." + cityName + ".CapturePoints") != null){
                for (String capturePointLocString : config.getConfigurationSection("Cities." + cityName + ".CapturePoints").getKeys(false)) {
                    Location location = Serializer.deserializeLocation(capturePointLocString);
                    Team activeTeamOfPoint = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+".CapturePoints."+capturePointLocString+".ActiveTeam"));
                    CapturePoint.CaptureType captureType = CapturePoint.CaptureType.valueOf(config.getString("Cities."+cityName+".CapturePoints."+capturePointLocString+".CaptureType"));
                    city.registerCapturePoint(location,activeTeamOfPoint,captureType);
                }
            }
            if(config.getConfigurationSection("LandMines") != null){
                for (String landMineLoc : config.getConfigurationSection("LandMines").getKeys(false)) {
                    Location location = Serializer.deserializeLocation(landMineLoc);
                    Team team = GameManager.getInstance().getTeam(config.getString("LandMines."+landMineLoc));
                    LandmineBlock landmineBlock = new LandmineBlock(location,team);
                    GameManager.getInstance().registerLandMineBlock(landmineBlock);
                }
            }
        }
    }

    void updateLandMine(LandmineBlock landmineBlock){
        config.set("LandMines."+Serializer.serializeLocation(landmineBlock.getLocation()),landmineBlock.getTeam().getName());
        save();
    }

    void removeLandMine(LandmineBlock landmineBlock){
        config.set("LandMines."+Serializer.serializeLocation(landmineBlock.getLocation()),null);
        save();
    }

    void removeCity(City city){
        config.set("Cities."+city.getName(),null);
        save();
    }

    void updateCity(City city){
        config.set("Cities."+city.getName()+".StandardTeam",city.getStandardTeam().getName());
        config.set("Cities."+city.getName()+".ActiveTeam",city.getActiveTeam().getName());
        for (CapturePoint capturePoint : city.getCapturePoints()) {
            config.set("Cities."+city.getName()+".CapturePoints."+Serializer.serializeLocation(capturePoint.getLocation())+".ActiveTeam", capturePoint.getActiveTeam().getName());
            config.set("Cities."+city.getName()+".CapturePoints."+Serializer.serializeLocation(capturePoint.getLocation())+".CaptureType", capturePoint.getCaptureType().name());
        }
        save();
    }

    public void updatePlayerData(WarPlayerData warPlayerData){
        config.set("UserData."+warPlayerData.getPlayer().getUniqueId(),warPlayerData.getTeam().getName());
        save();
    }

    public Team getPlayerTeam(UUID uuid){
        String teamName = config.getString("UserData."+uuid);
        if(teamName == null)
            return GameManager.getInstance().getTeam("neutral");
        return GameManager.getInstance().getTeam(teamName);
    }

}
