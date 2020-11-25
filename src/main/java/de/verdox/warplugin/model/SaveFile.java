package de.verdox.warplugin.model;

import de.verdox.vcore.files.Configuration;
import de.verdox.vcore.utils.Serializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

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
        for (String cityName : config.getConfigurationSection("Cities").getKeys(false)) {
            Team defaultTeam = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+",StandardTeam"));
            Team activeTeam = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+",ActiveTeam"));
            City city = new City(cityName,defaultTeam,activeTeam);

            for (String capturePointLocString : config.getConfigurationSection("Cities." + cityName + ".CapturePoints").getKeys(false)) {
                Location location = Serializer.deserializeLocation(capturePointLocString);
                Team activeTeamOfPoint = GameManager.getInstance().getTeam(config.getString("Cities."+cityName+".CapturePoints."+capturePointLocString+".ActiveTeam"));
                CapturePoint.CaptureType captureType = CapturePoint.CaptureType.valueOf(config.getString("Cities."+cityName+".CapturePoints."+capturePointLocString+".CaptureType"));
                city.registerCapturePoint(location,activeTeamOfPoint,captureType);
            }
        }
    }

    void updateCity(City city){
        config.set("Cities."+city.getName()+".StandardTeam",city.getStandardTeam().getName());
        config.set("Cities."+city.getName()+".ActiveTeam",city.getActiveTeam().getName());
        for (CapturePoint capturePoint : city.getCapturePoints()) {
            config.set("Cities."+city.getName()+".CapturePoints."+Serializer.serializeLocation(capturePoint.getLocation())+".ActiveTeam", capturePoint.getActiveTeam().getName());
            config.set("Cities."+city.getName()+".CapturePoints."+Serializer.serializeLocation(capturePoint.getLocation())+".CaptureType", capturePoint.getCaptureType());
        }
    }
}
