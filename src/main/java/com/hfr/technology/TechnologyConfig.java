package com.hfr.technology;

import net.minecraftforge.common.config.Configuration;
import com.hfr.lib.RefStrings;

public class TechnologyConfig {
    public boolean enabled = false;

    public String currencyPurchase = "credits";
    public boolean researchSharedByFaction = true;
    public boolean purchaseSharedByFaction = true;
    public boolean sequentialResearch = true;
    public String technologyTreePath = "technology_tree.json";

    public void load(Configuration config) {
        if (config == null) return;
        String cat = "XENOFACTIONS_21_TECHNOLOGY";
        config.addCustomCategoryComment(cat, "Technology, Research & Licensing System");
        enabled = config.get(cat, RefStrings.MODID + "_enabled", enabled,
                "Enable the technology, research and licensing system. When false, XenoFactions behaves normally.").getBoolean();
        currencyPurchase = config.get(cat, RefStrings.MODID + "_currency_purchase", currencyPurchase,
                "Currency identifier used for purchasing technology licenses.").getString();
        researchSharedByFaction = config.get(cat, RefStrings.MODID + "_research_shared_by_faction", researchSharedByFaction,
                "When true, research progress is shared by all faction members.").getBoolean();
        purchaseSharedByFaction = config.get(cat, RefStrings.MODID + "_purchase_shared_by_faction", purchaseSharedByFaction,
                "When true, purchased licenses are shared by all faction members.").getBoolean();
        sequentialResearch = config.get(cat, RefStrings.MODID + "_sequential_research", sequentialResearch,
                "When true, prerequisites must be researched before higher-tier technologies become available.").getBoolean();
        technologyTreePath = config.get(cat, RefStrings.MODID + "_technology_tree", technologyTreePath,
                "Technology tree filename under the HFR config directory/research.").getString();
    }
}
