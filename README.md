# Pocket Portal (Fabric, Minecraft 26.3)

- **Obsidian mines like glass.** Break time uses glass hardness (0.3). Blast resistance (1200) is untouched, and the
  diamond-pickaxe requirement for drops is unchanged.
- **Pocket Nether Portal.** Right-click to jump Overworld <-> Nether at 8:1 coordinates, landing on the nearest safe spot
  (searches up to 16 blocks around the target). No portal blocks are created. 2 second cooldown, not consumed.
  Recipe: 8 obsidian around a flint and steel.

Build with JDK 25 and Gradle 9+: `gradle build` (jar in `build/libs`). The included GitHub Action does this too.
