package de.verdox.warplugin.model;

import de.verdox.vcore.playersession.PlayerData;
import org.bukkit.entity.Player;

public class WarPlayerData extends PlayerData {

    public static String identifier = "War_Data";

    public Team team;

    public WarPlayerData(Player player, Team team) {
        super(player);
        this.team = team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public void onSessionRemove() {

    }
}
