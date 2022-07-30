package com.pixelmonmod.pixelmon.api.pokemon;

import com.pixelmonmod.pixelmon.api.exceptions.ShowdownImportException;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumNature;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PokemonSerializer {
    public static final String SPECIES_TEXT = "Pokémon";
    public static final String GENDER_TEXT = "Gender";
    public static final String ABILITY_TEXT = "Ability";
    public static final String LEVEL_TEXT = "Level";
    public static final String SHINY_TEXT = "Shiny";
    public static final String HAPPINESS_TEXT = "Happiness";
    public static final String EV_TEXT = "EVs";
    public static final String NATURE_TEXT = "Nature";
    public static final String IV_TEXT = "IVs";
    public static final String POKE_BALL_TEXT = "Poke Ball";
    public static final String GROWTH_TEXT = "Growth";
    public static final String CLONES_TEXT = "Clones";
    public static final String RUBY_TEXT = "Rubies";
    public static final String MINIOR_CORE_TEXT = "MiniorCore";
    public static final String SMELT_TEXT = "Smelts";
    public static final String MOVE_TEXT = "Moves";
    public static final String[] STAT_TEXT = new String[]{"hp", "Atk", "Def", "SpA", "SpD", "Spe"};
    public static final char MALE_SYMBOL = 'M';
    public static final char FEMALE_SYMBOL = 'F';
    public static final String SHINY_YES = "Yes";
    private static Map<String, String> importNameMap;

    private PokemonSerializer() {
    }


    public static void addLine(StringBuilder builder, String label) {
        builder.append(label);
        builder.append("\n");
    }

    public static void addColonSeparated(StringBuilder builder, String label, Object value) {
        builder.append(label);
        builder.append(": ");
        builder.append(value.toString());
        builder.append("\n");
    }

    private static String convertCamelCaseToWords(String text) {
        if (text != null && text.length() >= 2) {
            StringBuilder newText = new StringBuilder();
            int textLength = text.length();

            for(int i = 0; i < textLength; ++i) {
                char currentChar = text.charAt(i);
                if (currentChar >= 'A' && currentChar <= 'Z' && i > 0 && i < textLength) {
                    newText.append(' ');
                }

                newText.append(currentChar);
            }

            return newText.toString();
        } else {
            return text;
        }
    }

    private static void writeStats(StringBuilder exportText, int[] statArray, String statType, int defaultValue) {
        boolean defaultStats = true;
        int[] var5 = statArray;
        int i = statArray.length;

        for(int var7 = 0; var7 < i; ++var7) {
            int stat = var5[var7];
            if (stat != defaultValue) {
                defaultStats = false;
                break;
            }
        }

        if (!defaultStats) {
            exportText.append(statType);
            exportText.append(": ");
            boolean hasPrevious = false;

            for(i = 0; i < statArray.length; ++i) {
                if (statArray[i] != defaultValue) {
                    if (hasPrevious) {
                        exportText.append(" / ");
                    }

                    exportText.append(statArray[i]);
                    exportText.append(" ");
                    exportText.append(STAT_TEXT[i]);
                    hasPrevious = true;
                }
            }

            exportText.append("\n");
        }

    }

    public static Pokemon importText(String importText) throws ShowdownImportException {
        int[] ivs = new int[6];
        int[] evs = new int[6];

        Pokemon current = ImportExportConverter.importText(importText);
        String[] importTextSplit = importText.split("\n");
        try {
            String currentLine;

            boolean setIVs = false;
            int currentIndex = 1;

            for (int i = 0; i < 6; ++i) {
                ivs[i] = 31;
            }

            while (currentIndex < importTextSplit.length) {

                currentLine = importTextSplit[currentIndex];
                String moveText;
                if (currentLine.startsWith("Lv")) {
                    NBTTagCompound tag = new NBTTagCompound();
                    current.writeToNBT(tag);
                    tag.setInteger("Level", Integer.parseInt(getStringAfterColon(currentLine)));
                    current.readFromNBT(tag);

                } else if (currentLine.startsWith("EVs")) {
                    parseStats(currentLine, evs, (statValue, totalStats) -> statValue);
                } else if (currentLine.trim().endsWith("Nature")) {
                    moveText = currentLine.substring(0, currentLine.indexOf(32));
                    current.setNature(EnumNature.natureFromString(moveText));
                } else if (currentLine.startsWith("IVs")) {
                    setIVs = true;
                    parseStats(currentLine, ivs, (statValue, totalStats) -> statValue);
                    current.getIVs().fillFromArray(ivs);
                } else if (currentLine.trim().startsWith("Health")){
                    NBTTagCompound tag = new NBTTagCompound();
                    current.writeToNBT(tag);
                    tag.setInteger("Health", Integer.parseInt(getStringAfterColon(currentLine)));
                    tag.setInteger("StatsHP", Integer.parseInt(getStringAfterColon(currentLine)));
                    current.readFromNBT(tag);
                    try {
                        Field field = current.getClass().getDeclaredField("health");
                        field.setAccessible(true);
                        field.setInt(current, Integer.parseInt(getStringAfterColon(currentLine)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    current.markDirty(EnumUpdateType.HP);
                } else if (currentLine.trim().startsWith("Ability")) {
                    current.setAbility(getStringAfterColon(currentLine));
                } else if(currentLine.trim().startsWith("HeldItem")) {
                    Item item = PixelmonItems.getItemFromName(getStringAfterColon(currentLine));
                    if(item == null)
                        item = PixelmonItems.getItemFromName("item." + getStringAfterColon(currentLine));
                    current.setHeldItem(new ItemStack(item));
                }

                ++currentIndex;
            }
        } catch (NullPointerException | NoSuchElementException | NumberFormatException | IndexOutOfBoundsException var53) {
            var53.printStackTrace();
            return null;
        }
        return current;
    }

    public static int getIntAfterColon(String string) {
        return Integer.parseInt(getStringAfterColon(string));
    }

    public static String getStringAfterColon(String string) {
        return string.substring(string.indexOf(58) + 1).trim();
    }

    private static void parseStats(String statString, int[] statArray, StatValidator validator) {
        String[] splitStats = getStringAfterColon(statString).split("\\/");
        int totalStats = 0;
        String[] var5 = splitStats;
        int var6 = splitStats.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String stat = var5[var7];
            stat = stat.trim();
            String statType = stat.substring(stat.lastIndexOf(32) + 1, stat.length());

            for(int i = 0; i < STAT_TEXT.length; ++i) {
                if (STAT_TEXT[i].equalsIgnoreCase(statType)) {
                    int statAmount = Integer.parseInt(stat.substring(0, stat.indexOf(32)));
                    statAmount = validator.validateStat(statAmount, totalStats);
                    statArray[i] = statAmount;
                    totalStats += statAmount;
                }
            }
        }

    }

    private static String convertName(String nameText) {
        importNameMap = null;
        if (importNameMap == null) {
            initializeNameMap();
        }

        return importNameMap.containsKey(nameText) ? (String)importNameMap.get(nameText) : nameText;
    }

    private static void initializeNameMap() {
        importNameMap = new HashMap();
        importNameMap.put("Mime Jr.", "Mime_Jr.");
        importNameMap.put("Mr. Mime", "MrMime");
        importNameMap.put("Nidoran-F", "Nidoranfemale");
        importNameMap.put("Nidoran-M", "Nidoranmale");
        importNameMap.put("AncientPower", "Ancient Power");
        importNameMap.put("BubbleBeam", "Bubble Beam");
        importNameMap.put("DoubleSlap", "Double Slap");
        importNameMap.put("DragonBreath", "Dragon Breath");
        importNameMap.put("DynamicPunch", "Dynamic Punch");
        importNameMap.put("ExtremeSpeed", "Extreme speed");
        importNameMap.put("FeatherDance", "Feather Dance");
        importNameMap.put("Faint attack", "Feint attack");
        importNameMap.put("GrassWhistle", "Grass Whistle");
        importNameMap.put("Hi Jump Kick", "High Jump Kick");
        importNameMap.put("Sand-attack", "Sand attack");
        importNameMap.put("Selfdestruct", "Self-Destruct");
        importNameMap.put("SmellingSalt", "Smelling Salts");
        importNameMap.put("SmokeScreen", "Smokescreen");
        importNameMap.put("Softboiled", "Soft-Boiled");
        importNameMap.put("SolarBeam", "Solar Beam");
        importNameMap.put("SonicBoom", "Sonic Boom");
        importNameMap.put("ThunderShock", "Thunder Shock");
        importNameMap.put("U-Turn", "U-turn");
    }

    private interface StatValidator {
        int validateStat(int var1, int var2);
    }
}
