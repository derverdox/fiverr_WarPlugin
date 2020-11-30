package de.verdox.warplugin;

import de.verdox.vcore.VCore;
import de.verdox.warplugin.commands.MainCommand;
import de.verdox.warplugin.listener.MainListener;
import de.verdox.warplugin.model.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    public static VCore vcore;
    public static Core core;

    @Override
    public void onEnable() {
        core = this;
        vcore = VCore.getInstance(core);
        vcore.registerCommands();
        vcore.registerEvents();
        vcore.enable();
        getCommand("war").setExecutor(new MainCommand());
        getCommand("war").setTabCompleter(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new MainListener(),this);
        GameManager.getInstance();
    }
}
