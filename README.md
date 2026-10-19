# Easy Trade

Fabric mod for quality-of-life improvements to villager trading.

Implemented:

- Show a tooltip-style label with only the enchantment name and level beside each enchanted-book offer, with a trade-screen button to toggle labels; normal item tooltips remain available on hover.
- Preview locked villager offers when Easy Trade is installed on the server. Previews use server trade registries and are reused when the villager levels up; a client joining a server without Easy Trade simply gets no locked-offer preview.

Planned features:

- Cycle villager trades without removing and replacing the workstation.

## Setup

For IDE setup, see the [Fabric project setup guide](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up).

The initial target is Minecraft 26.3. Additional Minecraft versions will be
added as separate, tested builds when selected; Minecraft and Fabric API changes
may require version-specific code.

## License

Easy Trade is licensed under the MIT License. See [LICENSE](LICENSE).
