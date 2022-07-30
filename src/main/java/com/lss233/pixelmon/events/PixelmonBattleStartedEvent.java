package com.lss233.pixelmon.events;

import com.lss233.pixelmon.NMSUtil;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PixelmonBattleStartedEvent extends Event implements Cancellable {
    private final static HandlerList handlerList = new HandlerList();
    private final BattleStartedEvent event;
    public PixelmonBattleStartedEvent(BattleStartedEvent event) {
        this.event = event;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelable();
    }

    public BattleControllerBase getBattleControllerBase() {
        return event.bc;
    }

    public BattleParticipant[] getParticipants1(){
        return event.participant1;
    }

    public BattleParticipant[] getParticipants2(){
        return event.participant2;
    }

    public List<Player> getPlayers() {
        return getParticipants().stream().filter(i -> i.getEntity() instanceof EntityPlayerMP).map(BattleParticipant::getEntity).map(i -> NMSUtil.getPlayer((EntityPlayer) i)).collect(Collectors.toList());
    }
    public List<BattleParticipant> getParticipants(){
        return Stream.concat(Arrays.stream(getParticipants1()), Arrays.stream(getParticipants2())).collect(Collectors.toList());
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCanceled(cancel);
    }
}
