package com.lss233.pixelmon.events;

import com.google.common.collect.ImmutableMap;
import com.lss233.pixelmon.NMSUtil;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PixelmonBattleEndEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final BattleEndEvent event;
    public PixelmonBattleEndEvent(BattleEndEvent event) {
        this.event = event;
    }

    public List<EntityPlayerMP> getNMPlayers() {
        return event.getPlayers();
    }

    public List<Player> getPlayers() {
        return event.getPlayers().stream().map(NMSUtil::getPlayer).collect(Collectors.toList());
    }

    public ImmutableMap<BattleParticipant, BattleResults> getResults() {
        return event.results;
    }

    public EnumBattleEndCause getCause() {
        return event.cause;
    }

    public BattleControllerBase getBattleControllerBase() {
        return event.bc;
    }

    public Optional<BattleResults> getResultByEntity(Entity entity) {
        return event.results.entrySet().stream().filter(i -> i.getKey().getEntity().equals(entity)).findFirst().map(Map.Entry::getValue);
    }

    public Optional<BattleResults> getResultByEntity(Class<? extends Entity> clazz) {
        return event.results.entrySet().stream().filter(i -> clazz.isInstance(i.getKey().getEntity())).findFirst().map(Map.Entry::getValue);
    }

    public Optional<BattleResults> getResultByParticipant(Class<? extends BattleParticipant> clazz) {
        return event.results.entrySet().stream().filter(i -> clazz.isInstance(i.getKey())).findFirst().map(Map.Entry::getValue);
    }

    public Optional<BattleParticipant> getParticipantByResult(BattleResults results) {
        return event.results.entrySet().stream().filter(i -> results.equals(i.getValue())).findFirst().map(Map.Entry::getKey);
    }

    public boolean isAbnormal() {
        return event.abnormal;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
