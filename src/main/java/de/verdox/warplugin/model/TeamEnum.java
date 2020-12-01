package de.verdox.warplugin.model;

import org.bukkit.Color;

public enum TeamEnum {
    RED_TEAM(new Team("red", Color.RED)),
    BLUE_TEAM(new Team("blue", Color.BLUE)),
    NEUTRAL_TEAM(new Team("neutral", Color.WHITE)),
    ;
    private Team team;

    TeamEnum(Team team){
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public String getName(){
        return team.getName();
    }

    public Color getColor(){
        return team.getColor();
    }
}
