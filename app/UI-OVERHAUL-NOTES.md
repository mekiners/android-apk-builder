# UI overhaul notes

This revision is based on the original Android project and the user's page-2 visual reference. It preserves the existing Room entities, ViewModel callbacks, navigation, alarm/voice logic, and all 42 original hand-drawn pixel sprites.

## Visual changes

- Reworked the forest-cottage palette around moss, leaf green, warm parchment, shaded timber, amber honey, and terracotta.
- Split typography into pixel-inspired display headings and readable sans-serif body/number styles. Tabular numeral features are enabled for consistent number widths.
- Kept the original pixel icon artwork and rasterized each tiny sprite grid once, then scales it with nearest-neighbour sampling so the silhouettes stay crisp at different display densities.
- Rebuilt the material textures at 256 x 256 pixels and reduced material scaling to roughly 1 dp per source pixel. Texture art is sampled smoothly; icons remain pixel-crisp.
- Reworked shared panels, buttons, chips, fields, the title rail, and bottom navigation to reduce heavy bevels, repeated rivets, and identical high-contrast frames.
- Kept distinct quest materials: moss-framed parchment for Easy, parchment with a soft amber honey wash and a width-responsive drip edge for Medium, and cracked terracotta/rock for Hard.
- Made the honey strip scale to the actual card width and reserved a dedicated title area below the drips to avoid cropping or text overlap on compact phones.
- Moved the decorative vines behind screen content and reduced their scale/opacity.
- Refined the dashboard summary layout, market paper notes, shift calendar cells, and workout spacing.

## Typography note

The user's named font references were NT Brick Sans and Techpixs. Neither font file was supplied for this project. NT Brick Sans has a separate app-embedding license option; the Techpixs listing identifies it as an all-rights-reserved display font. This revision therefore does not redistribute either third-party font. It keeps the already-included Pixelify font for short display headings and uses Android's sans-serif for body copy and numbers. A properly licensed app font can replace the display face later.

## Scope and build verification

Only files under the `app/` folder were modified. Gradle files, manifest, database entities, ViewModel behavior, GitHub workflow files, and root project configuration were left unchanged.

A full Android build could not be run in this editing environment: the Gradle 8.0 wrapper distribution is not cached, and the environment could not reach `services.gradle.org`. Run `./gradlew assembleDebug` in Android Studio or in the connected GitHub Actions runner to complete the Android build verification.
