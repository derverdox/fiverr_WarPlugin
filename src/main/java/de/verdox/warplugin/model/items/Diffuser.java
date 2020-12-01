package de.verdox.warplugin.model.items;

import de.verdox.vcore.customitems.model.CustomItem;
import de.verdox.vcore.files.Configuration;
import de.verdox.vcore.utils.ItemUtil;
import de.verdox.warplugin.Core;
import de.verdox.warplugin.model.GameManager;
import de.verdox.warplugin.model.LandmineBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Diffuser extends CustomItem {
    public Diffuser(Configuration config) {
        super(config);
    }

    @Override
    public void onEquip(Player player) {

    }

    @Override
    public void onUnEquip(Player player) {

    }

    @Override
    public boolean onPlayerDeath(Player player) {
        return false;
    }

    @Override
    public boolean rightClickBlock(Player player, Block block) {
        LandmineBlock landmineBlock = GameManager.getInstance().getLandMineBlock(block.getLocation());
        if(landmineBlock == null){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cNo Landmine found&7!"));
            return false;
        }
        GameManager.getInstance().removeLandMineBlock(landmineBlock);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aSuccessfully diffused landmine&7!"));
        return false;
    }

    @Override
    public boolean leftClickBlock(Player player, Block block) {
        return false;
    }

    @Override
    public boolean isItemEquipped(Player player) {
        return false;
    }

    @Override
    protected ItemStack customItem() {
        return ItemUtil.createStack(Core.core, Material.IRON_HOE.name(),1,"&eDiffuser");
    }

    @Override
    public String identifier() {
        return "War_Diffuser";
    }

    @Override
    protected Map<Integer, Enchantment> enchantments() {
        return null;
    }
}
