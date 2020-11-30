package de.verdox.warplugin.model;

import org.bukkit.Color;

public class Team {

    private String name;
    private Color color;

    public Team(String name, Color color){
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", color=" + color +
                '}';
    }
}
