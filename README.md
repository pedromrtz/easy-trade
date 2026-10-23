<div align="center">
  <img src="./src/main/resources/assets/trade-assistant/icon.png" alt="Trade Assistant icon" width="128" />
  <h1>Trade Assistant</h1>
  <p>Small quality-of-life improvements for Minecraft villager trading.</p>
  <p>
    <a href="https://github.com/pedromrtz/trade-assistant">GitHub repository</a>
    · Minecraft 26.3
    · Fabric
    · Java 25
  </p>
</div>

Trade Assistant adds clearer enchanted-book offers, previews for locked villager trades, and a server-validated way to refresh eligible villager offers.

## Features

- **Enchantment labels:** show enchantment names and levels beside enchanted-book offers. Toggle the labels from the trade screen; the regular item tooltip remains available on hover.
- **Locked-trade previews:** when the server has Trade Assistant, preview future offers using the server's trade data. Cached previews are reused when the villager levels up.
- **Trade cycling:** refresh an untraded novice villager's offers from the trade screen or press **C**. The key can be rebound in **Options → Controls**. Cycling does not replace the workstation or consume payment items.

## Configuration

Install [Mod Menu](https://modrinth.com/mod/modmenu) to open Trade Assistant's configuration screen from the Mods list. Mod Menu is optional; Trade Assistant works without it, but the settings screen is only available when Mod Menu is installed. No additional configuration library is required.

The screen has local-player toggles for enchanted-book labels, locked-trade previews, and trade cycling. Choose **Save & Exit** to apply changes or **Cancel** to discard them. Preferences are stored in `config/trade-assistant.json` in the Minecraft instance folder. The enchanted-book label toggle is also available directly in the villager trade screen.

Cycling is available only while the villager is an untraded novice. The server validates every request, applies a short cooldown, and refreshes the displayed trade selection and locked-offer preview.

## Compatibility

| Where Trade Assistant is installed | Available features |
| --- | --- |
| Client only | Enchanted-book labels |
| Client and server | Enchanted-book labels, locked-trade previews, and trade cycling |
| Server only | Unmodded clients keep the vanilla trade screen |

In singleplayer, the integrated server provides the server-side features. The server does not require clients to install Trade Assistant; clients without it continue using the vanilla trade screen.
When the client mod is installed without server support, enchantment labels remain available while the cycling control stays hidden and locked-trade previews are unavailable.

> [!NOTE]
> The first supported game version is **Minecraft 26.3**. Use a matching Fabric Loader and Fabric API for that Minecraft version.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3 and the matching [Fabric API](https://modrinth.com/mod/fabric-api).
2. Optionally install [Mod Menu](https://modrinth.com/mod/modmenu) to access the Trade Assistant settings screen.
3. Download the Trade Assistant JAR from the [GitHub Releases](https://github.com/pedromrtz/trade-assistant/releases) page.
4. Place the JAR in the instance's `mods` folder. Install it on the server as well to enable cycling and locked-trade previews there.

## Build from source

You need **JDK 25**. Clone the repository and run the Gradle wrapper from the project root:

```bash
git clone https://github.com/pedromrtz/trade-assistant.git
cd trade-assistant
./gradlew build
```

On Windows PowerShell, run:

```powershell
.\gradlew.bat build
```

The runnable mod JAR is written to `build/libs/trade-assistant-1.0.0.jar` (do not use the `-sources.jar`). The build compiles common and client code, generates language resources, and runs the server game tests.

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
| `src/localization/trade-assistant-languages.json` | Source strings and locale mappings used to generate language resources |

Language files are generated for Minecraft's 142 locales. The current bundle includes translations for 34 non-English base languages; other locales use the English fallback.
