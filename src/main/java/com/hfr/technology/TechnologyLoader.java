package com.hfr.technology;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.hfr.util.XFLog;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

public class TechnologyLoader {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_PATH = "config/techtree/technology_tree.json";

    public static TechnologyTree load(Configuration config) {
        String path = config != null ? config.get("technology_system", "technology_tree", DEFAULT_PATH,
                "Path to the technology tree JSON relative to the config directory.").getString() : DEFAULT_PATH;
        File file = new File(path);
        if (!file.exists()) {
            XFLog.warn("[Technology] Technology tree not found at " + path + " - system disabled.");
            return null;
        }
        try {
            Reader reader = new FileReader(file);
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            reader.close();
            if (root == null || !root.has("technologies")) {
                XFLog.warn("[Technology] Invalid technology tree JSON at " + path);
                return null;
            }
            JsonArray arr = root.getAsJsonArray("technologies");
            List<Technology> techs = new ArrayList<Technology>();
            for (JsonElement el : arr) {
                try {
                    techs.add(parseTechnology(el.getAsJsonObject()));
                } catch (Exception e) {
                    XFLog.warn("[Technology] Failed to parse technology entry: " + e.getMessage());
                }
            }
            return new TechnologyTree(techs);
        } catch (Exception e) {
            XFLog.warn("[Technology] Failed to load technology tree from " + path + ": " + e.getMessage());
            return null;
        }
    }

    private static Technology parseTechnology(JsonObject obj) {
        String id = obj.has("id") ? obj.get("id").getAsString() : "unknown";
        String name = obj.has("name") ? obj.get("name").getAsString() : id;
        double tier = obj.has("tier") ? obj.get("tier").getAsDouble() : 0;
        TechnologyType type = TechnologyType.FREE;
        if (obj.has("type")) {
            try {
                type = TechnologyType.valueOf(obj.get("type").getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                XFLog.warn("[Technology] Unknown type '" + obj.get("type").getAsString() + "' for " + id + ", defaulting to FREE");
            }
        }
        List<String> prereqs = new ArrayList<String>();
        if (obj.has("prerequisites") && obj.get("prerequisites").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("prerequisites")) {
                prereqs.add(el.getAsString());
            }
        }
        int researchCost = obj.has("research_cost") ? obj.get("research_cost").getAsInt() : 0;
        int purchaseCost = obj.has("purchase_cost") ? obj.get("purchase_cost").getAsInt() : 0;
        List<TechnologyItem> items = new ArrayList<TechnologyItem>();
        if (obj.has("items") && obj.get("items").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("items")) {
                if (el.isJsonObject()) {
                    JsonObject io = el.getAsJsonObject();
                    String iid = io.has("id") ? io.get("id").getAsString() : "";
                    boolean crafting = io.has("crafting") ? io.get("crafting").getAsBoolean() : true;
                    boolean usage = io.has("usage") ? io.get("usage").getAsBoolean() : true;
                    int meta = io.has("metadata") ? io.get("metadata").getAsInt() : 0;
                    Item item = (Item) Item.itemRegistry.getObject(iid);
                    if (item == null) {
                        XFLog.warn("[Technology] Unknown item: " + iid + " in technology " + id);
                        continue;
                    }
                    items.add(new TechnologyItem(iid, meta, crafting, usage));
                } else if (el.isJsonPrimitive()) {
                    String iid = el.getAsString();
                    Item item = (Item) Item.itemRegistry.getObject(iid);
                    if (item == null) {
                        XFLog.warn("[Technology] Unknown item: " + iid + " in technology " + id);
                        continue;
                    }
                    items.add(new TechnologyItem(iid, true, true));
                }
            }
        }
        List<String> recipes = new ArrayList<String>();
        if (obj.has("recipes") && obj.get("recipes").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("recipes")) {
                recipes.add(el.getAsString());
            }
        }
        return new Technology(id, name, tier, type, prereqs, researchCost, purchaseCost, items, recipes);
    }
}