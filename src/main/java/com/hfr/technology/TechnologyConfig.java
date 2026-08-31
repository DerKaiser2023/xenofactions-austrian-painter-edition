package com.hfr.technology;

import net.minecraftforge.common.config.Configuration;

public class TechnologyConfig {
    public boolean enabled = false;
    public String currencyResearch = "research_points";
    public String currencyPurchase = "credits";
    public boolean researchSharedByFaction = true;
    public boolean purchaseSharedByFaction = true;
    public boolean sequentialResearch = true;
    public String technologyTreePath = "config/techtree/technology_tree.json";

    public void load(Configuration config) {
        if (config == null) return;
        config.addCustomCategoryComment("XENOFACTIONS_21_TECHNOLOGY", "Technology, Research & Licensing System");
        enabled = config.get("XENOFACTIONS_21_TECHNOLOGY", "enabled", enabled,
                "Enable the technology, research and licensing system. When false, XenoFactions behaves normally.").getBoolean();
        currencyResearch = config.get("XENOFACTIONS_21_TECHNOLOGY", "currency_research", currencyResearch,
                "Currency identifier used for researching technologies.").getString();
        currencyPurchase = config.get("XENOFACTIONS_21_TECHNOLOGY", "currency_purchase", currencyPurchase,
                "Currency identifier used for purchasing technology licenses.").getString();
        researchSharedByFaction = config.get("XENOFACTIONS_21_TECHNOLOGY", "research_shared_by_faction", researchSharedByFaction,
                "When true, research progress is shared by all faction members.").getBoolean();
        purchaseSharedByFaction = config.get("XENOFACTIONS_21_TECHNOLOGY", "purchase_shared_by_faction", purchaseSharedByFaction,
                "When true, purchased licenses are shared by all faction members.").getBoolean();
        sequentialResearch = config.get("XENOFACTIONS_21_TECHNOLOGY", "sequential_research", sequentialResearch,
                "When true, prerequisites must be researched before higher-tier technologies become available.").getBoolean();
        technologyTreePath = config.get("XENOFACTIONS_21_TECHNOLOGY", "technology_tree", technologyTreePath,
                "Path to the technology tree JSON file.").getString();
    }
}