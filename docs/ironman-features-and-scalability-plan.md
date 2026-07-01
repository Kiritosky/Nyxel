# Nyxel — Ironman Features & Scalability Plan

Nyxel is a client-side Hypixel SkyBlock QoL mod. This plan (a) adds features that
matter to **Ironman/Stranded** players and (b) hardens the mod's architecture so
it scales to dozens of features without `NyxelClient` and `NyxelConfig` becoming
bottlenecks.

## Why Ironman is the right focus

Ironman profiles **cannot** use the Bazaar, Auction House, trading, or receive
coins. So price/flip features (`PriceTooltipFeature`, `auction-flips` stub) are
near-useless to them. Ironman value comes from **self-sufficiency**: crafting,
collections, minions, NPC shops, and resource routing. The Garden
`MutationHelperFeature` is already the flagship — lean into that identity.

---

## Part 1 — Ironman-focused features

Grouped by effort. Each is a `Feature` (some also `HudElement`).

### Foundation (do first — everything below reads from it)
1. **Ironman profile detection** (`core/ProfileContext`). Detect Ironman/Stranded
   from the tab-list / profile widget icon (♲) or the Hypixel profiles API, and
   expose `isIronman()`. Gate/relabel features on it (e.g. hide Bazaar prices,
   surface NPC-sell instead). This is the linchpin — see Part 2 §5.

### High value, low effort
2. **NPC price tooltips** — Ironman analogue of `PriceTooltipFeature`: show NPC
   buy/sell value from a bundled data table instead of Bazaar. Reuses the tooltip
   hook; no network.
3. **Collection progress HUD** — track collection gains from chat/action-bar
   (`+X Item (n/threshold)`) and show progress toward the next recipe unlock.
   Recipes are the Ironman economy, so "next unlock" is the key metric.
4. **Minion output estimator** — given placed minion tier + fuel + storage,
   estimate items/hour and time-to-full. Pure calc, data-driven table. Ironman
   income is minions.

### Medium effort
5. **Crafting tree planner** (`feature/crafting/`) — "to craft X, you need N of A,
   M of B; A comes from collection/minion/mob Y." A recursive resolver over a
   bundled recipe graph, rendered in a screen like `MutationPlannerScreen`. Mirror
   the existing `garden/engine` solver pattern (`PlacementSolver`/`FusionPlanner`).
6. **"Where do I get X" locator** — item → source (mob drop / collection / NPC /
   fishing) + best zone. Data-driven; pairs with #5.
7. **Fairy soul & accessory (Magical Power) tracker** — HUD showing collected vs
   total and MP from bundled tuning data. Progression, not economy → Ironman-safe.

### Larger
8. **Jacob's Contest / Foraging route helpers** — Ironman-legal medal/XP income;
   extends the Garden module.
9. **Essence & dungeon-loot self-sufficiency tracker** — essence per run vs upgrade
   cost. Extends the `dungeons-*` stubs.

---

## Part 2 — Make the mod scalable

Concrete refactors, cheapest-first. Each is independently shippable.

### 1. Auto-register features instead of the hardcoded list
`NyxelClient` hand-wires every feature (lines 45–72) and will grow unbounded.
Introduce a `FeatureModule` per category (`GardenModule`, `FishingModule`, …) each
returning its `List<Feature>`; `NyxelClient` just iterates the modules. Keeps the
entrypoint flat and makes categories self-contained. (Avoid classpath annotation
scanning — slow and brittle under Fabric remapping.)

### 2. Per-feature config schema (kill the god-config)
`NyxelConfig` hardcodes per-feature knobs (e.g. `economy.priceCacheSeconds`).
That doesn't scale. Add `default Map<String,Option> options()` to `Feature`, let
`FeatureManager` persist them under a namespaced key, and have
`NyxelConfigScreen` render them generically. New feature ⇒ zero core edits.

### 3. Index hooks; don't iterate all features per event
`FeatureManager` loops all features for every tick/hud/actionbar. Add richer hooks
(`onChatMessage`, `onItemTooltip`, `onSlotRender`) via a capability interface and
bucket features by the hooks they implement, so dispatch touches only relevant
ones. Keep the try/catch-per-feature isolation that already exists.

### 4. Shared, rate-limited API/data layer
`BazaarClient` spins its own thread. As API features grow this fragments and can
trip Hypixel rate limits. Extract a single `NyxelExecutor` (shared daemon pool) +
a rate-limited `HttpJson` gateway, and a `DataRepository<T>` base for the bundled
JSON tables (recipes, NPC prices, collections) mirroring `MutationRepository`.

### 5. Promote SkyBlock context into a real state service
`SkyblockState` only parses the sidebar. Add profile type (Ironman detection, §Part
1.1), island/region, and a small event bus so features subscribe to
`onZoneChange` / `onProfileChange` instead of polling `zone()` each tick.

### 6. Tests & data validation
Extend the existing test pattern (`FusionPlannerTest`, `PlacementSolverTest`) to
the crafting resolver and to a schema-validation test that fails the build if a
bundled data file is malformed. Add a datagen check so recipe/price tables stay
in sync.

---

## Suggested order

1. Part 2 §1–§2 (registration + config schema) — unblocks cheap feature adds.
2. Part 1 §1 Ironman detection + Part 2 §5 state service.
3. Part 1 §2–§4 (NPC tooltips, collection HUD, minion estimator) — fast wins.
4. Part 2 §3–§4 (hook indexing, shared API/data layer) — under load.
5. Part 1 §5–§7 crafting tree + locator + trackers.
6. Larger §8–§9 as the Ironman flagship expands.
