package com.lss233.pixelmon;

import catserver.api.bukkit.event.ForgeEvent;
import com.lss233.pixelmon.events.PixelmonBattleEndEvent;
import com.lss233.pixelmon.events.PixelmonBattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PixelmonEventListener implements Listener {
    @EventHandler
    public void onPixelmonEvent(ForgeEvent forgeEvent){
        if(forgeEvent.getForgeEvent() instanceof BattleStartedEvent){
            Bukkit.getServer().getPluginManager().callEvent(new PixelmonBattleStartedEvent((BattleStartedEvent) forgeEvent.getForgeEvent()));
        } else if(forgeEvent.getForgeEvent() instanceof BattleEndEvent){
            Bukkit.getServer().getPluginManager().callEvent(new PixelmonBattleEndEvent((BattleEndEvent) forgeEvent.getForgeEvent()));
        }
    }
}
