<div align="center">
  <img src="./src/main/resources/assets/easy-trade/icon.png" alt="Easy Trade mod icon" width="128" />
  <h1>Easy Trade</h1>
  <p>Small quality-of-life improvements for Minecraft villager trading.</p>
  <p>
    <a href="https://github.com/pedromrtz/easy-trade">GitHub repository</a>
    · Minecraft 26.3
    · Fabric
    · Java 25
  </p>
</div>

Easy Trade adds clearer enchanted-book offers, previews for locked villager trades, and a server-validated way to refresh eligible villager offers.

## Features

- **Enchantment labels:** show enchantment names and levels beside enchanted-book offers. Toggle the labels from the trade screen; the regular item tooltip remains available on hover.
- **Locked-trade previews:** when the server has Easy Trade, preview future offers using the server's trade data. Cached previews are reused when the villager levels up.
- **Trade cycling:** refresh an untraded novice villager's offers from the trade screen or press **C**. The key can be rebound in **Options → Controls**. Cycling does not replace the workstation or consume payment items.

Cycling is available only while the villager is an untraded novice. The server validates every request, applies a short cooldown, and refreshes the displayed trade selection and locked-offer preview.

## Compatibility

| Where Easy Trade is installed | Available features |
| --- | --- |
| Client only | Enchanted-book labels |
| Client and server | Enchanted-book labels, locked-trade previews, and trade cycling |
| Server only | Unmodded clients keep the vanilla trade screen |

In singleplayer, the integrated server provides the server-side features. The server does not require clients to install Easy Trade; clients without the mod continue using the vanilla trade screen.
When the client mod is installed without server support, enchantment labels remain available while the cycling control stays hidden and locked-trade previews are unavailable.

> [!NOTE]
> The first supported game version is **Minecraft 26.3**. Use a matching Fabric Loader and Fabric API for that Minecraft version.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3 and the matching [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the Easy Trade JAR from the [GitHub Releases](https://github.com/pedromrtz/easy-trade/releases) page.
3. Place the JAR in the instance's `mods` folder. Install it on the server as well to enable cycling and locked-trade previews there.

## Build from source

You need **JDK 25**. Clone the repository and run the Gradle wrapper from the project root:

```bash
git clone https://github.com/pedromrtz/easy-trade.git
cd easy-trade
./gradlew build
```

On Windows PowerShell, run:

```powershell
.\gradlew.bat build
```

The runnable mod JAR is written to `build/libs/easy-trade-1.0.0.jar` (do not use the `-sources.jar`). The build compiles common and client code, generates language resources, and runs the server game tests.

To run the game tests without the full build:

```powershell
.\gradlew.bat runGameTest
```

On macOS or Linux, use `./gradlew runGameTest`.

The tests cover cycling eligibility, invalid requests, request cooldowns, preservation of payment items, and locked-offer cache invalidation.

## Project layout

| Path | Purpose |
| --- | --- |
| `src/main/java` | Server/common code, networking, trade logic, and common mixins |
| `src/client/java` | Client UI, enchantment labels, and client mixins |
| `src/gametest` | Server game tests; excluded from the distributable mod JAR |
| `src/localization/easy-trade-languages.json` | Source strings and locale mappings used to generate language resources |

Language files are generated for Minecraft's 142 locales. The current bundle includes translations for 34 non-English base languages; other locales use the English fallback.
