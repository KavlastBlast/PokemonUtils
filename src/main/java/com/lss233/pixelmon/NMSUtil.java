package com.lss233.pixelmon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NMSUtil {
    public static EntityPlayer getEntityPlayer(Player player){
        return (EntityPlayer)(Object) ((CraftPlayer)player).getHandle();
    }
    public static Player getPlayer(EntityPlayer player) {
        return Bukkit.getPlayer(((Entity)player).getUniqueID());
    }

    public static NBTTagCompound getTagFromJson(String str) throws NBTException {
        return JsonToNBT.getTagFromJson(str);
    }

    public static NBTTagCompound getNBTTagCompound(NBTTagCompound tag, String key) {
        return tag.getCompoundTag(key);
    }
    public static ItemStack createItemStack(NBTTagCompound tag){
        return convertItemStack(new net.minecraft.item.ItemStack(tag));
    }

    public static ItemStack convertItemStack(net.minecraft.item.ItemStack item) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) (Object) item);
    }
}
