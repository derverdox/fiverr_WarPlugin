package de.verdox.warplugin.model.items;

import de.verdox.vcore.customitems.model.CustomItem;
import de.verdox.vcore.files.Configuration;
import de.verdox.vcore.utils.ItemUtil;
import de.verdox.warplugin.Core;
import de.verdox.warplugin.model.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LandMine extends CustomItem {
    public LandMine(Configuration config) {
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
        GameManager.getInstance().createLandMine(player,block.getLocation());
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aYou have placed a landmine&7!"));
        return true;
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
        return ItemUtil.createStack(Core.core, Material.BAT_SPAWN_EGG.name(),1,"&eLand-Mine");
    }

    @Override
    public String identifier() {
        return "War_LandMine";
    }

    @Override
    protected Map<Integer, Enchantment> enchantments() {
        return null;
    }
}
