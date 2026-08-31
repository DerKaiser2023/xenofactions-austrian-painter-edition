# XenoFactions technology tree

Place `technology_tree.json` in this directory. The system is disabled by default; enable it in `XenoFactions.cfg` under `XENOFACTIONS_21_TECHNOLOGY`.

## Shape

```json
{
  "technologies": [
    {
      "id": "tech.early_industry",
      "name": "Early Industry",
      "tier": -0.5,
      "type": "research_required",
      "research_cost": 500,
      "purchase_cost": 1000,
      "prerequisites": ["tech.ironworking"],
      "items": [{ "id": "hfr:part_sawdust", "crafting": true, "usage": true }],
      "recipes": ["hfr:basic_engine"]
    }
  ]
}
```

Technology IDs are permanent and unique. Types are `free`, `unlockable`, and `research_required`. `free` technologies are unlocked automatically. Items use complete Forge registry IDs and may include `:metadata`, for example `minecraft:wool:14`; item objects may alternatively provide `"metadata": 14`. Invalid item IDs are logged and skipped.

Prerequisites are explicit; tiers are display/progression values and do not create dependencies automatically. `research_required` technologies must be researched before purchase. `unlockable` technologies can be purchased directly. The faction owns completed research and licenses, so members gain or lose access when joining or leaving.
