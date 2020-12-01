package de.verdox.warplugin.model;

import org.bukkit.Location;

import java.util.Objects;

public class LandmineBlock {

    private Location location;
    private Team team;

    public LandmineBlock(Location location, Team team){
        this.location = location;
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LandmineBlock)) return false;
        LandmineBlock that = (LandmineBlock) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, team);
    }
}
