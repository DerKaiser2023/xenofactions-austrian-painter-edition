# XenoFactions research tree

The technology, research, and licensing system lives here. By default it is
**enabled** in `hfr.cfg` under `XENOFACTIONS_21_TECHNOLOGY` (key `hfr_enabled`),
but the system only becomes fully functional once a tree JSON file is present in
this folder.

## Where the JSON lives

XenoFactions reads `<minecraft-config>/research/technology_tree.json` by default.
On a dedicated server that resolves to `./config/research/technology_tree.json`,
and on a client to `.minecraft/config/research/technology_tree.json`.

You can change the filename from `hfr.cfg` using the `hfr_technology_tree` key.

The directory, this README, and a sample `technology_tree.json` are written here
the first time the mod starts with the technology system enabled. Edit the
sample, then run `/xc research reload` (or restart the server) to apply it.

## File shape

```json
{
  "currency": {
    "research": "research_points"
  },
  "technologies": [
    {
      "id": "tech.early_industry",
      "name": "Early Industry",
      "tier": -0.5,
      "type": "research_required",
      "research_cost": 500,
      "purchase_cost": 1000,
      "prerequisites": ["tech.ironworking"],
      "items": [
        { "id": "hfr:part_sawdust", "crafting": true, "usage": true }
      ],
      "recipes": ["hfr:basic_engine"]
    }
  ]
}
```

### Root fields

| Field | Required | Description |
| --- | --- | --- |
| `currency.research` | no | Identifier used for the research cost currency. Defaults to `research_points`. The purchase currency is configured in `hfr.cfg` (`hfr_currency_purchase`, default `credits`). |
| `technologies` | yes | Array of technology objects. |

### Technology fields

| Field | Required | Description |
| --- | --- | --- |
| `id` | yes | Permanent, unique identifier. Used in commands (`/xc research info <id>`). |
| `name` | no | Display name; falls back to `id`. |
| `tier` | no | Progression value used only for sorting/display; not enforced automatically. |
| `type` | no | One of `free`, `unlockable`, `research_required`. Defaults to `free`. |
| `research_cost` | no | Amount of `currency.research` needed to research this tech. `0` means research is disabled. |
| `purchase_cost` | no | Amount of `hfr_currency_purchase` needed to buy a license. `0` means purchase is disabled. |
| `prerequisites` | no | Array of `id` strings that must be researched/purchased first. When `sequential_research` is on, the system enforces this ordering. |
| `items` | no | Array of items this tech governs. See "Item entries" below. |
| `recipes` | no | Array of recipe identifiers this tech governs. |

### Technology types

- **`free`** — always unlocked. No research, no purchase. Use for baseline recipes/items every faction gets.
- **`unlockable`** — players (or factions) can purchase the license directly with `hfr_currency_purchase` once prerequisites are met.
- **`research_required`** — players must spend `currency.research` to research first, then purchase with `hfr_currency_purchase`.

### Item entries

Items use Forge registry IDs (`modid:itemname`). With a metadata suffix, the
entry becomes `"minecraft:wool:14"`. Object form gives per-flag control:

```json
"items": [
  "minecraft:iron_ingot",
  { "id": "minecraft:iron_pickaxe", "crafting": true, "usage": true }
]
```

- `crafting` (default `true`) — blocks crafting the item at the workbench.
- `usage` (default `true`) — blocks right-clicking/using the item in the world.
- `metadata` (default `0`) — restrict to a specific item damage value.

Unknown item IDs are logged at WARN and skipped, so the rest of the tree still
loads.

### Recipe entries

`recipes` is an array of recipe IDs from the registry. The Tech system uses
these to gate specific shaped/shapeless recipes when the gating is enabled.

## Workflow

1. Edit `technology_tree.json` to match the gameplay you want.
2. Restart the server (or run `/xc research reload`).
3. `/xc research status` — verify the technologies were loaded.
4. `/xc research gui` — open the in-game tree GUI to inspect the design.
5. Players research with `/xc research research <id>` (admins can
   `/xc research grant <player|faction> <id>`).

## Examples

A minimal "everything is free" tree:

```json
{ "technologies": [
  { "id": "tech.all", "name": "Everything", "type": "free" }
] }
```

A pure research tree (no purchase gating):

```json
{ "currency": { "research": "lab_points" }, "technologies": [
  { "id": "tech.start", "type": "free" },
  { "id": "tech.gunpowder", "type": "research_required",
    "research_cost": 100, "prerequisites": ["tech.start"] }
] }
```

## Troubleshooting

- **`/xc research status` says "No technology tree loaded."**
  The JSON is missing, malformed, or pointed at the wrong path. Check the file
  is at `config/research/technology_tree.json` (or override via
  `hfr_technology_tree` in `hfr.cfg`).
- **An item I added doesn't gate.**
  Make sure the registry ID is exact (`minecraft:iron_pickaxe`, not `Iron Pickaxe`)
  and that no other mod is overriding the entry. Check the server log for
  "Unknown item" warnings.
- **Tech never unlocks.**
  Confirm all `prerequisites` were already researched/purchased by your faction,
  the player is in a faction, and the `hfr_research_shared_by_faction` /
  `hfr_purchase_shared_by_faction` settings match your intent.