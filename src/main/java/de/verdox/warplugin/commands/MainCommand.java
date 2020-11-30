package de.verdox.warplugin.commands;

import de.verdox.warplugin.model.CapturePoint;
import de.verdox.warplugin.model.City;
import de.verdox.warplugin.model.GameManager;
import de.verdox.warplugin.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cThis command is only for players&7!"));
            return false;
        }
        Player player = (Player) sender;
        /**
         * /war setTeam player team
         * /war createPoint red/blue/neutral city type
         * /war createCity red/blue/neutral cityName
         * /war deletePoint
         * /war deleteCity city
         */
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("deletePoint")){
                RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
                if(rayTraceResult == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cPlease look at a block in a 5m range&7!"));
                    return false;
                }
                CapturePoint capturePoint = GameManager.getInstance().getCapturePoint(rayTraceResult.getHitBlock().getLocation());
                if(capturePoint == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cPlease look at a CapturePoint in a 5m range&7!"));
                    return false;
                }
                GameManager.getInstance().removeCapturePoint(capturePoint);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aSuccessfully deleted point&7!"));
                return true;
            }
        }
        else if(args.length == 2){
            if(args[0].equalsIgnoreCase("deleteCity")){
                City city = GameManager.instance.getCity(args[1]);
                if(city == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cThis city does not exist&7!"));
                    return false;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aSuccessfully deleted City&7!"));
                GameManager.getInstance().removeCity(city);
                return true;
            }
        }
        else if(args.length == 3){
            if(args[0].equalsIgnoreCase("setTeam")){

                Player target = Bukkit.getPlayer(args[1]);
                if(target == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cPlayer not found&7!"));
                    return false;
                }
                Team team = GameManager.getInstance().getTeam(args[2]);
                if(team == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cTeam not found&7!"));
                    return false;
                }
                GameManager.getInstance().setPlayerTeam(player,team);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aSuccessfully set team of player&7!"));
                return true;
            }
            else if(args[0].equalsIgnoreCase("createCity")){
                String teamName = args[1];
                Team team = GameManager.getInstance().getTeam(teamName);
                if(team == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cTeam does not exist&7!"));
                    return false;
                }
                String cityName = args[2];
                if(GameManager.getInstance().exist(cityName)){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cCity does already exist&7!"));
                    return false;
                }
                GameManager.getInstance().createCity(cityName,team,team);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aCity &e"+cityName+"&a successfully created&7!"));
                return true;
            }
        }
        else if(args.length == 4){
            if(args[0].equalsIgnoreCase("createPoint")){
                String teamName = args[1];
                Team team = GameManager.getInstance().getTeam(teamName);
                if(team == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cTeam does not exist&7!"));
                    return false;
                }
                String cityName = args[2];
                if(!GameManager.getInstance().exist(cityName)){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cCity does not exist&7!"));
                    return false;
                }
                City city = GameManager.getInstance().getCity(cityName);
                String type = args[3];
                if(!CapturePoint.exist(type)){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cCapturePoint type does not exist&7!"));
                    return false;
                }
                CapturePoint.CaptureType captureType = CapturePoint.CaptureType.valueOf(type);

                RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
                if(rayTraceResult == null){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cPlease look at a block in a 5m range&7!"));
                    return false;
                }
                GameManager.getInstance().createCapturePoint(city,team,rayTraceResult.getHitBlock().getLocation(),captureType);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aPoint successfully created for &e"+cityName+"&7!"));
                return true;
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cNot enough arguments&7!"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> suggest = new ArrayList<>();

        if(args.length <= 1){
            suggest.add("deletePoint");
            suggest.add("deleteCity");
            suggest.add("setTeam");
            suggest.add("createPoint");
            suggest.add("createCity");
        }
        else if(args.length <= 2){
            if(args[0].equalsIgnoreCase("deleteCity")){
                GameManager.getInstance().getCities().forEach(city -> suggest.add(city.getName()));
            }
            else if(args[0].equalsIgnoreCase("setTeam")){
                Bukkit.getOnlinePlayers().forEach(player -> suggest.add(player.getName()));
            }
            else if(args[0].equalsIgnoreCase("createPoint")){
                GameManager.getInstance().getTeams().forEach(team -> suggest.add(team.getName()));
            }
            else if(args[0].equalsIgnoreCase("createCity")){
                GameManager.getInstance().getTeams().forEach(team -> suggest.add(team.getName()));
            }
            else {
                return suggest;
            }
        }
        else if(args.length <= 3){
            if(args[0].equalsIgnoreCase("setTeam")){
                GameManager.getInstance().getTeams().forEach(team -> suggest.add(team.getName()));
            }
            else if(args[0].equalsIgnoreCase("createPoint")){
                GameManager.getInstance().getCities().forEach(city -> suggest.add(city.getName()));
            }
            else if(args[0].equalsIgnoreCase("createCity")){
                suggest.add("newCity");
            }
            else {
                return suggest;
            }
        }
        else if(args.length <= 4){
            if(args[0].equalsIgnoreCase("createPoint")){
                for (CapturePoint.CaptureType value : CapturePoint.CaptureType.values()) {
                    suggest.add(value.name());
                }
            }
        }


        return suggest;
    }
}
