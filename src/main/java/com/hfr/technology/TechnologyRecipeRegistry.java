package com.hfr.technology;

import java.util.*;

public class TechnologyRecipeRegistry {
    public static final Map<String, String> recipeTechnologyMap = new HashMap<String, String>();

    public static void rebuild(TechnologyTree tree) {
        recipeTechnologyMap.clear();
        if (tree == null) return;
        for (Technology tech : tree.technologies) {
            for (String recipe : tech.recipes) {
                if (!recipeTechnologyMap.containsKey(recipe)) {
                    recipeTechnologyMap.put(recipe, tech.id);
                }
            }
        }
    }
}