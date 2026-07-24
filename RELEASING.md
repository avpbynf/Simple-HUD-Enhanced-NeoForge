# Versioning & release process

This fork ports [Simple HUD Enhanced](https://github.com/SoRadGaming/Simple-HUD-Enhanced) (Fabric) to NeoForge. This document describes how versions are numbered, how releases are published, and how the port is brought up to a new upstream or Minecraft version.

## Branches

- `main` — the port for the newest supported Minecraft version (currently 26.2).
- `<mcver>-neoforge` (e.g. `26.2-neoforge`, `26.1-neoforge`) — one branch per Minecraft version, mirroring upstream's branch-per-game-version layout. The newest version's branch is kept in sync with `main`; the older ones are maintenance branches.

## Version numbers

- The mod version always mirrors the upstream release the port is based on (currently **4.7.5**). A given `X.Y.Z` on NeoForge has the same features as upstream's `X.Y.Z` on Fabric.
- Port-specific fixes (nothing changed upstream) bump a fourth digit: `4.7.5.1`, `4.7.5.2`, …
- A new game-version branch's first release ships at the fork's current fix level (its first release may be `4.7.5.1` directly, with no `4.7.5` before it), so equal version numbers mean equal fix content across branches.
- Never invent a three-digit version upstream hasn't released — those numbers belong to upstream.

## Tags & releases

- One tag per release per game version: `v<modversion>+<mcver>` — e.g. `v4.7.5+26.2` on `main`, `v4.7.5+26.1` on `26.1-neoforge`.
- Publishing a GitHub release for such a tag triggers the publish workflow (`.github/workflows/publish.yml`), which builds the jar and uploads it to CurseForge with the release notes as changelog.
- When publishing the same mod version for several game versions, publish the oldest game version first and the newest last, so GitHub's "Latest" badge lands on the newest one (or fix it afterwards by setting `make_latest` on the right release).

## Updating to a new upstream release (e.g. upstream ships 4.7.6)

1. `git fetch upstream`
2. Rebase the port commits onto the updated upstream branch (or re-apply them by cherry-pick). The port is deliberately kept as a small commit series:
   build system (ModDevGradle) → mod metadata + entrypoints → mixins → Trinkets cleanup → version ranges → README.
3. Update `mod_version` in `gradle.properties`.
4. `./gradlew build`, then `./gradlew runClient`: check the mod loads and the HUD renders. Pay attention to the effect duration bars — the `GuiMixin` local-variable capture is the most version-sensitive injection.
5. Tag (`v4.7.6+<mcver>`) and publish the GitHub release.

## Adding support for a new Minecraft version (e.g. 26.3)

1. Wait for the upstream `26.3` branch, then create `26.3-neoforge` from `upstream/26.3` and fast-forward `main` policy: `main` always follows the newest version, older ports live on their own branches.
2. Cherry-pick the port commit series from the previous port branch.
3. In `gradle.properties`, update:
   - `minecraft_version` and `minecraft_version_range` (mirror upstream's supported range),
   - `neo_version` (latest NeoForge for that game version on [maven.neoforged.net](https://maven.neoforged.net/releases/net/neoforged/neoforge/)) and `neo_version_range`,
   - `archives_base_name`,
   - `cloth_config_version` (matching `cloth-config-neoforge`).
4. Re-verify the fragile spots against the new sources: `./gradlew createMinecraftArtifacts`, then check `Hud.extractEffects` still matches the `GuiMixin` injection points (blitSprite ordinal, `effect`/`x`/`y` locals).
5. Build, runClient, tag, release.

## Port fixes affecting several branches

Fix on the newest branch first, then cherry-pick to the maintained older branches. Bump the fourth digit on every branch you release from.
