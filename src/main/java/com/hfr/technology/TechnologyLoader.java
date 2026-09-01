package com.hfr.technology;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.hfr.util.XFLog;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

public class TechnologyLoader {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_PATH = "config/research/technology_tree.json";
    private static final String STARTER_TREE = "{\n  \"currency\": { \"research\": \"research_points\" },\n  \"technologies\": []\n}\n";
    private static final String README = "# HFR Research Technology Tree\n\nThis folder is created automatically. `technology_tree.json` is only loaded when the HFR research system is enabled in hfr.cfg.\n\n## Currency\n\nSet research currency in the tree root: `\"currency\": { \"research\": \"research_points\" }`. Set license purchase currency in hfr.cfg with `hfr_currency_purchase`.\n\n## Technologies\n\nAdd entries to the `technologies` array. Each needs a permanent `id`, display `name`, `tier`, `type` (`free`, `unlockable`, or `research_required`), `prerequisites`, costs, items, and recipes. Item IDs use `modid:item`; metadata variants may use `modid:item:metadata`.\n";

    /** Creates editable starter files without parsing or enabling the technology tree. */
    public static void ensureStarterFiles(Configuration config) {
        File directory = ensureDirectory(config);
        String configuredName = config != null ? TechnologyManager.config.technologyTreePath : "technology_tree.json";
        writeIfMissing(new File(directory, "README.md"), README);
        writeIfMissing(new File(directory, configuredName), STARTER_TREE);
    }

    private static void writeIfMissing(File file, String contents) {
        if (file.exists()) return;
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(contents);
            XFLog.info("[Technology] Created starter file at " + file.getAbsolutePath());
        } catch (IOException e) {
            XFLog.warn("[Technology] Could not create starter file at " + file.getAbsolutePath() + ": " + e.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (IOException ignored) { }
        }
    }

    /** Returns the folder containing technology tree JSON files, creating it if necessary. */
    public static File ensureDirectory(Configuration config) {
        File directory = config != null ? new File(config.getConfigFile().getParentFile(), "research") : new File("config/research");
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                XFLog.info("[Technology] Created research directory at " + directory.getAbsolutePath());
            } else {
                XFLog.warn("[Technology] Could not create research directory at " + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    public static TechnologyTree load(Configuration config) {
        String configuredName = config != null ? TechnologyManager.config.technologyTreePath : "technology_tree.json";
        File directory = ensureDirectory(config);
        File file = new File(directory, configuredName);
        String path = file.getPath();
        if (!file.exists()) {
            XFLog.warn("[Technology] Technology tree not found at " + path + " - place a JSON there and run /xc research reload.");
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
            String researchCurrency = "research_points";
            if (root.has("currency") && root.get("currency").isJsonObject() && root.getAsJsonObject("currency").has("research"))
                researchCurrency = root.getAsJsonObject("currency").get("research").getAsString();
            return new TechnologyTree(techs, researchCurrency);
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
                type = TechnologyType.valueOf(obj.get("type").getAsString().toUpperCase().replace('-', '_'));
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