package com.hfr.technology;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import com.hfr.util.XFLog;
import com.hfr.clowder.Clowder;

public class TechnologyManager {
    public static TechnologyConfig config = new TechnologyConfig();
    public static TechnologyTree tree;
    public static boolean treeAvailable = false;
    public static boolean initialized = false;

    public static void init(Configuration config) {
        if (initialized) return;
        TechnologyManager.config.load(config);
        tree = TechnologyLoader.load(config);
        treeAvailable = tree != null;
        if (TechnologyManager.config.enabled && !treeAvailable) {
            XFLog.warn("[Technology] Technology system enabled but no tree loaded. Block/Item registrations and /xc research gui will still work; gameplay hooks will no-op until a tree is loaded (drop technology_tree.json under config/research/ and run /xc research reload).");
        }
        initialized = true;
    }

    /** Currency identifier declared at the root of technology_tree.json. */
    public static String getResearchCurrency() {
        return tree == null ? "research_points" : tree.researchCurrency;
    }

    /** Currency identifier configured in hfr.cfg for technology licenses. */
    public static String getPurchaseCurrency() {
        return config.currencyPurchase;
    }

    public static boolean isEnabled() {
        return initialized && config.enabled && tree != null;
    }

    public static boolean isConfigEnabled() {
        return initialized && config.enabled;
    }

    public static boolean isTreeAvailable() {
        return treeAvailable;
    }

    public static Technology getTechnology(String id) {
        if (!isEnabled()) return null;
        return tree.get(id);
    }

    public static Clowder getFaction(EntityPlayer player) {
        if (!isEnabled()) return null;
        return Clowder.getClowderFromPlayer(player);
    }

    public static boolean isResearched(EntityPlayer player, String techId) {
        Technology tech = getTechnology(techId);
        if (tech != null && tech.type == TechnologyType.FREE) return true;
        if (!isEnabled()) return true;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return false;
        return clowder.techData.isResearched(techId);
    }

    public static boolean isPurchased(EntityPlayer player, String techId) {
        Technology tech = getTechnology(techId);
        if (tech != null && tech.type == TechnologyType.FREE) return true;
        if (!isEnabled()) return true;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return false;
        return clowder.techData.isPurchased(techId);
    }

    public static boolean isUnlocked(EntityPlayer player, String techId) {
        Technology tech = getTechnology(techId);
        if (tech != null && tech.type == TechnologyType.FREE) return true;
        if (!isEnabled()) return true;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return false;
        return clowder.techData.isUnlocked(techId);
    }

    public static boolean canResearch(EntityPlayer player, String techId) {
        if (!isEnabled()) return false;
        Technology tech = tree.get(techId);
        if (tech == null) return false;
        if (tech.type == TechnologyType.FREE) return false;
        if (tech.type == TechnologyType.UNLOCKABLE) return false;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return false;
        if (clowder.techData.isResearched(techId)) return false;
        for (String pre : tech.prerequisites) {
            if (!isUnlocked(player, pre)) return false;
        }
        return true;
    }

    public static boolean canPurchase(EntityPlayer player, String techId) {
        if (!isEnabled()) return false;
        Technology tech = tree.get(techId);
        if (tech == null) return false;
        if (tech.type == TechnologyType.FREE) return false;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return false;
        if (clowder.techData.isPurchased(techId)) return false;
        if (tech.type == TechnologyType.RESEARCH_REQUIRED && !clowder.techData.isResearched(techId)) return false;
        return true;
    }

    public static boolean canUseItem(EntityPlayer player, ItemStack stack) {
        if (!isEnabled()) return true;
        if (stack == null || stack.getItem() == null) return true;
        String itemId = Item.itemRegistry.getNameForObject(stack.getItem());
        if (itemId == null) return true;
        String key = itemId;
        if (stack.getItemDamage() != 0) key = itemId + ":" + stack.getItemDamage();
        String techId = tree.getTechnologyForItem(key);
        if (techId == null) return true;
        Technology tech = tree.get(techId);
        if (tech == null) return true;
        TechnologyItem ti = findTechnologyItem(tech, key);
        if (ti == null) return true;
        if (!ti.usage) return true;
        return isUnlocked(player, techId);
    }

    public static boolean canCraft(EntityPlayer player, ItemStack result) {
        if (!isEnabled()) return true;
        if (result == null || result.getItem() == null) return true;
        String itemId = Item.itemRegistry.getNameForObject(result.getItem());
        if (itemId == null) return true;
        String key = itemId;
        if (result.getItemDamage() != 0) key = itemId + ":" + result.getItemDamage();
        String techId = tree.getTechnologyForItem(key);
        if (techId == null) return true;
        Technology tech = tree.get(techId);
        if (tech == null) return true;
        TechnologyItem ti = findTechnologyItem(tech, key);
        if (ti == null) return true;
        if (!ti.crafting) return true;
        return isUnlocked(player, techId);
    }

    public static boolean canCraftRecipe(EntityPlayer player, String recipeId) {
        if (!isEnabled()) return true;
        if (recipeId == null) return true;
        String techId = tree.getTechnologyForRecipe(recipeId);
        if (techId == null) return true;
        return isUnlocked(player, techId);
    }

    public static TechnologyItem findTechnologyItem(Technology tech, String key) {
        for (TechnologyItem ti : tech.items) {
            String k = ti.metadata != 0 ? ti.id + ":" + ti.metadata : ti.id;
            if (k.equals(key)) return ti;
        }
        return null;
    }

    public static void research(EntityPlayer player, String techId) {
        if (!isEnabled()) return;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return;
        clowder.techData.setResearched(techId, true);
    }

    public static void purchase(EntityPlayer player, String techId) {
        if (!isEnabled()) return;
        Clowder clowder = getFaction(player);
        if (clowder == null || clowder.techData == null) return;
        clowder.techData.setPurchased(techId, true);
    }
}