# Easy Trade

Fabric mod for quality-of-life improvements to villager trading.

Implemented:

- Show a tooltip-style label with only the enchantment name and level beside each enchanted-book offer, with a trade-screen button to toggle labels; normal item tooltips remain available on hover.
- Preview locked villager offers when Easy Trade is installed on the server. Previews use server trade registries and are reused when the villager levels up; a client joining a server without Easy Trade simply gets no locked-offer preview.
- Cycle untraded novice villagers' offers using the refresh icon in the trade screen or **C** (reassignable in Controls), without replacing their workstation or waiting for working hours. Cycling also refreshes locked-offer previews. Once any trade has been completed, cycling is disabled.

Cycling and locked-offer previews work in singleplayer and on servers with Easy Trade installed.
On servers without Easy Trade, only the client-side enchantment labels are available; the cycling control is hidden.
Servers with Easy Trade also accept clients without the mod, which continue to use the vanilla trade screen.

## Setup

For IDE setup, see the [Fabric project setup guide](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up).

The initial target is Minecraft 26.3. Additional Minecraft versions will be
added as separate, tested builds when selected; Minecraft and Fabric API changes
may require version-specific code.

Build with JDK 25 and `./gradlew build` (`.\gradlew.bat build` on Windows).
The build includes server game tests for cycling eligibility, invalid requests, cooldowns,
preserving payment items, and invalidating locked-offer previews. To run those tests alone,
use `.\gradlew.bat runGameTest`.

## License

Easy Trade is licensed under the MIT License. See [LICENSE](LICENSE).
