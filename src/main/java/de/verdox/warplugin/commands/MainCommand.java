package de.verdox.warplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

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
         * /war createPoint red/blue/neutral city
         * /war createCity red/blue/neutral cityName
         * /war deletePoint
         * /war deleteCity city
         */
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("deletePoint")){

            }
        }
        else if(args.length == 2){
            if(args[0].equalsIgnoreCase("deleteCity")){

            }
        }
        else if(args.length == 3){
            if(args[0].equalsIgnoreCase("setTeam")){

            }
            else if(args[0].equalsIgnoreCase("createPoint")){

            }
            else if(args[0].equalsIgnoreCase("createCity")){

            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}
