package de.verdox.warplugin.model;

import com.boydti.fawe.bukkit.wrapper.AsyncBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Objects;

public class CaptureBlock {
    private Block block;
    private CapturePoint capturePoint;

    public CaptureBlock(Block block, CapturePoint capturePoint){
        this.block = block;
        this.capturePoint = capturePoint;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation(){return block.getLocation();}

    public CapturePoint getCapturePoint() {
        return capturePoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaptureBlock)) return false;
        CaptureBlock that = (CaptureBlock) o;
        return Objects.equals(getBlock(), that.getBlock()) &&
                Objects.equals(getCapturePoint(), that.getCapturePoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlock(), getCapturePoint());
    }
}
