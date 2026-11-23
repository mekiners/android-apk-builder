# AI_GUIDE: Cozy Assistant (Stardew-style Android app)

Read this whole file before editing. It is the hand-over from the AI that built the app with the user over a long
conversation. Update it (section 11 especially) whenever you change something important.

---------------------------------------------------------------------------------------------------------------------

## 1. What this is, and who it is for

- A personal Android life-organizer with a **Stardew Valley / cottagecore pixel-art look**. Package `com.example.sampleapp`,
  app name "Cozy Assistant". Kotlin + Jetpack Compose + Room. One module (`app`).
- The user is **not a developer**. They assemble the app "with the help of several AIs" (earlier code came from Gemini and
  DeepSeek and was rewritten). They build the APK through **GitHub Actions from a tablet/phone** and install the debug APK.
- Their work is **rotating three-shift air traffic control** (Day 07:00-15:00, Eve 15:00-23:00, Mid 23:00-07:00), which is why
  the Shifts tab and time clock exist. Philippines, so Android phone, 24-hour times, week starts Monday.
- Tone they like: warm, practical, honest. They give detailed feedback and iterate visually. They hate anything that breaks
  the cozy immersion (emoji icons, default Material look, plain boxes, hard-to-read fonts).

## 2. Working agreement and hard constraints

1. **The AI sandbox cannot compile Android code** (no Android SDK, Google Maven is blocked). Everything is verified only by the
   user's GitHub build. So: write conservative Kotlin, double-check imports, scopes and smart casts, and run the static checks in
   section 10 before delivering. When a build fails, the user pastes the Gradle error; fix exactly that.
2. **Deliver one zip named `cozy-assistant-stardew.zip`** containing the whole project folder
   `android-apk-builder-2.0-cozy-stable/`. Same filename every time (their workflow depends on it).
3. **Only the `app/` folder is deployed** by the user's unpack workflow (section 3). Changes to root files
   (`build.gradle`, `settings.gradle`, `.github/workflows`, `gradle.properties`) will NOT reach the repo unless you tell the user to
   edit them on GitHub by hand. Keep all changes inside `app/` whenever possible.
4. The user said "let's discuss first" for big design changes. For visual overhauls, propose and ask one question, then build.
5. Never store or invent personal data. Do not put emoji in the UI (they dislike it); use pixel sprites.
6. Keep answers short and plain; they read on a phone.

## 3. Build and delivery pipeline

- Repo layout (root): `.github/workflows/android-build.yml`, `build.gradle`, `settings.gradle`, `gradle*`, `app/`, plus
  boilerplate docs.
- `android-build.yml` runs on push: JDK 17, `./gradlew build`, then `./gradlew assembleDebug`. The user downloads the **debug APK**
  from the run's Artifacts. (`lint.abortOnError false` and `checkReleaseBuilds false` are set in `app/build.gradle` so lint never
  fails the build.)
- Versions: AGP 8.1.4, Gradle wrapper 8.0, Kotlin 1.9.0, Compose compiler 1.5.1, Compose BOM 2024.02.00 (Compose ~1.6, Material3
  1.2), Room with kapt, compileSdk/targetSdk 34, **minSdk 26** (raised from 24 so `java.time` works without desugaring).
- The user cannot upload folders on their tablet, so they upload the zip as a single file and run a one-time workflow
  `.github/workflows/unpack.yml` (manual "Run workflow"):

```yaml
name: Unpack zip
on:
  workflow_dispatch:
permissions:
  contents: write
jobs:
  unpack:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Unpack
        run: |
          set -e
          unzip -q -o cozy-assistant-stardew.zip -d /tmp/unz
          SRC=$(find /tmp/unz -maxdepth 1 -mindepth 1 -type d | head -n 1)
          rm -rf app
          cp -r "$SRC"/app ./app
          rm -f cozy-assistant-stardew.zip
      - name: Commit
        run: |
          git config user.name "github-actions"
          git config user.email "github-actions@users.noreply.github.com"
          git add -A
          git commit -m "Unpack Stardew app" || echo "nothing to commit"
          git push
```
  It deletes and replaces `app/`, so removed files disappear (that is intended). Pushes made by a workflow may not trigger the
  build; the user then runs the build workflow manually or makes a tiny commit.
- Sandbox network allow-list (for the AI): github.com / raw.githubusercontent.com, PyPI, npm and a few others work. Google
  Fonts files can be fetched from `raw.githubusercontent.com/google/fonts/main/ofl/...` (URL-encode `[` `]`).

## 4. Code map (`app/src/main/java/com/example/sampleapp/`)

| File | Role |
|---|---|
| `MainActivity.kt` | Activity + `CozyAssistantApp` scaffold: top banner, bottom dock, six tabs, per-tab background (`ScreenKind`), `PanelLook` per tab (stone on Gym), vine overlay. Reads intent extra `tab` (widgets/notifications open a given tab). Requests notification permission (Android 13+). Calls `vm.refreshDay()` in `onResume`. |
| `ui/CommandViewModel.kt` | Single AndroidViewModel for everything: StateFlows (`tasks`, `todayTasks`, `categories`, `user`, `rewards`, `groceries`, `groceryRules`, `workouts`, `routines`, `shifts`, `timeLogs`, `today`) and all actions. First-run seeding, repeating-quest rollover, alarms, widget refresh. |
| `ui/screens/DashboardScreen.kt` | Home: greeting panel, quick-add (text + mic + plus), options (reward/repeat/when), category chips, quest list, completed section. |
| `ui/screens/QuestUi.kt` | Shared quest UI: `QuestCard` (difficulty looks), difficulty/repeat/when controls, `CustomRepeatDialog`, `QuestEditorDialog`. |
| `ui/screens/QuestsScreen.kt` | Today's quests (overdue / today / done) with progress bar. |
| `ui/screens/ShopScreen.kt` | Rewards shop, reward idea generator, redeem confirm. |
| `ui/screens/MarketScreen.kt` | Grocery list as a cork bulletin board with pinned notes, section signs, learned categories. |
| `ui/screens/ShiftScreen.kt` | Month calendar with 3 boxes/day, brush quick-fill, day inspector, **time clock**, punch log, add-punch dialog. |
| `ui/screens/WorkoutScreen.kt` | Routines as expandable cards with customizable exercises, templates, set logging, PRs, streak and nudges. |
| `ui/screens/StatsScreen.kt` | Insights tab: recap, heatmap, pay, punctuality, leave, time budget, Settings dialog. |
| `ui/theme/Theme.kt` | Palette tokens, fonts, `StardewTypography`, `StardewTheme` (provides textures + default `PanelLook`). |
| `ui/theme/Components.kt` | All reusable widgets: `PanelBox`, `StardewPanel`, `StardewBanner`, `StardewButton`, `StardewIconButton`, `PixelIconButton`, `StardewChip`, `StardewCheckbox`, `StardewTextField`, `StardewDialog`, `StardewDock`, `SectionLabel`, `NoteTag`, `RestingCat`, `GoldBadge`, `Pill`. |
| `ui/theme/PixelSprites.kt` | GENERATED. `Sprite` class, `SpritePalette`, `PixelIcon`, and the `Sprites` object (icons drawn from text grids). |
| `ui/theme/Textures.kt` | Texture loading, `PanelLook`, `panelChrome`, `tiledBackground`, `bevel`, `screenBackground`, `VineOverlay`, draw helpers. |
| `data/*.kt` | Room entities and `AppDatabase.kt` (all DAOs + migrations). |
| `util/Dates.kt` | Recurrence math, labels, millis conversion. |
| `util/GroceryLogic.kt` | Section keyword detection + learned rules. |
| `util/ShiftLogic.kt` | Shift windows, next/current shift, headline/detail strings (`ShiftSlots` constants). |
| `util/TimeClock.kt` | Punch (time in/out) logic shared by app, widget and notification button. |
| `util/Stats.kt`, `util/Settings.kt` | Pure calculation helpers (pay, punctuality, recap, heatmap, gym insights) and the SharedPreferences settings store. |
| `CommandParser.kt` | Parses typed/spoken quests ("buy milk tomorrow at 5pm hard quest daily"). |
| `VoiceButton.kt` | Mic button using `RecognizerIntent` (no mic permission needed). |
| `AlarmScheduler.kt`, `AlarmReceiver.kt` | Per-quest reminder notifications. |
| `ShiftReminders.kt` | Shift reminders (15 min before to time in; at shift end to time out), `ShiftReminderReceiver`, `BootReceiver`. |
| `Widgets.kt`, `CozyWidget.kt`, `GroceryWidget.kt`, `ShiftWidget.kt` | Home-screen widgets (section 6.7). |

Resources: `res/font` (pixelify_regular/bold, jersey15_regular), `res/drawable-nodpi` (all PNG textures, decorations and nine-patches),
`res/drawable` (launcher icon vector, widget marker shapes), `res/layout/widget_*.xml`, `res/xml/widget_*info.xml`.

## 5. Data model and migrations (Room, currently version 8)

Tables: `tasks`, `categories`, `user_profile` (single row id=1, `coins`), `rewards`, `groceries`, `grocery_rules`, `workouts`,
`routines`, `shifts`, `time_logs`, `quest_log`, `leave_days`, `workout_logs`.

- `TaskEntity`: title, category, `gold` (1/5/10), `dueDay` (epoch day, null = none), `timeMinutes` (minutes after midnight, null = none),
  `recurrence` ("None","Daily","Weekly","Monthly","Days"), `repeatDays` (bitmask, bit0 = Mon ... bit6 = Sun, used when "Days"),
  `isCompleted`, `completedDay`.
- `ShiftEntity(epochDay PK, slots)`: slots bitmask over SIX shift types: bit0 Day 07-15, bit1 Eve 15-23, bit2 Mid 23-07, bit3 Day-Eve 11-19,
  bit4 Eve-Mid 19-03, bit5 Mid-Day 03-11. `epochDay` is the **roster date** (the calendar cell): Mid belongs to the date it ENDS on (it starts at 23:00
  the evening before), Eve-Mid belongs to the date it starts on (19:00) and ends 03:00 next day. Use `ShiftSlots.startOf(rosterDay, slot)` for real start times. No row when slots = 0.
- `TimeLogEntity`: `shiftDay` (the shift's ROSTER date, see above), `slot` (0-2, or -1 if not matched to a scheduled shift), `inMillis`,
  `outMillis` (null = still timed in).
- `GroceryEntity`: name, section, isBought, quantity (text), price (nullable), inStock (pantry), boughtDay. `GroceryRuleEntity(ruleKey PK, section)`: learned name -> section.
- `WorkoutEntity`: name, detail, `routineId`, `doneDay` (counts as done only when equal to today). `WorkoutLogEntity`: history; weightKg == null is a plain done-tick, otherwise one logged set.
  `QuestLogEntity`: one row per quest completion (history for recaps/heatmap). `LeaveDayEntity(epochDay, kind)`: "Vacation" or "Sick". `RoutineEntity`: name.
- Migrations so far: 4->5 (tasks.repeatDays, routines table, workouts.routineId, existing workouts moved into "My Routine"),
  5->6 (time_logs), 6->7 (grocery_rules), 7->8 (groceries.quantity/price/inStock/boughtDay, quest_log, leave_days, workout_logs). Versions 1-3 predate the rewrite and were destructive.

**Rules for schema changes (important, the user already lost data once):**
1. Bump `version`, add a `Migration(old, new)` and register it in `addMigrations(...)`. `fallbackToDestructiveMigration()` stays
   only as a last-resort safety net; never rely on it.
2. Write the migration SQL to match what Room expects: backtick names, `NOT NULL` for non-null Kotlin types, nullable types
   without NOT NULL, new columns via `ALTER TABLE ... ADD COLUMN ... NOT NULL DEFAULT x` (defaults are fine, Room does not
   compare defaults unless declared). Test the SQL with Python `sqlite3` if possible.
3. Add the entity to `@Database(entities=[...])`, expose a DAO, and expose a `Flow` in the ViewModel.

## 6. Feature behavior (what exists and the rules behind it)

### 6.1 Home (Dashboard)
- Greeting: "Good morning/afternoon/evening" (no name, user asked for this), date, today's quest count, next shift summary, sun/sunset/moon sprite by hour.
- Quick add: 40dp text field, mic, plus (all the same height). Options toggle is an arrow button only. Small summary line shows coin icon + amount, repeat, category.
- Options: reward (coin icons 1 / coin pile 5 / gold sack 10, shown as icon + number, no words), repeat (Once, Daily, Weekly, Custom), when (Today, Tomorrow, Pick date, No date, time).
- Category chips (All + user categories; seeded: maknae, To Do, To Buy, Work). Long-press a chip to delete; "+" adds. Emoji field on `CategoryEntity.icon` is unused on purpose.
- Typed or spoken text is parsed by `CommandParser`: times ("at 5pm", "at 17:30"), "today/tomorrow", "daily/weekly/monthly/every day...",
  difficulty only as "easy/medium/hard quest" or "difficulty hard" (so titles like "hard drive" are safe).

### 6.2 Quests (rules)
- Gold: Easy 1, Medium 5, Hard 10. Completing adds gold; un-checking subtracts it.
- Custom repeat = Monthly (same date each month) or "Days" (chosen weekdays, e.g. Tue + Wed). The first due date is aligned to a matching weekday.
- **Rollover** (`rolloverRepeatingQuests`, runs at start and on resume): a repeating quest whose due day has passed moves to its next
  occurrence >= today and becomes unchecked (missed ones are skipped, not piled up). One-off overdue quests stay and show as Overdue.
- Today's list = due today or overdue, plus anything completed today. "Clear done" deletes only completed one-off quests.
- Quest alarms: `AlarmScheduler` + `AlarmReceiver`; recreated when a quest is edited or rolls over.
- Grocery items can be sent to quests ("Buy X", category To Buy).

### 6.3 Shop
Rewards with gold cost; redeem asks for confirmation and subtracts gold atomically (`addCoins` SQL with MAX(0, ...)).
"Generate Reward Idea" picks from a built-in list (not already in the shop) and offers to add it.

### 6.4 Market (grocery)
- Sections: Produce, Meat, Dairy, Bakery, Pantry, Household, Tech, Misc. Auto-detection in `GroceryLogic.detect`:
  learned rules first, then multi-word phrases, then whole-word keywords (plural forms allowed, Filipino terms included).
- **Learning:** when the user picks a section chip (not Auto) while adding an item, or moves an item by tapping its icon,
  `learnSection` stores `normalize(name) -> section` in `grocery_rules` (skipped if the built-in guess already agrees). Learned
  names match exactly or as whole words inside longer names ("gaming pc" follows "pc"). There is no UI yet to list/forget rules.
- **Pantry:** items can be "in stock" (inStock). "Stock up" moves the basket to the pantry; "Need more" returns an item to the list; adding a name already in the pantry shows a toast instead. Each item can carry a quantity note and a price estimate (currency from Settings); a footer shows the estimated total. Tap an item to edit section, quantity, price or move it to the pantry.
- Look: cork board in a wood frame, paper notes with red pins, wooden section signs. Items can be sent to quests.

### 6.5 Shifts
- **Six shift types** (all 8 h, start times 4 h apart): Mid 23-07, Mid-Day 03-11, Day 07-15, Day-Eve 11-19, Eve 15-23, Eve-Mid 19-03. The user's roster
  convention: **a day starts with Mid, then Day, then Eve**, so each calendar cell has three boxes in the order **[Mid][Day][Eve]**. In-between
  shifts are drawn as half-fills bridging two neighbouring boxes (Mid-Day: Mid right half + Day left half; Day-Eve: Day right + Eve left;
  Eve-Mid: Eve right half of this day + Mid left half of the NEXT day). Colors: Day green, Eve gold, Mid terracotta, D-E teal, E-M violet, M-D slate blue.
  `boxHalves()` in ShiftScreen holds the mapping. The user confirmed Eve-Mid is listed on the date it starts.
- Calendar: full month, Monday-first. Brushes (Select, six types, Off, Leave) fill days quickly; the day inspector toggles each type and sets a day off
  (Vacation/Sick). Next/current shift banner updates every 30 s (`ShiftLogic.nextOrCurrent`).
- **Time clock:** one button toggles time in / time out. `TimeClock.punch`: an open session younger than 18 h is closed; otherwise a new session starts and is
  matched to a scheduled shift whose window (start-3h to end+1h, closest start wins) contains now. A session open for 18 h or more is auto-closed at its
  shift end (or +8 h). Calendar days show a green check (completed) or gold mark (still in). Month "Time Log" lists punches, durations and an OVERTIME tag
  (longer than the overtime threshold in Settings). "Add punch" dialog handles all six types (a time long before the scheduled start belongs to the next day).
- **Reminders** (`ShiftReminders.reschedule`, at app start, on every shift edit and at boot): next 45 days, 15 min before a scheduled shift (only if no punch
  exists) and at shift end (only if still timed in), each with a TIME IN / TIME OUT action.
- Known caveat: before the six-type change the app treated a Mid on date D as starting at 23:00 on D (a day late). Existing time logs for Mid shifts may sit one
  day off on the calendar; the user was told to delete and re-add them if it matters.

### 6.6 Gym
Several routines (accordion cards). Each routine holds exercises with sets/reps text and a per-day checkbox. Add/rename/delete routine,
add/edit/delete exercise, and templates (Push, Pull, Leg, Full Body, Cardio) pre-fill a new routine.

### 6.6b Gym logging and insights
- Each exercise has a Log button: sets with weight and reps (`WorkoutLogEntity`), shows last session, a progressive-overload hint (based on a "4 x 8" style detail)
  and the PR; a new PR heaviest weight triggers a toast. Ticking an exercise also records a gym day.
- Gym header shows sessions this week, weekly streak (weeks with 2+ sessions), a rest/deload nudge (5+ sessions in 7 days, or 5+ strong weeks) and up to three
  suggested training days from the shift schedule (`Stats.suggestGymDays`: most free waking time, avoids recovery after a night shift).

### 6.6c Stats tab ("Insights", 7th tab) and Settings
- Weekly recap (quests + gold, hours worked, gym days, grocery spend), 12-week heatmap (quests + worked shifts + gym days), pay estimate (week, month so far,
  month estimate, overtime, night hours), punctuality (on-time %, average arrival, average shift length, on-time and in/out streaks), leave balances, weekly time budget.
- All numbers come from `util/Stats.kt` (pure functions). User numbers (hourly rate, overtime threshold and multiplier, night differential %, grace minutes, leave
  allowances, sleep hours, currency symbol) live in `util/Settings.kt` (SharedPreferences) and are edited in the Settings dialog on this tab.
- Pay is an estimate only (no tax, allowances or holiday rules).

### 6.6d Undo
`CommandViewModel.offerUndo(message, restore)` shows a 7-second wooden bar with UNDO (`UndoBar` in MainActivity). Covered: quests, cleared quests, categories, rewards,
grocery items, exercises, routines (with exercises), punches. Restoring re-inserts the same rows (ids are preserved).

### 6.7 Widgets (home screen)
- `CozyWidget` (label "Cozy Quests", 2x2): today's pending quests with a colored marker by difficulty and a done count.
- `GroceryWidget` ("Cozy To Buy", 2x2): items not yet bought.
- `ShiftWidget` ("Cozy Shifts", 4x2): next/current shift with countdown, next two shifts, status and the **TIME IN / TIME OUT** button
  (broadcast to the provider, handled in `ShiftWidget.onReceive` with `goAsync`).
- Each provider has `suspend fun updateNow(context)`; `Widgets.refreshAll(context)` is called from the ViewModel after any relevant change.
  Tapping a widget opens the app on the matching tab. `updatePeriodMillis` is 30 min (system minimum).
- **RemoteViews limits (this caused a real bug):** widget layouts may only use FrameLayout, LinearLayout, RelativeLayout, GridLayout,
  TextView, Button, ImageView, ImageButton, ProgressBar, Chronometer, TextClock, AnalogClock (plus list/adapter views). A plain
  `<View>` makes the whole widget fail with "Problem loading widget". Use an empty `LinearLayout` with a background as a divider.
  No Compose, no custom views. Backgrounds are nine-patch PNGs.

## 7. Design system (keep it consistent)

- **Palette** (`Theme.kt`): canvas `#EED9B7`, parchment `#F7E6C4`, parchment dark `#E4CFA9`, wood `#6B4423`, wood dark `#3B240E`, cedar
  `#9C6B3E`, gold `#E5A93B`, green `#5B8C3E`, terracotta `#C86D51`, text `#2C1E11` / secondary `#6E553F`.
- **Fonts:** Jersey 15 for body, numbers and labels (clear digits). Pixelify Sans Bold for big titles only, because its `5` looks like
  `S`. Both are OFL and bundled as static TTFs in `res/font` (Pixelify's variable font was instanced with `fontTools.varLib.instancer`).
  Jersey has one weight; bold is synthesized. The user mentioned liking "NT Brick Sans" or "Techpixs" (license unchecked, files not available to the AI).
- **No emoji, no Material icons.** Icons are `Sprites.*` drawn by `PixelIcon` (grid of characters mapped to colors, each cell a rect).
  Sprites come from `tools/sprites.py`; to regenerate `PixelSprites.kt` print each grid as `val name = Sprite(listOf("..."))`.
- **Textures:** seamless PNG tiles `tex_*` and decorations `deco_*` in `res/drawable-nodpi`, loaded once (`rememberTextures`, `inScaled=false`)
  and drawn with `drawImage(filterQuality = None)` in repeated tiles, one art pixel = 3 dp (`pxScale(3.dp)`). Use `tiledBackground`.
- **Panels:** `panelChrome` draws outline, textured frame ring, inner dark line, textured interior, corner rivets, optional overlay.
  `PanelLook(frame, interior, ring, studs)`; default is wood frame + parchment, Gym uses a stone frame. `StardewPanel`/`PanelBox` use `LocalPanelLook`.
- **Backgrounds per screen** (`ScreenKind` -> `screenBackground`): Home grass + hanging vines, Quests tilled soil, Shop teal wallpaper,
  Market cork board in wood frame, Shifts dark wood, Gym cobblestone + side vines. `VineOverlay` draws vines over the signboard edges.
  Any text placed directly on a textured background must sit on a tag: `SectionLabel` (wood sign) or `NoteTag` (parchment note).
- **Quest cards:** Easy = leafy frame + beige interior + moss corners; Medium = golden frame + honey interior + dripping honey strip;
  Hard = dark rock frame + calm lava interior. Keep contrast calm so dark text stays readable.
- Details matter to this user ("the devil is in the details"): rivets, nails on tags, pins on notes, the sleeping cat/sprout footer, bevels. Do not use plain boxes, and do not overuse textures.
- Dialogs: `StardewDialog` (scrollable content, Cancel + confirm). Inputs: `StardewTextField` (BasicTextField, compact).

## 8. State of the visual direction and roadmap

**Art integration status (batch 1 from the user is wired in).** `ui/theme/Textures.kt` `loadArt()` looks up `art_*` files in `res/drawable-nodpi` by name at runtime
(`getIdentifier`), so anything missing falls back to the built-in drawn art, and new files can simply be dropped in. Delivered and used: `art_panel_parchment`,
`art_panel_stone` (9-slice panels), `art_card_easy/medium/hard` (quest cards, scaled by height), `art_banner`, `art_honey` (strip over medium cards), `art_dock`
(stretched), `art_cat`, backgrounds `art_bg_home/gym/shop/market` (tiles) and `art_bg_quests` (scene, centre-cropped). `tools/process_batch1.py` made them
(magenta chroma key, crop, WebP). `bg_gym` and `bg_market` were not seamless and were cross-faded (slight ghosting); regenerating truly seamless tiles would look better.

**Still missing from the requested art list (the app uses built-in art for these):** `bg_shifts` (dark wood tile), `vine_tl`, `vine_tr`, `vine_side_l`, `vine_side_r`,
`button_wood`, `button_square`, `tag_parchment`, `tag_wood`, `sign_label`, `field_inset`, `coin_pouch`, `dock_selected`, `note_paper`, `pin`, `checkbox_empty`,
`checkbox_checked`, and the three 1024x1024 icon sheets (4x4 cells on magenta). Optional extras: app icon (1024x1024), mood faces. To hook one up, add a field to
`ArtAssets`, load it in `loadArt`, and use it where the matching drawn piece is built (the fallback must stay).

- The user generated mockup art with an image AI (grass + vines, stone gym wall, studded parchment, honey drips, lava rock; plus an
  "Adventure Log / Guild Shop" style with scroll-shaped panels). Current app art is procedural and chunky-pixel; they want a more
  **modern pixel** feel with smoother detail and depth, non-rectangular boxes, and flowing honey.
- Agreed plan: the user will send their own PNG assets later; the app should then load them (9-slice drawing) with the current
  procedural art as fallback. Requested batches (file names exact, transparent PNG, no baked text):
  - Batch 1: `panel_parchment`, `panel_stone`, `card_easy`, `card_medium`, `honey_drips`, `card_hard`, `banner_plaque`, `bg_home`, `bg_gym`, `dock_bar`.
  - Batch 2: `bg_quests`, `bg_shop`, `bg_market`, `bg_shifts`, `vine_tl`, `vine_tr`, `vine_side_l`, `vine_side_r`, `button_wood`, `button_square`,
    `tag_parchment`, `tag_wood`, `sign_label`, `field_inset`, `coin_pouch`, `dock_selected`, `note_paper`, `pin`, `checkbox_empty`, `checkbox_checked`.
  - Batch 3: three 1024x1024 icon sheets (4x4 cells of 256 px on magenta) + `cat_sleeping`. Slice, remove magenta, fix tile seams.
- Alternative the AI offered: render smoother assets in code (`tools/art.py` sample exists). Open question to the user: smooth finish, or smooth detail with a fine pixel grid?
- Other open items: light vs dark theme (AI recommends keeping light parchment panels with warm dark surroundings, optional night variant),
  a UI to review/forget learned grocery categories, widget tap-to-complete, a settings screen (reminder toggle).

## 9. Lessons learned and pitfalls

1. **Widgets:** only RemoteViews-allowed classes (section 6.7). Update widgets from a coroutine with `goAsync()`, and wrap in try/catch.
2. **Compose scoping:** `Modifier.weight` only inside Row/Column scope; a parameter named `items` shadows `LazyListScope.items` (rename it, e.g. `groceries`);
   variables declared inside a `Column { }` lambda are not visible after it (this broke a dialog default once).
3. **Smart casts:** after `a?.b != null` the compiler does not reliably smart-cast `a.b`; copy to a local `val` first.
4. **java.time needs minSdk 26** (or desugaring). Epoch days are used everywhere for dates; minutes-after-midnight for times.
5. **Mid shift crosses midnight**: attribute it to the day it starts (`shiftDay`); end = start + 8 h.
6. **Notifications/alarms:** Android 13 needs the POST_NOTIFICATIONS permission (requested on launch); exact alarms can be denied on 12+, so
   fall back to `setAndAllowWhileIdle`. Alarms are lost on reboot: quest alarms are not restored, shift reminders are (`BootReceiver`).
7. **Fonts:** Pixelify's digits are ambiguous (5/S). Always check digits when choosing a font.
8. **Pixel art in Compose:** avoid bitmap filtering (`FilterQuality.None`), snap scales to integer pixels, tile textures from the composable origin.
9. **Room:** see section 5. Kapt is slow; keep entities simple.
10. **Voice input** uses the system recognizer intent, so no RECORD_AUDIO permission is needed; catch the exception when no recognizer exists.
11. Earlier AI-written files (Gemini/DeepSeek) used package `cozyassistant` and Material icons; all of it was rewritten to this structure.
12. The user's first full build worked on their phone; later rounds were visual. They test on a real device and send screenshots: read them carefully
    (they revealed the `5`/`S` font problem and the widget failure).

## 10. Checklists

**Before delivering a zip** (run from the project root; these are the checks used so far):
```bash
# balanced brackets per Kotlin file (strings stripped)
for f in $(find app/src -name '*.kt'); do python3 - "$f" <<'PY'
import sys,re
s=open(sys.argv[1]).read(); t=re.sub(r'"(\\.|[^"\\])*"','""',s)
for a,b in ['()','{}','[]']:
    if t.count(a)!=t.count(b): print(sys.argv[1],'unbalanced',a)
PY
done
# every Sprites.x / R.id.x / R.drawable.x reference must exist; every XML must parse; widget layouts must use allowed classes only
```
Also run `tools/callcheck.py` (named arguments / missing parameters against project function signatures), and scan every file for a used-but-not-imported
symbol (a missing `import androidx.compose.ui.unit.dp` once broke the build: the syntax-only kotlinc check cannot see it). Also confirm: new `R.drawable`/`R.id`/strings exist, manifest entries exist for new receivers, every DAO method used exists, DB version bumped with a migration.

**Add a screen:** composable in `ui/screens`, add a `TabInfo` + `ScreenKind` entry (and a background in `screenBackground`), wire state/callbacks in `CozyAssistantApp`.
**Add an entity:** entity file, DAO, `@Database`, version bump + migration, ViewModel flow + actions, widget refresh if relevant.
**Add a widget:** provider class with `updateNow`, layout using allowed views, `xml/*_info.xml`, manifest `<receiver>` with label, call from `Widgets.refreshAllNow`.
**Visual change:** reuse `PanelBox`/`StardewPanel`, sprites and textures; keep text on tags; check contrast against the lava and honey cards.

## 11. Change log (newest last; add to it)

1. Rebuilt the simple APK into a six-tab Stardew app (Home, Quests, Shop, Market, Shifts, Gym), Room DB, gold system, voice add, ATC shift calendar, widget.
2. Pixel icons, readable pixel font, difficulty-coloured quest cards, custom repeat, Meat/Tech sections, routines with templates (migration 4->5).
3. Textured art pass: per-screen backgrounds, framed panels, vines, honey/moss/lava quest looks.
4. Time clock + punch log + reminders (migration 5->6); widgets: quests 2x2, to-buy 2x2, shifts 4x2 with TIME IN/OUT; body font switched to Jersey 15.
5. Fixed widget loading (removed disallowed `<View>` from widget layouts), added learned grocery categories (migration 6->7), wrote this guide.
6. Six shift types with [Mid][Day][Eve] boxes and half-fill bridges; undo for deletes; pantry + quantity/price; Stats tab (pay, punctuality, leave, recap, heatmap, time budget) + Settings; workout logging, PRs, streaks, nudges, shift-aware gym suggestions (migration 7->8); batch-1 art integrated with runtime fallback.

7. Build fix: MainActivity was missing the `dp` import after the UndoBar was added.

## 12. Questions worth asking the user next

- Do they want the smooth finish or a fine pixel grid for the new art? Will they send the asset batches (section 8)?
- Should shift reminders be switchable, and at what lead time?
- Do they want a screen to review and forget learned grocery categories?
- Is the Jersey/Pixelify font mix good, or do they have licensed files for NT Brick Sans / Techpixs?
