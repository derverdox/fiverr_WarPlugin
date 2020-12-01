package de.verdox.warplugin.model;

import org.bukkit.Color;

import java.util.Objects;

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

    public String getNameWithColor(){
        if(color.equals(Color.RED))
            return "&c"+name.toUpperCase();
        else if(color.equals(Color.BLUE))
            return "&b"+name.toUpperCase();
        else
            return "&f"+name.toUpperCase();
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", color=" + color +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        Team team = (Team) o;
        return Objects.equals(getName(), team.getName()) &&
                Objects.equals(getColor(), team.getColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getColor());
    }
}
