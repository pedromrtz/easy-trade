# Trade Assistant — Multi-version Fabric Compatibility Plan

> **Single source of truth for autonomous execution.** Complete and verify each checklist item before marking it done. Add newly discovered required work here before continuing.

## Assumptions and Constraints

- Target every Minecraft **release** represented by the agreed range, not snapshots: 26.3, 26.2, 26.1, 1.21 through 1.21.11, 1.20 through 1.20.6, 1.19 through 1.19.4, 1.18 through 1.18.2, 1.17 through 1.17.1, 1.16 through 1.16.5, 1.15 through 1.15.2, and 1.14 through 1.14.4 (46 targets total).
- Keep the loader Fabric-only. Preserve the mod ID `trade-assistant` and the current feature contract unless a legacy game version makes a feature technically impossible; document any such limitation before publishing that target.
- Work in descending Minecraft version order, starting with the current 26.3 baseline and proceeding to 1.14. Do not claim mod compatibility based only on a vanilla/Fabric launch.
- Produce a distinct, correctly version-constrained JAR for each Minecraft target. Stage each JAR in its matching Prism `MC <version> Fabric Smoke` instance. Add a Fabric API JAR only when that target's mod metadata/runtime dependencies require it, and use an API build compatible with that Minecraft release.
- The user's manual playtesting is deferred until artifacts are staged. Record the target and artifact path so the user can test versions later.
- Prism smoke instances already exist for all 46 targets and were verified to reach client initialization with Fabric Loader. Their current test worlds are user test data and must not be overwritten or deleted.
- Java runtime families used for builds/tests: Java 8 for 1.14–1.16, Java 17 for 1.17–1.20.4 (Java 17 is used for 1.17 because that is the available compatible runtime), Java 21 for 1.20.5–1.21.11, and Java 25 for 26.x. Verify per-version requirements against Mojang metadata before configuring a target.
- The current repository is configured for Minecraft 26.3, Loader 0.19.5, Fabric API 0.163.0+26.3, and Java 25. Recent payload networking, component-based item data, trade-set generation, client GUI rendering, and mixin targets are known compatibility boundaries; older releases may need dedicated implementations.
- Keep local `AGENTS.md` uncommitted. After every code change, use `gradlew.bat build` and follow its required distribution-copy steps. Do not replace unrelated files in Prism instances.
- This is a Fabric mod, not a hosted service. Database, web API, secrets, cloud deployment, alerting, and backup infrastructure are not applicable. Relevant operational requirements are reproducible builds, local artifact staging, and retained test logs.

## System Architecture

- Keep feature behavior conceptually shared: enchantment labels, server-provided locked-trade previews, and server-validated trade cycling.
- Isolate Minecraft/Fabric API differences behind version-targeted source and configuration (including mixins, networking, UI rendering, trade generation, and item enchantment access). Share adapters only after compilation and runtime tests prove the APIs are compatible.
- Configure builds so each target resolves its own Minecraft, Loom/mappings, Fabric Loader, Fabric API, Java release, metadata and mixin configuration. A target artifact must not include classes/configurations that reference unavailable APIs on that target.
- Per target flow: compile common and client code -> run available automated tests -> build/remap JAR -> inspect metadata/dependencies -> stage JAR and required Fabric API in the matching Prism instance -> launch and inspect logs -> record result and artifact path.

## Repository and Project Structure

- Preserve the existing `src/main/java` common/server and `src/client/java` client split as the baseline.
- Add clearly named version-targeted source/configuration directories or Gradle subprojects only as selected by the architecture spike; keep test-only code out of distributable JARs.
- Keep target artifacts under a version-distinguishable build output path/name and avoid committing generated JARs or local Prism data.
- Keep the root project and existing release build usable for 26.3 throughout migration.

## Technology Stack

- Java, Gradle wrapper, Fabric Loom, Fabric Loader, Fabric API, Minecraft mappings, and Mixin, with versions selected per target.
- Prism Launcher instances are the client launch/smoke-test environment. Mojang version metadata and Fabric Maven/metadata are the sources of target Java, loader, and Fabric API compatibility information.
- Use existing Fabric GameTests where supported; add version-specific automated tests or a documented equivalent when APIs differ.

## Implementation Checklist

### Compatibility foundation and build pipeline

- [x] Capture a baseline 26.3 build/test result and preserve the currently working 26.3 source, metadata, and artifact before changing build structure. `gradlew.bat build` succeeded on 2026-10-22; all 5 GameTests passed. The user's current working tree changes (branding/project name) were preserved. Baseline runnable artifact: `build/libs/trade-assistant-1.0.0.jar`.
- [x] Inventory every direct Minecraft/Fabric/Mixin/Mod Menu API usage in common and client sources, classify by compatibility boundary, and record affected files and feature behavior in `docs/multiversion-api-inventory.md`.
- [x] Verify official Minecraft release list, Java requirement, Fabric Loader availability, and Fabric API availability for all 46 targets; record exact dependency coordinates and any unsupported target in `docs/fabric-target-dependencies.csv`. Every target has a matching API release; the 1.17 targets require Java 16 per Mojang metadata and are smoke-tested with the available Java 17 runtime.
- [x] Design and implement a reproducible Gradle target-selection/build mechanism in `build.gradle` and `gradle/minecraft-targets.json`. `-PmcTarget=<version>` selects target Minecraft/Loader/Fabric API/Mod Menu/Java, resource metadata, optional Mod Menu source, per-target source roots, and version-distinct archive names. Loom's non-remapping plugin is selected for 26.1+; the remapping plugin and official Mojang mappings are selected for 1.21.11 and older. Verified default 26.3 `build` and 1.21.11 `processResources`; full 1.21.11 compile awaits its version-specific source port.
- [x] Add validation that each produced JAR has mod ID `trade-assistant`, exact Minecraft compatibility metadata, correct Loader/API requirements, correct client/common mixin declarations, and a version-distinguishable filename. `verifyTargetArtifact` checks metadata, mixin configs/levels, required entrypoints/classes, Java bytecode, test-class exclusion, and target archive naming.
- [x] Add a repeatable build-and-stage script at `scripts/stage-target.ps1`. It selects a target, runs Gradle build and artifact validation, preserves each runnable JAR under ignored `target-artifacts/<version>/`, stages only in the matching existing Prism smoke instance, and downloads/replaces Fabric API only when the JAR declares it.
- [x] Add `docs/target-build-results.csv` recording target Java/Loader/API, build/validation/test/launch statuses, artifact and staged paths. All 46 targets are listed; only validated targets are marked complete.
- [x] Verify the 26.3 target can be built, GameTested (all 5 pass), metadata-checked, staged in `MC 26.3 Fabric Smoke`, and launched with the staged mod. It initialized and reached sound-engine startup on 2026-10-22.

### Version targets — newest to oldest

For each target below, complete both listed tasks before moving to the next older target. Each staged artifact is for later user playtesting; report any failed build, automated test, dependency resolution, or launch instead of marking it successful.

#### Minecraft 26.x

- [x] Implement/adapt and compile the 26.3 target, preserving all supported features and current server/client boundaries. The existing 26.3 implementation remains the baseline.
- [x] Test, package, validate metadata, stage the 26.3 JAR and required Fabric API in `MC 26.3 Fabric Smoke`, launch, and record its result/artifact in `docs/target-build-results.csv`.
- [x] Implement/adapt and compile the 26.2 target. Shared refresh logic now calls the merchant container's `updateSellItem()` through `MerchantMenuAccessor`, which is present in both 26.2 and 26.3; `MerchantMenu.updateSellItem()` itself was removed in 26.2.
- [x] Test, package, validate metadata, stage the 26.2 JAR and required Fabric API in `MC 26.2 Fabric Smoke`, launch, and record its result/artifact in `docs/target-build-results.csv`. All 5 GameTests passed and the staged client initialized successfully on 2026-10-22.
- [x] Implement/adapt and compile the 26.1 target. Added target-specific screen access because `Minecraft.gui.screen()` is absent in 26.1; standardized the GameTest spawn to `EntityType.VILLAGER`, which is shared across 26.x.
- [x] Test, package, validate metadata, stage the 26.1 JAR and required Fabric API in `MC 26.1 Fabric Smoke`, launch, and record its result/artifact in `docs/target-build-results.csv`. All 5 GameTests passed and the staged client initialized successfully on 2026-10-22.

#### Minecraft 1.21.x

- [x] Implement/adapt and compile the 1.21.11 target. **Compatibility decision (reassessed 2026-10-22): preserve shared feature/business logic and add bounded version-specific adapters; do not rewrite the whole mod.** Compilation confirmed legacy villager trade tables (`VillagerTrades`) replace `TradeSet`/`VillagerProfession.getTrades`; networking retains typed payloads but uses `PayloadTypeRegistry.playC2S/playS2C`; and the client needs the 1.21 `GuiGraphics` rendering path plus legacy keybinding registration. The 13 initial compile errors remained concentrated in these boundaries. The server trade cache, request/response semantics, cycling validation, preview/client state, and control visuals are shared; the 1.21.11 controls reproduce the pixel-art cycle glyph and enchanted-book/status indicator used in 26.x using the legacy render hooks. Fabric API 0.141.6 requires Loader 0.17.3+, so both target metadata and the smoke instance use 0.17.3. Rationale and affected files are in `docs/multiversion-api-inventory.md`.
- [x] Test, package, validate metadata, stage the 1.21.11 JAR and required Fabric API in `MC 1.21.11 Fabric Smoke`, launch, and record its result/artifact. All 5 GameTests passed. A later user playtest exposed a lazy-loaded `MerchantScreenMixin` injection failure when opening a villager: `MerchantScreen` inherits `render`, so that method is not a valid declared injection target. Changed the 1.21.11 overlay injections to target Mojang-mapped `renderContents` (intermediary `method_71085`), rebuilt and verified the runnable JAR, and re-staged it; the user confirmed merchant opening/trading works. The control visuals were then ported to match the 26.x pixel-art cycle glyph and enchanted-book/status indicator using the legacy render hooks. The existing Prism `test-world` was not reopened or modified by these validation steps.
- [x] Implement/adapt and compile the 1.21.10 target. **Compatibility decision (reassessed 2026-10-22): preserve the shared networking, codecs, cache/cycling rules, and UI behavior; provide bounded 1.21.10 source adapters rather than rewriting common business logic.** The initial compile's 36 errors reduce to version-specific boundaries: old `npc.*` villager package, `ResourceLocation`, classic `VillagerTrades.TRADES`/`addOffersFromItemListings`, old `VillagerData` accessors, and classic `Button`/`GuiGraphics` UI; 1.21.10 still supports typed payload codecs and `ValueInput`/`ValueOutput`, so these remain shared. Adapted trade generation/cache mixins, identifier-bearing mod/payload types, villager client preview, button/screen rendering, and GameTest imports under `src/versions/1.21.10`; the common client/server state logic remains shared. Smoke launch showed Fabric API needs Loader >=0.17.0 and Mod Menu 16.0.1 needs >=0.17.2, so both the target matrix and Smoke instance use 0.17.2.
- [x] Test, package, validate metadata, stage the 1.21.10 JAR and required Fabric API in `MC 1.21.10 Fabric Smoke`, launch, and record its result/artifact. The original full build and Prism smoke launch passed; the user confirmed merchant-screen functionality but reported button backgrounds on refresh and label-toggle controls. Removed their inherited button background rendering. The rebuild exposed that source filtering excluded cycle payloads without 1.21.10-specific replacements; added the two 1.21.10 payload definitions using the target-compatible VAR_INT codec. Rebuilt and validated the runnable JAR, passed all 5 GameTests under Loader 0.17.2, and re-staged the matching JAR/Fabric API. Did not relaunch Prism per the user's instruction; awaiting their visual confirmation. The existing `test-world` was not opened.
- [x] Implement/adapt and compile the 1.21.9 target. Compatibility assessment: the initial root-source compile reported 36 errors matching the classic villager/identifier API boundary already isolated in 1.21.10. Official mappings confirmed classic `VillagerTrades`, `ResourceLocation`, codec-backed `ValueInput`/`ValueOutput`, and the legacy GUI path. Reused the bounded 1.21.10 API adapters while retaining shared gameplay/network behavior. Mod Menu 16.0.1 requires Loader >=0.17.2, so the target and Smoke instance use 0.17.2.
- [x] Test, package, validate metadata, stage the 1.21.9 JAR and required Fabric API in `MC 1.21.9 Fabric Smoke`, launch, and record its result/artifact. Original full build/JAR verification and smoke launch passed. The user reported backgrounds on the refresh and label-toggle buttons; removed inherited button background rendering. The rebuild exposed missing 1.21.9 cycle payload definitions due target source filtering; added target-compatible payloads with VAR_INT codecs. Rebuilt and validated the runnable JAR, all 5 GameTests passed, and the matching JAR/Fabric API were re-staged. Did not relaunch Prism per the user's instruction; awaiting visual confirmation. No saved world was opened.
- [x] Implement/adapt and compile the 1.21.8 target. Compatibility assessment: the root-source compile showed the same 36 errors concentrated at `npc.villager`/`TradeSet`, `Identifier`, and `Nullable`; mappings confirmed classic villager packages and `GuiGraphics`. Reused the bounded 1.21.9 server/GUI adapters and shared gameplay/network logic. Target compilation then exposed client-only key input/keybinding and settings-screen API changes; added narrow adapters for those, preserving staged config semantics. Common and client compilation now pass. Existing Loader 0.16.14, Fabric API 0.136.1+1.21.8, and Mod Menu 15.0.2 remain configured.
- [x] Test, package, validate metadata, stage the 1.21.8 JAR and required Fabric API in `MC 1.21.8 Fabric Smoke`, launch, and record its result/artifact. Initial build/JAR verification and title-screen startup passed, but the user reported a crash when a key was pressed in-world: `MerchantScreenMixin` targeted `renderContents`, which does not exist in the 1.21.8 `MerchantScreen` (the log showed the failed remapped target `method_71085`). Changed the two screen injections to the target's declared `render` method and removed inherited backgrounds from both controls. The rebuild exposed the same omitted cycle-payload sources; added 1.21.8 payload definitions with VAR_INT codecs. Rebuilt and validated the runnable JAR, all 5 GameTests passed, and the JAR/Fabric API were re-staged. Did not launch Prism or open the user's world; awaiting user verification of merchant interaction and button appearance.
- [x] Implement/adapt and compile the 1.21.7 target. Compatibility assessment found the same legacy villager/trade/identifier/annotation boundaries as 1.21.8. Official mappings confirmed its string-based keybinding categories, legacy `GuiGraphics` merchant UI, and keyboard callback shape; reused bounded 1.21.8 adapters and shared business logic. Common and client compilation pass. Keep configured Loader 0.16.14 and Fabric API 0.129.0+1.21.7 unless staging reveals a dependency conflict.
- [x] Test, package, validate metadata, stage the 1.21.7 JAR and required Fabric API in `MC 1.21.7 Fabric Smoke`, launch, and record its result/artifact. Initial build/JAR verification and title-screen startup passed, but the user reported the same lazy `MerchantScreenMixin` crash as 1.21.8: the remapped `renderContents` target (`method_71085`) is absent in this version, which declares `render`. Changed both injections to `render`, removed inherited control backgrounds, and added 1.21.7-specific cycling payloads excluded by source filtering. Rebuilt and validated the runnable JAR, all 5 GameTests passed, and re-staged the matching JAR/Fabric API. Did not relaunch Prism or open the user's world; awaiting user verification of interaction and button appearance.
- [x] Implement/adapt and compile the 1.21.6 target. Compatibility assessment: root compilation reports the same 36 newer API failures as 1.21.7/1.21.8. Official mappings confirm classic villagers and the prior string-category keybinding API; reused bounded 1.21.8 adapters, preserving shared behavior. Common and client compilation pass. Keep Loader 0.16.14, Fabric API 0.128.2+1.21.6, and Mod Menu 15.0.2 unless validation identifies a conflict.
- [x] Test, package, validate metadata, stage the 1.21.6 JAR and required Fabric API in `MC 1.21.6 Fabric Smoke`, launch, and record its result/artifact. Initial build/JAR verification and title startup passed, but the user reported the same `MerchantScreenMixin` crash as 1.21.7/.8: `renderContents` remapped to `method_71085` is absent here; `MerchantScreen` declares `render`. Changed the two injections to `render`, removed inherited button backgrounds, and added target-specific cycle payloads omitted by source filtering. Rebuilt and validated the runnable JAR, all 5 GameTests passed, and re-staged the matching JAR/Fabric API. Did not relaunch Prism or open the user's world; awaiting verification of interaction and button appearance.
- [x] Implement/adapt and compile the 1.21.5 target. Compatibility assessment found 40 errors: established legacy villager/trade/identifier boundaries plus `ValueInput`/`ValueOutput`, introduced after this target. Kept shared trade/cache/network semantics and added an NBT persistence adapter using `CompoundTag`/`NbtOps`; adapted the older pose stack and tooltip rendering APIs for labels, reusing the client input/settings adapters. Common and client compilation now pass. Keep configured Loader 0.16.14, Fabric API 0.128.2+1.21.5, and Mod Menu 14.0.0 pending validation.
- [x] Test, package, validate metadata, stage the 1.21.5 JAR and required Fabric API in `MC 1.21.5 Fabric Smoke`, launch, and record its result/artifact. Original build, artifact verification, and smoke launch passed; the `MerchantScreen.renderContents` warning had already been fixed by targeting declared `render`. User feedback prompted removal of inherited control backgrounds and adding excluded 1.21.5 cycling payloads with VAR_INT codecs. Restored the requested original label position (`itemY + 14`) and retained the villager save fix using registry-aware `RegistryOps` (covered by a GameTest). Following the user's clarification that labels must overlay the UI rather than move, matched vanilla tooltip rendering: label panel at z=400 from the base pose, then label text with pose translated to z=400. Latest build/artifact validation passed and all 6 GameTests passed. A retry succeeded in staging the refreshed JAR/Fabric API after the user authorized closing the target process if needed; that instance's Java process was terminated. Did not launch Prism or open the user's world.
- [x] Implement/adapt and compile the 1.21.4 target. API assessment confirmed classic `Villager`, `VillagerData.getProfession()/getLevel()`, profession-keyed `VillagerTrades.TRADES`, CompoundTag persistence, and declared `MerchantScreen.render(GuiGraphics,...)`. Reused the 1.21.5 NBT/cache and legacy UI adapters, with versioned common/client adjustments for the older villager data/registry APIs. Shared trade/network behavior remains unchanged. Common/client compilation passes. Keep configured Loader 0.16.14, Fabric API 0.119.4+1.21.4, and Mod Menu 13.0.3 unless validation finds a conflict.
- [x] Test, package, validate metadata, stage the 1.21.4 JAR and required Fabric API in `MC 1.21.4 Fabric Smoke`, launch, and record its result/artifact. Original full build, runnable JAR verification, 4 GameTests, and smoke launch passed. The user reported control backgrounds and enchantment labels below the book icons; removed inherited button backgrounds and restored the original `itemY + 14` label position. In response to the user's layering clarification, matched vanilla tooltip rendering: label panel at z=400 from the base pose, then label text with pose translated to z=400. The user then reported entity-save crashes due to enchantment registry access; updated locked-trade cache save/load to use `RegistryOps` with the villager's `RegistryAccess` and added a registry-aware cache round-trip GameTest. Cycle payloads omitted by target source filtering were also supplied with VAR_INT codecs. Latest rebuild/artifact validation passed; all 5 GameTests passed, and the JAR/Fabric API were re-staged. Did not relaunch Prism per the user's instruction. No saved world opened.
- [x] Implement/adapt and compile the 1.21.3 target. The root compile reports the same 40 post-target API issues as 1.21.4/1.21.5; reused its bounded classic trade/NBT/client adapters and shared business/network behavior. Target compilation exposed one additional method removal: villager `stillValid(player)` is absent, so the cycling guard now uses the active `MerchantMenu.stillValid(player)` check. Common/client compilation passes. Keep configured Loader 0.16.14, Fabric API 0.114.1+1.21.3, and Mod Menu 12.0.1 pending validation.
- [x] Test, package, validate metadata, stage the 1.21.3 JAR and required Fabric API in `MC 1.21.3 Fabric Smoke`, launch, and record its result/artifact. The user reported button backgrounds, labels drawing below book icons, and a pause/save crash. Removed inherited backgrounds from both buttons, set the label panel/text to vanilla tooltip z=400 without changing label coordinates, and migrated locked-trade cache save/load to `RegistryOps` with villager registry access; added a cache round-trip GameTest. Build initially exposed missing target-specific cycling payload classes, so added both VAR_INT payload definitions. Final build/artifact validation passed; all 5 GameTests passed and the refreshed JAR/Fabric API were staged. Did not relaunch Prism or open the user's world; awaiting manual verification of controls, label layering, and pause/save behavior.
- [x] Implement/adapt and compile the 1.21.2 target. Baseline compilation reports the same 40 newer-API errors observed at 1.21.3; reused the 1.21.3 classic villager/NBT/UI adapters and shared logic. Target common/client compilation passes without further signature changes. Preserve configured Loader 0.16.14, Fabric API 0.106.1+1.21.2, and Mod Menu 12.0.1 unless staging identifies incompatibility.
- [ ] Implement/adapt and compile the 1.21.2 target.
- [x] Test, package, validate metadata, stage the 1.21.2 JAR and required Fabric API in `MC 1.21.2 Fabric Smoke`, launch, and record its result/artifact. User feedback prompted removal of inherited button backgrounds, label/background rendering at vanilla tooltip depth with unchanged label coordinates, and registry-aware `RegistryOps` for villager cache save/load plus a round-trip GameTest. Added 1.21.2-specific VAR_INT cycle payloads after target compilation showed the filtered shared payload classes were missing. Final build/artifact validation passed; all 5 GameTests passed and the JAR/Fabric API were re-staged. Did not relaunch Prism or open the user's world; awaiting manual verification of controls, label layering, and pause/save behavior.
- [x] Implement/adapt and compile the 1.21.1 target. Root-source compile shares classic villager/identifier/NBT failures with 1.21.2 and additionally lacks `ByteBufCodecs.CONTAINER_ID`; target codecs use `VAR_INT`, preserving the same integer wire encoding. Compilation also required the 1.21.1 tooltip API (`ClientTooltipComponent.getHeight()` and six-argument tooltip background renderer), handled in a bounded client overlay. Common/client compilation passes. Keep configured Loader 0.16.14, Fabric API 0.116.17+1.21.1, and Mod Menu 11.0.3 pending validation.
- [ ] Implement/adapt and compile the 1.21.1 target.
- [x] Test, package, validate metadata, stage the 1.21.1 JAR and required Fabric API in `MC 1.21.1 Fabric Smoke`, launch, and record its result/artifact. User feedback prompted removal of inherited button backgrounds, label/panel rendering at vanilla tooltip z=400 without changing label coordinates, and registry-aware `RegistryOps` for villager cache save/load plus a cache round-trip GameTest. Final build/artifact validation passed; all 5 GameTests passed and the JAR/Fabric API were re-staged. Did not relaunch Prism or open the user's world; awaiting manual verification of controls, label layering, and pause/save behavior.
- [x] Implement/adapt and compile the 1.21 target. Baseline compilation has the same 44 failures as 1.21.1; reused its legacy trade/NBT/client and VAR_INT payload adapters without extra method changes. Common/client compilation passes. Keep configured Loader 0.16.14, Fabric API 0.102.0+1.21, and Mod Menu 11.0.5 unless validation reports a conflict.
- [ ] Implement/adapt and compile the 1.21 target.
- [x] Test, package, validate metadata, stage the 1.21 JAR and required Fabric API in `MC 1.21 Fabric Smoke`, launch, and record its result/artifact. User feedback prompted removal of inherited button backgrounds, label/panel rendering at vanilla tooltip z=400 without changing label coordinates, and registry-aware `RegistryOps` for villager cache save/load plus a cache round-trip GameTest. Final build/artifact validation passed; all 5 GameTests passed and the JAR/Fabric API were re-staged. Did not relaunch Prism or open the user's world; awaiting manual verification of controls, label layering, and pause/save behavior.
- [x] Implement/adapt and compile the 1.20.6 target. Initial compile reports 44 boundary errors; reused the 1.21.1 legacy trade/NBT/client adapters and shared feature logic. The target-specific compile also required the older `ResourceLocation(namespace,path)` constructor and instance enchantment `getFullname(level)` method, isolated to the overlay. Common/client compilation passes. Keep configured Loader 0.16.14, Fabric API 0.100.8+1.20.6, and Mod Menu 10.0.0 pending validation.

#### Minecraft 1.20.x

- [ ] Implement/adapt and compile the 1.20.6 target.
- [x] Test, package, validate metadata, stage the 1.20.6 JAR and required Fabric API in `MC 1.20.6 Fabric Smoke`, launch, and record its result/artifact. User feedback prompted removal of inherited backgrounds from both custom buttons and moved the label panel/text to vanilla tooltip depth z=400 without changing label coordinates. Final build/artifact validation passed and all 4 GameTests passed; refreshed JAR/Fabric API were staged. Did not relaunch Prism or open a world; awaiting manual UI verification.
- [ ] Implement/adapt and compile the 1.20.5 target.
- [ ] Implement/adapt and compile the 1.20.5 target.
- [ ] Test, package, validate metadata, stage the 1.20.5 JAR and required Fabric API in `MC 1.20.5 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.20.4 target.
- [ ] Test, package, validate metadata, stage the 1.20.4 JAR and required Fabric API in `MC 1.20.4 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.20.3 target.
- [ ] Test, package, validate metadata, stage the 1.20.3 JAR and required Fabric API in `MC 1.20.3 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.20.2 target.
- [ ] Test, package, validate metadata, stage the 1.20.2 JAR and required Fabric API in `MC 1.20.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.20.1 target.
- [ ] Test, package, validate metadata, stage the 1.20.1 JAR and required Fabric API in `MC 1.20.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.20 target.
- [ ] Test, package, validate metadata, stage the 1.20 JAR and required Fabric API in `MC 1.20 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.19.x

- [ ] Implement/adapt and compile the 1.19.4 target.
- [ ] Test, package, validate metadata, stage the 1.19.4 JAR and required Fabric API in `MC 1.19.4 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.19.3 target.
- [ ] Test, package, validate metadata, stage the 1.19.3 JAR and required Fabric API in `MC 1.19.3 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.19.2 target.
- [ ] Test, package, validate metadata, stage the 1.19.2 JAR and required Fabric API in `MC 1.19.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.19.1 target.
- [ ] Test, package, validate metadata, stage the 1.19.1 JAR and required Fabric API in `MC 1.19.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.19 target.
- [ ] Test, package, validate metadata, stage the 1.19 JAR and required Fabric API in `MC 1.19 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.18.x

- [ ] Implement/adapt and compile the 1.18.2 target.
- [ ] Test, package, validate metadata, stage the 1.18.2 JAR and required Fabric API in `MC 1.18.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.18.1 target.
- [ ] Test, package, validate metadata, stage the 1.18.1 JAR and required Fabric API in `MC 1.18.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.18 target.
- [ ] Test, package, validate metadata, stage the 1.18 JAR and required Fabric API in `MC 1.18 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.17.x

- [ ] Implement/adapt and compile the 1.17.1 target.
- [ ] Test, package, validate metadata, stage the 1.17.1 JAR and required Fabric API in `MC 1.17.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.17 target.
- [ ] Test, package, validate metadata, stage the 1.17 JAR and required Fabric API in `MC 1.17 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.16.x

- [ ] Implement/adapt and compile the 1.16.5 target.
- [ ] Test, package, validate metadata, stage the 1.16.5 JAR and required Fabric API in `MC 1.16.5 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.16.4 target.
- [ ] Test, package, validate metadata, stage the 1.16.4 JAR and required Fabric API in `MC 1.16.4 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.16.3 target.
- [ ] Test, package, validate metadata, stage the 1.16.3 JAR and required Fabric API in `MC 1.16.3 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.16.2 target.
- [ ] Test, package, validate metadata, stage the 1.16.2 JAR and required Fabric API in `MC 1.16.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.16.1 target.
- [ ] Test, package, validate metadata, stage the 1.16.1 JAR and required Fabric API in `MC 1.16.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.16 target.
- [ ] Test, package, validate metadata, stage the 1.16 JAR and required Fabric API in `MC 1.16 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.15.x

- [ ] Implement/adapt and compile the 1.15.2 target.
- [ ] Test, package, validate metadata, stage the 1.15.2 JAR and required Fabric API in `MC 1.15.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.15.1 target.
- [ ] Test, package, validate metadata, stage the 1.15.1 JAR and required Fabric API in `MC 1.15.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.15 target.
- [ ] Test, package, validate metadata, stage the 1.15 JAR and required Fabric API in `MC 1.15 Fabric Smoke`, launch, and record its result/artifact.

#### Minecraft 1.14.x

- [ ] Implement/adapt and compile the 1.14.4 target.
- [ ] Test, package, validate metadata, stage the 1.14.4 JAR and required Fabric API in `MC 1.14.4 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.14.3 target.
- [ ] Test, package, validate metadata, stage the 1.14.3 JAR and required Fabric API in `MC 1.14.3 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.14.2 target.
- [ ] Test, package, validate metadata, stage the 1.14.2 JAR and required Fabric API in `MC 1.14.2 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.14.1 target.
- [ ] Test, package, validate metadata, stage the 1.14.1 JAR and required Fabric API in `MC 1.14.1 Fabric Smoke`, launch, and record its result/artifact.
- [ ] Implement/adapt and compile the 1.14 target.
- [ ] Test, package, validate metadata, stage the 1.14 JAR and required Fabric API in `MC 1.14 Fabric Smoke`, launch, and record its result/artifact.

### Final verification and documentation

- [ ] Run the complete target build/test matrix and confirm all 46 artifacts are staged in the corresponding Prism mods folders with only compatible dependencies.
- [ ] Verify every target JAR's `fabric.mod.json`, mixin configs, entrypoints, Java bytecode level, and runtime dependency list; verify no game-test classes are packaged.
- [ ] Verify Prism launch logs for all 46 staged mod instances and record any game/mod incompatibilities separately from successful Fabric-only smoke launches.
- [ ] Update README compatibility/install/build sections with the exact supported releases, per-version Fabric API/Loader/Java requirements, and artifact naming.
- [ ] Review final changes, ensure local AGENTS.md and Prism/world data are not committed, and report the 46 artifact paths plus any feature limitations and unverified manual tests.
