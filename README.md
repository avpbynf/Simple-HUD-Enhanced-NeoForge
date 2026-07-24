# Simple HUD Enhanced — NeoForge (1.21.11)

**Unofficial NeoForge port** of [Simple HUD Enhanced](https://github.com/SoRadGaming/Simple-HUD-Enhanced) by [SoRadGaming](https://github.com/SoRadGaming).

This branch is a fork of the original Fabric mod, adapted to run on the [NeoForge](https://neoforged.net/) mod loader for Minecraft 1.21.11. All credit for the mod itself — its features, design and assets — goes to SoRadGaming and the upstream contributors. Only the loader-specific plumbing (build system, mod metadata, event hooks) differs from upstream.

The original Fabric version is available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-hud-enhanced) and [Modrinth](https://modrinth.com/mod/simple-hud-enhanced).

## Description
A Minecraft mod that enhances the game's Heads-Up Display (HUD) by introducing
customizable elements and features to display information.
The mod is designed to be lightweight and easy to use, allows users to adjust the position, visibility,
and other settings of these elements, providing a personalized HUD experience
tailored to individual preferences.

## Installation
1. Download the latest version of the mod from the releases page.
2. Download and install [NeoForge](https://neoforged.net/) for Minecraft 1.21.11.
3. Place the downloaded mod in the `mods` folder in your Minecraft directory.
4. Launch the game using the NeoForge profile.
5. Enjoy!

Cloth Config is bundled inside the jar — no other dependency is required.

## Differences from the Fabric version
- No Mod Menu needed: the config screen is reachable from NeoForge's built-in mod
  list (Config button) and from the dedicated button in the pause menu.
- The Trinkets integration is not included (Trinkets is Fabric-only; the NeoForge
  analogue would be Curios). The equipment HUD falls back to the vanilla armour,
  main-hand and off-hand slots.
- Client-side TPS tracking is preserved: it is estimated from the server's time
  packets, exactly like the Fabric build, so no server-side mod is required.
- Opening the F3 debug screen no longer overwrites the HUD toggle in the config:
  the HUD is simply hidden while F3 (or F1) is held, without rewriting or saving
  the setting (a deliberate divergence from upstream).

## Code Contributions
The original mod has been built from the ground up to be modular and faster to update,
originally as a fork of Simple Utilities Mod by [johnvictorfs](https://github.com/johnvictorfs/simple-utilities-mod).

It has also been built with the help of the following mods:
- For Status Effect Rings:
[A5b84](https://github.com/A5b84/status-effect-bars)
- For TPS Tracking Client Side:
[mooziii](https://github.com/mooziii/tpshud-fabric)

Issues and suggestions specific to the NeoForge port belong on this repository's
issue tracker; anything about the mod's features themselves is best reported
[upstream](https://github.com/SoRadGaming/Simple-HUD-Enhanced/issues).
