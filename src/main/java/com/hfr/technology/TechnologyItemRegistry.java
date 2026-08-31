package com.hfr.technology;

import java.util.*;

public class TechnologyItemRegistry {
    public static final Map<String, String> itemTechnologyMap = new HashMap<String, String>();

    public static void rebuild(TechnologyTree tree) {
        itemTechnologyMap.clear();
        if (tree == null) return;
        for (Technology tech : tree.technologies) {
            for (TechnologyItem ti : tech.items) {
                String key = ti.metadata != 0 ? ti.id + ":" + ti.metadata : ti.id;
                if (!itemTechnologyMap.containsKey(key)) {
                    itemTechnologyMap.put(key, tech.id);
                }
            }
        }
    }
}