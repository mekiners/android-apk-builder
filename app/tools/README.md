# tools/ — asset generators (Python, not part of the Android build)

These scripts produced the pixel art, textures and widget art. They are kept so a future AI can regenerate or
tweak assets. They need `pip install pillow numpy fonttools`. All paths are hard-coded to the original sandbox
(`/home/claude/...`, `/tmp/art`, `/tmp/fonts`): edit them before running.

| Script | Output | Notes |
|---|---|---|
| `sprites.py` | text-grid pixel icons (12x12, cat 16x10, etc.) | Output was converted into `ui/theme/PixelSprites.kt` (object `Sprites`). Letters in the grids map to the `PALETTE` dict, which became `SpritePalette` in Kotlin. |
| `textures.py` | `tex_*.png` tiles and `deco_*.png` overlays | Copy results into `app/src/main/res/drawable-nodpi/`. Tiles are seamless. File names are referenced in `ui/theme/Textures.kt`. |
| `art.py` | smooth high-resolution frames (honey drips, leafy border, lava, torn parchment) | SAMPLE ONLY, not used in the app yet. Starting point if the user approves the smooth style. |
| `widgetart.py` | `widget_panel.9.png`, `btn_in.9.png`, `btn_out.9.png` (nine-patch) | Used by the three home-screen widgets. Imports helpers from `art.py`. |

To add a sprite: add a grid to `sprites.py`, regenerate the Kotlin `Sprites` object (see the loop at the end of
the generation snippets in AI_GUIDE.md section 7), and reference it as `Sprites.name`.
