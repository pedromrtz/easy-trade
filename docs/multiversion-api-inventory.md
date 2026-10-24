# Multi-version compatibility inventory

Baseline inspected: current working tree targeting Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.163.0+26.3, Java 25. Inventory is based on common, client, GameTest and Gradle sources before multi-version implementation. Existing uncommitted branding/project-name edits were left untouched.

## Compatibility boundaries

| Area | Current use | Primary files | Porting risk / behavior to preserve |
| --- | --- | --- | --- |
| Java language/runtime | Java 25 source/bytecode; records, pattern matching, `List.of`, switch expressions, modern NIO APIs | `build.gradle`, `gradle.properties`, payload/cache/config/preview records, `TradeCyclingButton.java` | Java 8-era targets cannot load Java 25 bytecode or compile records/pattern matching. Split source/API implementation or lower language baseline; set bytecode target per game runtime. |
| Fabric initialization and identifiers | `ModInitializer`, `ClientModInitializer`, modern `Identifier` | `TradeAssistantMod.java`, `TradeAssistantClient.java` | Initialization APIs are stable in concept but identifier class/factory and registration names differ by Minecraft/Fabric generations. |
| Networking registration | `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`, typed custom payloads and `StreamCodec`/`RegistryFriendlyByteBuf` | `TradeAssistantMod.java`; `network/*Payload.java`; `LockedTradeServerNetworking.java`; `TradeCyclingServerNetworking.java`; `ClientLockedTradePreviews.java`; `ClientTradeCycling.java`; `TradeAssistantClient.java` | Current typed payload API is newer than legacy Fabric channel registration and packet-buffer APIs. Maintain request/response semantics, container/request IDs, stale response checks, optional server support, and server validation. |
| Trade-offer serialization | `MerchantOffers.CODEC` and `STREAM_CODEC` over registry-aware buffers | `TradeOfferGroup.java`, `LockedTradesPayload.java` | Codec/registry-aware buffer types are version-specific. Provide equivalent serialization for each target and test round-tripping offer costs/results, including enchantments. |
| Villager offer generation | `TradeSet`, `ResourceKey<TradeSet>`, `AbstractVillager.addOffersFromTradeSet` | `LockedTradeOfferService.java`, `AbstractVillagerTradeAccessor.java`, `AbstractVillagerTradeMixin.java` | Trade-set system and method are modern. Legacy releases use different villager trade tables/registries. Preview generation must remain deterministic and avoid consuming or mutating payment stacks. |
| Villager state and persistence | `VillagerData`, `ValueInput`/`ValueOutput`, NBT-backed locked-trade cache and profession-level invalidation | `VillagerLockedTradesMixin.java`, `LockedTradeCache.java`, `LockedTradeOfferService.java` | Persistence signatures/storage APIs changed across game versions. Preserve save/load, cache invalidation on profession/level changes, and safe behavior for old/missing data. |
| Merchant menu and cycling | `MerchantMenu`, `MerchantOffers`, `ServerPlayer`, `containerMenu`, accessor for `trader`, offer replacement and `sendMerchantOffers` | `TradeCyclingService.java`, `MerchantMenuAccessor.java`, `TradeCyclingServerNetworking.java` | Menu field/method names and signatures can move. Preserve novice/untraded checks, cooldown, payment preservation, offer refresh, and client selection reset. |
| Client trade screen | `MerchantScreen`, `Screen`, current `extractContents`/`postButtonClick` injection points, `GuiGraphicsExtractor`, `Screen` widget invoker and private scrolling fields | `MerchantScreenMixin.java`, `ScreenAccessor.java`, `TradeCyclingKeyMixin.java` | Rendering method/signature and screen lifecycle changed repeatedly; modern extraction rendering differs from older `render`/`renderBg` paths. Mixins must target real methods for each version and remain client-only. |
| Enchantment labels | `DataComponents.STORED_ENCHANTMENTS`, `ItemEnchantments`, `Enchantment.getFullname`, `ClientTooltipComponent`, `TooltipRenderUtil` | `MerchantScreenMixin.java`, `EnchantmentTradeLabelRenderer.java`, `EnchantmentLabelsToggleButton.java` | Data components are not available in older versions; older enchanted books store enchantment NBT and expose different registry/translation APIs. Keep labels client-side and tooltips intact. |
| UI widgets and input | `Button`, `Tooltip`, `StringWidget`, `EditBox`, `KeyEvent`, `KeyMappingHelper` | `TradeCyclingButton.java`, `EnchantmentLabelsToggleButton.java`, `TradeAssistantConfigScreen.java`, `TradeCyclingKeyMixin.java`, `ClientTradeCycling.java` | Widget constructors, drawing APIs, key event types, and Fabric keybinding registration vary. Preserve optional cycling visibility when server lacks support. |
| Client/server events and optional integrations | Fabric entity-use callback, client networking registration, Fabric Loader config directory, optional compile-only Mod Menu API | `TradeAssistantClient.java`, `TradeAssistantConfig.java`, `TradeAssistantModMenu.java`, `build.gradle` | Event callback return values and Mod Menu APIs differ. Keep Mod Menu optional; no config-library dependency. Config edits remain staged until Save & Exit; Cancel/Escape discard. |
| Mixins and accessors | Common villager/menu accessors plus client screen/menu mixins, `JAVA_25`, required injectors | `src/main/resources/trade-assistant.mixins.json`, `src/client/resources/trade-assistant.client.mixins.json`, `mixin/*` | Compatibility level must match target bytecode; owners, descriptors, and injection points require per-target validation. Client mixins load in integrated-server contexts, so retain explicit client-side guards. |
| Tests/build/resources | Fabric API GameTests, split Loom source sets, generated 142-locale resources, single pinned version/dependencies | `src/gametest/**`, `build.gradle`, `gradle.properties`, `fabric.mod.json`, localization bundle | GameTest APIs and test mod metadata may differ; test-only classes must remain excluded. Resource generation must continue to produce valid per-target assets and metadata. |

## Feature invariants for ports

- Enchantment labels are client-only and must not expose client classes to a dedicated server.
- Locked-trade previews are predictions sourced from the modded server; do not show client-only predictions when the server lacks support.
- Trade cycling must be rejected server-side for traded/non-novice villagers, preserve payment items, honor cooldowns, invalidate stale locked-offer data, and refresh selected offer/scroll state.
- Unmodded clients must still be able to play on servers with the mod; server-only installation must not require client installation.
- Continue using `trade-assistant` for mod ID, resource namespace, mixin package/config references, and networking identifiers.

## Target-specific port decisions

### Minecraft 1.21.9 — reuse the legacy boundary adapter, retain shared logic

- **Decision:** Preserve shared networking/codecs, trade cache and cycling rules, and client preview flow; reuse the bounded classic-villager/identifier/GUI adapter approach from 1.21.10. No full rewrite.
- **Evidence:** The initial compile reports 36 API errors centered on classes introduced after 1.21.9 (`npc.villager`, `TradeSet`, `Identifier`, and jspecify `Nullable`). Official 1.21.9 mappings confirm classic `VillagerTrades`, `ResourceLocation`, codec-backed `ValueInput`/`ValueOutput`, and `MerchantScreen.renderContents(GuiGraphics)`; these boundaries match 1.21.10.
- **Dependency correction:** Fabric API `0.134.1+1.21.9` is listed with Loader `0.16.14`, but this target matrix also installs Mod Menu `16.0.1`, whose minimum is Loader `0.17.2`. Use Loader `0.17.2` for the smoke target.
- **Validation:** Overlay implemented; common/client compilation, full build, runnable artifact validation, all 5 GameTests, and Smoke launch passed. Client initialized the mod and sound engine without startup mixin errors; no saved world opened, and manual merchant interaction remains unverified.

### Minecraft 1.21.8 — reuse the classic-villager adapter, retain shared logic

- **Decision:** Keep shared gameplay/network behavior and reuse bounded adapters for classic villager trades, identifiers, merchant UI, and the older client key/config APIs. A version-specific rewrite is not justified.
- **Evidence:** The root compile reports the same 36 errors at the newer `npc.villager`/`TradeSet`, `Identifier`, and `Nullable` boundaries. Official 1.21.8 mappings expose the classic `net.minecraft.world.entity.npc.Villager` family and `GuiGraphics` merchant UI. The first adapted client compile found additional differences (`Screen.keyPressed(int,int,int)`, string key categories, `StringWidget` without `setMaxWidth`, and `Minecraft.setScreen`); small target-specific client adapters resolved these.
- **Dependencies:** Keep configured Fabric Loader `0.16.14`, Fabric API `0.136.1+1.21.8`, and Mod Menu `15.0.2` unless target build or Smoke resolution reveals a conflict.
- **Validation:** Common/client compilation, full build, runnable artifact validation, all 5 GameTests, and staging passed. Prism loaded Minecraft 1.21.8 with Loader 0.16.14, initialized Trade Assistant, started the sound engine, and showed no startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.7 — reuse the 1.21.8 legacy adapter set

- **Decision:** Preserve shared gameplay/network behavior and use bounded compatibility adapters rather than a full rewrite.
- **Evidence:** The initial root-source compile reports 36 errors at the same post-1.21.7 villager/`TradeSet`, identifier, and nullability API boundaries. Official 1.21.7 mappings confirm the classic villager/UI and string-based keybinding API shape used by the 1.21.8 adapters; target compilation will validate exact signatures.
- **Dependencies:** Retain configured Fabric Loader `0.16.14`, Fabric API `0.129.0+1.21.7`, and Mod Menu `15.0.2` unless the target build or Smoke instance identifies a conflict.
- **Validation:** Reused the 1.21.8 overlay structure; common/client compilation and full build pass, all 5 GameTests pass, runnable JAR verified/staged. Prism loaded Minecraft 1.21.7 with Loader 0.16.14, initialized Trade Assistant and sound engine, without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.6 — reuse 1.21.8 legacy adapters

- **Decision:** Retain shared gameplay/network logic and port only version-specific classic villager, identifier, GUI, and key/config API edges.
- **Evidence:** The initial root compile shows the same 36 newer villager/`TradeSet`, identifier, and nullability incompatibilities. Official 1.21.6 mappings confirm classic `Villager` and string-category `KeyMapping`, matching the 1.21.8 adapter design. Compile will verify the target's exact method signatures.
- **Dependencies:** Preserve configured Loader `0.16.14`, Fabric API `0.128.2+1.21.6`, and Mod Menu `15.0.2` unless target validation finds a conflict.
- **Validation:** Overlay compiled for common/client; full build, runnable artifact validation, all 5 GameTests, and staging passed. Prism loaded Minecraft 1.21.6 with Loader 0.16.14, initialized Trade Assistant, and started sound without mixin errors. A Mojang auth user-properties fetch warning appeared after initialization and did not block reaching title. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.5 — assess the component-era API transition

- **Decision:** Preserve the shared cycling/cache/network semantics if API changes remain localized; choose a version-specific rewrite only if trade generation or data components require broader changes.
- **Evidence:** Initial root compile reports 40 failures: the same classic-villager/`TradeSet`/identifier/nullability issues plus `ValueInput`/`ValueOutput`, absent in 1.21.5. Official mappings confirm the classic `Villager` API is still present. The bounded 1.21.8 adapter strategy applies to trade generation and client input/UI; persistence uses a target-specific `CompoundTag`/`NbtOps` adapter to preserve the serialized cache. A first overlay compile found older `PoseStack` and tooltip text/background rendering signatures, so those label visuals also need a small legacy GUI adapter. Keep data-component enchantment and typed payload code shared unless target compilation proves otherwise.
- **Dependencies:** Configured Loader `0.16.14`, Fabric API `0.128.2+1.21.5`, and Mod Menu `14.0.0`; reassess requirements during build/staging.
- **Validation:** Common/client compilation, full build, runnable artifact verification, all 5 GameTests, and staging pass. A remapper warning caught an invalid `renderContents` hook; switched to `render`, rebuilt without warnings, and Prism loaded the mod with the sound engine started and no mixin errors. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.4 — assess older persistence, merchant UI, and trade APIs

- **Decision:** Retain shared cycling/cache/network behavior and adapt only demonstrated API boundaries. The 1.21.5 NBT adapter may be reusable, but verify `CompoundTag`, villager trade tables, data components, and merchant render hooks at this target before reusing it.
- **Evidence:** The root compile found the same 40 newer-API failures as 1.21.5. Official mappings confirm `VillagerData.getProfession()/getLevel()`, `VillagerTrades.TRADES` keyed directly by profession, classic `CompoundTag` persistence, and a declared `MerchantScreen.render(GuiGraphics,...)`. Adapted registry identity and trade-table access in the common/server sources and villager state lookup client-side, reusing the 1.21.5 NBT and rendering adapters.
- **Dependencies:** Configured Loader `0.16.14`, Fabric API `0.119.4+1.21.4`, Mod Menu `13.0.3`; reassess only if target validation reports conflicts.
- **Validation:** Common/client compilation, full build, runnable JAR verification, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.21.4 with Loader 0.16.14, initialized Trade Assistant, and started the sound engine without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.3 — reuse the 1.21.4 legacy adapter set

- **Decision:** Preserve shared cache/cycling/network behavior and reuse bounded adapters for classic villager trade generation, NBT persistence, identifiers, and GUI/key APIs; no full rewrite indicated by the initial compile.
- **Evidence:** Root compilation reports the same 40 errors as 1.21.4/1.21.5, at post-1.21.3 villager/trade-set namespaces, `Identifier`, `ValueInput`/`ValueOutput`, and nullability. Reused the 1.21.4 overlay; compilation found `Villager.stillValid(player)` absent, so the service now validates the active menu with `MerchantMenu.stillValid(player)`. Common and client compilation pass.
- **Dependencies:** Keep configured Loader `0.16.14`, Fabric API `0.114.1+1.21.3`, and Mod Menu `12.0.1` unless target build or Smoke resolution reveals conflict.
- **Validation:** Common/client compilation, full build, runnable JAR validation, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.21.3 with Loader 0.16.14, initialized Trade Assistant, and started sound without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.2 — reuse the classic-villager/NBT adapter set

- **Decision:** Keep shared cycling/cache/network behavior; reuse bounded 1.21.3 adapters for classic villager/trade APIs, `CompoundTag` persistence, identifiers, and client GUI/key APIs rather than a full rewrite.
- **Evidence:** Baseline target compilation reports the same 40 post-1.21.2 API errors as 1.21.3. Mapping lookup for 1.21.2 is unavailable from the current mapping index, so verify exact APIs through the target compiler and full build.
- **Dependencies:** Retain configured Loader `0.16.14`, Fabric API `0.106.1+1.21.2`, and Mod Menu `12.0.1` unless build or Smoke resolution indicates a conflict.
- **Validation:** Common/client compilation, full build, runnable JAR validation, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.21.2 with Loader 0.16.14, initialized Trade Assistant, and started sound without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.1 — reuse legacy adapters, adapt container ID codecs

- **Decision:** Preserve shared network payload meanings and feature behavior; reuse the classic villager/NBT/client adapters and use the older integer codec for container IDs.
- **Evidence:** Baseline compile shows the same classic API differences as 1.21.2 and reveals `ByteBufCodecs.CONTAINER_ID` is unavailable. Container IDs use a target-scoped `VAR_INT` codec, preserving their integer wire encoding. Target compile also identified older tooltip rendering signatures; isolated a renderer adapter using no-argument `ClientTooltipComponent.getHeight()` and the six-argument background method. Common/client compilation passes.
- **Dependencies:** Keep configured Loader `0.16.14`, Fabric API `0.116.17+1.21.1`, and Mod Menu `11.0.3` unless validation reports conflicts.
- **Validation:** Common/client compilation, full build, runnable JAR verification, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.21.1 with Loader 0.16.14, initialized Trade Assistant, and started sound without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21 — reuse the 1.21.1 adapter set

- **Decision:** Keep shared gameplay/network semantics; reuse classic villager, NBT, legacy UI, and VAR_INT container-ID adapters rather than rewriting feature logic.
- **Evidence:** Root compilation reports 44 errors matching the 1.21.1 compatibility boundaries. The 1.21.1 overlay is the compatibility baseline; this target's compiler will establish exact API parity.
- **Dependencies:** Retain Loader `0.16.14`, Fabric API `0.102.0+1.21`, and Mod Menu `11.0.5` unless target validation identifies a conflict.
- **Validation:** Common/client compilation, full build, runnable JAR verification, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.21 with Loader 0.16.14, initialized Trade Assistant, and started sound without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.20.6 — reuse classic trade/NBT adapters

- **Decision:** Keep shared trade/cache/network semantics and adapt the classic villager, identifier, NBT, and client UI boundaries. The target retains data components/typed networking, so a full rewrite is not warranted.
- **Evidence:** Baseline compile reports 44 errors at newer villager/trade, identifier, persistence, and container-ID codec APIs. Reused bounded 1.21.1 adapters. Target common/client compilation then identified `ResourceLocation(namespace,path)` as the available identifier constructor and instance `Enchantment.getFullname(level)` for holder-based data-component entries; these remain target-scoped. Shared networking payload semantics, cache behavior, and cycling logic are retained.
- **Dependencies:** Retain Loader `0.16.14`, Fabric API `0.100.8+1.20.6`, and Mod Menu `10.0.0` unless validation finds a conflict.
- **Validation:** Common/client compilation, full build, runnable JAR verification, all 4 GameTests, and staging passed. Prism loaded Minecraft 1.20.6 with Loader 0.16.14, initialized Trade Assistant, and started sound without startup mixin/ERROR messages. No saved world opened; manual merchant interaction remains unverified.

### Minecraft 1.21.10 — retain shared logic, adapt legacy API edges

- **Decision:** Keep the common networking payload model/codecs, trade cache representation, cycling validation, client preview state, and feature flows. Use narrow version-specific source overlays for villager trade generation, identifier types, old `VillagerData` accessors, and the classic GUI widget/render signatures. Do not rewrite the mod.
- **Evidence:** The first target compile produced 36 errors, predominantly from the moved `npc.villager` package, renamed `Identifier` type, and the newer `TradeSet` API not existing in 1.21.10. The targeted overlay compiles against the 1.21.10 official mappings, which expose `VillagerTrades.TRADES`, `AbstractVillager.addOffersFromItemListings`, `VillagerData.profession()/level()`, `ResourceLocation`, and `MerchantScreen.renderContents(GuiGraphics)`. Typed payload codecs and `ValueInput`/`ValueOutput` compile unchanged.
- **Port scope:** `src/versions/1.21.10/main/java` adapts the villager service/mixins, identifier users, and server networking; the client overlay adapts villager lookup, `Button` rendering, and `MerchantScreen` mixin targets; the GameTest overlay uses target-era packages/identifier types. Shared gameplay rules and wire-data semantics remain unchanged.
- **Dependency correction:** Fabric API `0.138.4+1.21.10` requires Loader `>=0.17.0`, and the installed Mod Menu `16.0.1` requires `>=0.17.2`. The target matrix and `MC 1.21.10 Fabric Smoke` use Loader `0.17.2`, satisfying both; the previous `0.16.14` requirement was incompatible.
- **Validation:** Common/client source compilation passed; full `build verifyTargetArtifact` passed, all 5 GameTests passed, and the runnable JAR plus matching Fabric API were staged in the 1.21.10 Smoke instance with matching SHA-256 hashes. Prism launch reached the title screen, initialized Trade Assistant, started the sound engine, and reported no startup mixin errors. No world was opened; manual merchant interaction remains unverified.

### Minecraft 1.21.11 — port shared behavior, adapt API boundaries

- **Decision:** Preserve the shared feature/business logic and use version-specific adapters for trade generation, payload registry selection, keybinding registration, and GUI rendering. Do not rewrite the complete mod for this target.
- **Evidence:** The initial target compile reported 13 errors concentrated in the trade-generation and typed-payload registration APIs. After adapting those, a compile exposed additional expected UI-era differences: extracted rendering (`GuiGraphicsExtractor`) and the newer keymapping helper are absent in 1.21.11. The 1.21.11 client uses `GuiGraphics` and the `KeyBindingHelper` registration API.
- **Rationale:** The server cache/cycling rules, request IDs, packet data, client preview state, settings, and feature flows remain usable. Version adapters preserve those invariants and keep the API-specific changes at their boundaries. A complete rewrite would duplicate stable logic and increase feature-drift risk. The only visible UI concession is text glyphs in place of custom extracted button artwork.
- **Port scope:** `src/versions/1.21.11/main/java` replaces the trade-set service/accessor/mixin with `VillagerTrades` and `addOffersFromItemListings`; generated `TargetPayloadRegistration` selects `playC2S`/`playS2C`; `src/versions/1.21.11/client/java` supplies the legacy keybinding/rendering adaptations. Common packet definitions and the server/client feature rules remain shared. The target's mixin compatibility level is generated as Java 21.
- **Dependency correction:** Fabric API `0.141.6+1.21.11` requires Fabric Loader `>=0.17.3`; the matrix, dependency inventory, artifact metadata, and dedicated smoke instance were updated from `0.16.14` to `0.17.3`.
- **Validation:** `gradlew.bat build verifyTargetArtifact -PmcTarget=1.21.11` passed; all 5 GameTests passed; artifact metadata and Java 21 bytecode validated; the runnable JAR and matching Fabric API were staged only in `MC 1.21.11 Fabric Smoke`; the client initialized and started its sound engine without mixin-application errors.

## Build baseline

- `gradlew.bat build` completed successfully on 2026-10-22 with Minecraft 26.3, Loader 0.19.5, Fabric API 0.163.0+26.3 and Java 25.
- All 5 server GameTests passed.
- Current runnable baseline artifact is `build/libs/trade-assistant-1.0.0.jar`; `build/libs/trade-assistant-1.0.0.jar` is an older artifact and must not be mistaken for the build just verified.
