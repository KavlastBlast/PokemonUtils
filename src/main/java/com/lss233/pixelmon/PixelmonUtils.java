package com.lss233.pixelmon;

import com.meteor.pbl.Main;
import com.meteor.pbl.data.BreedResult;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.specs.UnbreedableFlag;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumEggGroup;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.LanguageMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class PixelmonUtils {
    private final static PixelmonUtils instance = new PixelmonUtils();
    private final static boolean isPokemonBreedLimitEnabled;
    static {
        boolean localIsPokemonBreedLimitEnabled;
        try {
            Class.forName("com.meteor.pbl.Main");
            localIsPokemonBreedLimitEnabled = true;
        } catch (ClassNotFoundException e) {
            localIsPokemonBreedLimitEnabled = false;
        }

        isPokemonBreedLimitEnabled = localIsPokemonBreedLimitEnabled;
    }

    public static PixelmonUtils getInstance() {
        InputStream inputstream = LanguageMap.class.getResourceAsStream("/assets/minecraft/lang/zh_CN.lang");

        try {
            LanguageMap.inject(inputstream);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }
        return instance;
    }

    public void tempSavePlayerStorage(File baseDir, PlayerPartyStorage pps) throws IOException {
        File fileDir = new File(baseDir, "playerdata");
        if(!fileDir.exists())
            fileDir.mkdirs();
        File file = new File(fileDir, pps.getOwnerUUID().toString() + "-pokemon.yml");
        if(file.exists())
            file.delete();
        YamlConfiguration pokemonData = new YamlConfiguration();
        NBTTagCompound nbt = new NBTTagCompound();
        pps.writeToNBT(nbt);
        pokemonData.set("pps", nbt.toString());

        pokemonData.set("pokemons", Arrays.stream(pps.getAll()).map(i -> {
            if(i == null) return "";
            NBTTagCompound _nbt = new NBTTagCompound();
            i.writeToNBT(_nbt);
            return _nbt.toString();
        }).toArray());
        pokemonData.save(file);

    }

    public void loadTempSavedStorage(File baseDir, PlayerPartyStorage pps) throws NBTException {
        clearPlayerPartyStorage(pps);
        File fileDir = new File(baseDir, "playerdata");
        if(!fileDir.exists())
            fileDir.mkdirs();

        File file = new File(fileDir, pps.getOwnerUUID().toString() + "-pokemon.yml");
        if(!file.exists())  return;
        YamlConfiguration pokemonData = YamlConfiguration.loadConfiguration(file);
        pps.readFromNBT(JsonToNBT.getTagFromJson(pokemonData.getString("pps")));
        List<String> stringList = pokemonData.getStringList("pokemons");
        for (int i = 0, stringListSize = stringList.size(); i < stringListSize; i++) {
            String nbtStr = stringList.get(i);
            try {
                if (nbtStr.equalsIgnoreCase("")) continue;
                pps.set(i, Pixelmon.pokemonFactory.create(JsonToNBT.getTagFromJson(nbtStr)));
            } catch (NBTException e) {
                e.printStackTrace();
            }
        }
        pps.updatePlayer();
        file.delete();
    }

    public void clearPlayerPartyStorage(PlayerPartyStorage pps) {
        for(int i = 0; i < 6; ++i){
            pps.set(i, null);
        }
    }

    public BattleControllerBase startPlayerBattle(BattleParticipant p1, BattleParticipant p2, BattleRules rules){
        p1.startedBattle = true;
        p2.startedBattle = true;
        return BattleRegistry.startBattle(new BattleParticipant[]{p1}, new BattleParticipant[]{p2}, rules);
    }

    public EntityPlayer getEntityPlayer(Player player){
        return (EntityPlayer)(Object) ((CraftPlayer)player).getHandle();
    }

    public Map<String, String> serializePokemon(Pokemon pokemon) {
        return new HashMap<String, String>(){{
            for (StatsType value : StatsType.values()) {
                put("IVS." + value.name(), StringUtils.leftPad(String.valueOf(pokemon.getIVs().get(value)), 3));
            }
            for (StatsType value : StatsType.values()) {
                put("EVS." + value.name(), StringUtils.leftPad(String.valueOf(pokemon.getEVs().get(value)), 3));
            }
            put("level", String.valueOf(pokemon.getLevel()));
            put("nature", pokemon.getNature().getLocalizedName());
            put("growth", pokemon.getGrowth().getLocalizedName());
            put("gender", pokemon.getGender().getLocalizedName());
            put("held", pokemon.getHeldItem().getDisplayName());
            if(get("held").equalsIgnoreCase("Air"))
                replace("held", "无");
            put("name", pokemon.getSpecies().getLocalizedName());
            put("ability", pokemon.getAbility().getLocalizedName());
            put("shiny", pokemon.isShiny() ? "是" : "否");
            if(pokemon.getExtraStats() instanceof MewStats) {
                put("hasClones", "能");
                put("numCloned", String.valueOf(((MewStats)pokemon.getExtraStats()).numCloned));
                put("remainCloned", String.valueOf(MewStats.MAX_CLONES - ((MewStats)pokemon.getExtraStats()).numCloned));
            } else {
                put("hasClones", "不能");
                put("numCloned", "0");
                put("remainCloned", "0");
            }
            if(isPokemonBreedLimitEnabled) {
                put("canBreed", Arrays.asList(pokemon.getBaseStats().eggGroups)
                        .contains(EnumEggGroup.Undiscovered) ? "不能" :
                        UnbreedableFlag.UNBREEDABLE.matches(pokemon) ? "不能" :
                                Main.instance.getBreedHandler().getIstorage().getBreedData(pokemon, "get").isNoBreed() ? "不能" :
                                        "能");
            } else {
                put("canBreed", Arrays.asList(pokemon.getBaseStats().eggGroups).contains(EnumEggGroup.Undiscovered) ? "不能" : UnbreedableFlag.UNBREEDABLE.matches(pokemon) ? "不能" : "能");
            }


            for (int i = 1; i <= 4; i++) {
                if(pokemon.getMoveset().get(i - 1) == null){
                    put("moveset." + i, "无");
                } else {
                    put("moveset." + i, pokemon.getMoveset().get(i - 1).getMove().getLocalizedName());
                }
            }
        }};
    }

}
