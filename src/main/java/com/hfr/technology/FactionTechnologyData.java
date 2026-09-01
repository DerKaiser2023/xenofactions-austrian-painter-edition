package com.hfr.technology;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class FactionTechnologyData {
    public final Set<String> researched = new HashSet<String>();
    public final Set<String> purchased = new HashSet<String>();

    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList researchList = new NBTTagList();
        for (String id : researched) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", id);
            researchList.appendTag(tag);
        }
        nbt.setTag("techResearched", researchList);
        NBTTagList purchaseList = new NBTTagList();
        for (String id : purchased) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", id);
            purchaseList.appendTag(tag);
        }
        nbt.setTag("techPurchased", purchaseList);
    }

    public static FactionTechnologyData readFromNBT(NBTTagCompound nbt) {
        FactionTechnologyData data = new FactionTechnologyData();
        NBTTagList researchList = nbt.getTagList("techResearched", 10);
        for (int i = 0; i < researchList.tagCount(); i++) {
            NBTTagCompound tag = researchList.getCompoundTagAt(i);
            data.researched.add(tag.getString("id"));
        }
        NBTTagList purchaseList = nbt.getTagList("techPurchased", 10);
        for (int i = 0; i < purchaseList.tagCount(); i++) {
            NBTTagCompound tag = purchaseList.getCompoundTagAt(i);
            data.purchased.add(tag.getString("id"));
        }
        return data;
    }

    public boolean isResearched(String id) {
        return researched.contains(id);
    }

    public boolean isPurchased(String id) {
        return purchased.contains(id);
    }

    public boolean isUnlocked(String id) {
        return researched.contains(id) && purchased.contains(id);
    }

    public void setResearched(String id, boolean value) {
        if (value) researched.add(id); else researched.remove(id);
    }

    public void setPurchased(String id, boolean value) {
        if (value) purchased.add(id); else purchased.remove(id);
    }

    public void clearAll() {
        researched.clear();
        purchased.clear();
    }
}