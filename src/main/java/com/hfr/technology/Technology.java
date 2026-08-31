package com.hfr.technology;

import java.util.ArrayList;
import java.util.List;

public class Technology {
    public final String id;
    public final String name;
    public final double tier;
    public final TechnologyType type;
    public final List<String> prerequisites;
    public final int researchCost;
    public final int purchaseCost;
    public final List<TechnologyItem> items;
    public final List<String> recipes;

    public Technology(String id, String name, double tier, TechnologyType type,
                      List<String> prerequisites, int researchCost, int purchaseCost,
                      List<TechnologyItem> items, List<String> recipes) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.type = type;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<String>();
        this.researchCost = researchCost;
        this.purchaseCost = purchaseCost;
        this.items = items != null ? items : new ArrayList<TechnologyItem>();
        this.recipes = recipes != null ? recipes : new ArrayList<String>();
    }
}