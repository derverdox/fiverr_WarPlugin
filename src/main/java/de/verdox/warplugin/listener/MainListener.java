package de.verdox.warplugin.listener;

import de.verdox.vcore.playersession.PlayerSession;
import de.verdox.vcore.playersession.events.PlayerSessionCreateEvent;
import de.verdox.warplugin.model.GameManager;
import de.verdox.warplugin.model.Team;
import de.verdox.warplugin.model.WarPlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MainListener implements Listener {

    @EventHandler
    public void breakListener(BlockBreakEvent e){
        if(GameManager.getInstance().getCapturePoint(e.getBlock().getLocation()) != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void sessionCreate(PlayerSessionCreateEvent e){
        PlayerSession session = e.getPlayerSession();
        Team team = GameManager.getInstance().getSaveFile().getPlayerTeam(e.getPlayer().getUniqueId());
        WarPlayerData warPlayerData = new WarPlayerData(e.getPlayer(), team);
        session.addDataToSession(warPlayerData);
        GameManager.getInstance().getSaveFile().updatePlayerData(warPlayerData);
    }

}
