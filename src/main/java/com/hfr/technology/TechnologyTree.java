package com.hfr.technology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnologyTree {
    public final List<Technology> technologies;
    /** Currency name declared by the loaded technology-tree JSON. */
    public final String researchCurrency;
    public final Map<String, Technology> byId;
    public final Map<String, String> itemToTech;
    public final Map<String, String> recipeToTech;

    public TechnologyTree(List<Technology> technologies, String researchCurrency) {
        this.researchCurrency = researchCurrency == null || researchCurrency.trim().isEmpty() ? "research_points" : researchCurrency;
        this.technologies = technologies;
        this.byId = new HashMap<String, Technology>();
        this.itemToTech = new HashMap<String, String>();
        this.recipeToTech = new HashMap<String, String>();
        buildIndex();
    }

    private void buildIndex() {
        for (Technology tech : technologies) {
            byId.put(tech.id, tech);
            for (TechnologyItem ti : tech.items) {
                String key = ti.metadata != 0 ? ti.id + ":" + ti.metadata : ti.id;
                if (!itemToTech.containsKey(key)) {
                    itemToTech.put(key, tech.id);
                }
            }
            for (String recipe : tech.recipes) {
                if (!recipeToTech.containsKey(recipe)) {
                    recipeToTech.put(recipe, tech.id);
                }
            }
        }
    }

    public Technology get(String id) {
        return byId.get(id);
    }

    public String getTechnologyForItem(String itemId) {
        return itemToTech.get(itemId);
    }

    public String getTechnologyForRecipe(String recipeId) {
        return recipeToTech.get(recipeId);
    }
}