# Validation

## Candidate 1.16.5 — 2026-10-16

- VersionCode 45. Release assembly/lint passed; 86 JVM tests passed. Signature and 16 KB alignment verified.
- `keepOuterOutline` defaults to false, is persisted per icon, has its own reset and follows logo/icon Light/Dark links. Unit coverage includes serialization, legacy default, independent theme values, removal of circular/rectangular uniform backgrounds and preservation of transparent symbols.
- Runtime tests on the delivered signed APK: import circular bitmap with internal cross from native picker; enable outline and monochrome; toggle Keep outer outline off/on/off and visually verify cross alone versus cross + circle; switch EN/IT; Save, force-stop/reopen and verify persisted result. No crash-buffer entries. Launcher screenshot retained.
- Background removal is conservative: detects a mostly uniform perimeter, flood-fills matching background pixels and retains a separate foreground. Ambiguous/nonuniform backgrounds retain their silhouette rather than erasing the symbol.
- Evidence in workspace work/: outer-outline-off.png, outer-outline-on.png, outer-outline-it.png, outer-outline-reopened.png, outer-outline-widget.png.
- Same APK before/after testing, no rebuild: outputs/1.16.5/MySearchWidget-1.16.5.apk; SHA-256 `f070f369ab4a024aec1be6232b31372d742bce9471632eeaf2b198ff3ef442bc`.

## Candidate 1.16.4 — 2026-10-16

- Final versionCode 43. Release assembly/lint passed. 83 JVM tests passed for the implementation, including four outline regressions (internal contours, transparent silhouette, chromatic edges and low-level noise). Build 43 additionally updates the EN/IT help text.
- Tested the delivered signed APK on API 37.1: native picker returns resource, bitmap and android.resource URI; transparent star outline; opaque circular bitmap with internal cross outline; monochrome enabled; Save, force-stop/reopen and actual launcher widget rendering. No crash-buffer entries.
- Native cancellation, malformed result and explicit internal-catalogue fallback were verified on build 42 (same implementation, older help text).
- Screenshot evidence: work/outline-final-star.png, work/outline-final-controls.png, work/outline-final-reopened.png, work/outline-final-widget.png. Tests use a controlled picker fixture, not every third-party icon pack.
- Outline traces alpha and contrasting RGB boundaries. Embedded backgrounds are not semantically removed; low-contrast details can remain faint. Original assets remain unchanged.
- Signature and 16 KB alignment passed. Final APK: outputs/1.16.4/MySearchWidget-1.16.4-build43.apk. SHA-256 unchanged after testing: `01f6ea97900c3b9d5b4238d1baf71fb1fe6d7b683ea55246f3ec9ccc1bc25d8c`.

## Candidate 1.16.3 — 2026-10-16 — runtime validation incomplete

- VersionCode 41. Release assembly and lint passed. All 79 JVM tests passed, including three tests for internal monochrome contrast, transparent edges and colored details.
- Signed APK installed on the API 37.1 emulator. Opening the external ADW picker from Appearance was verified. This also verified the fix for the localized context missing an ActivityResultRegistryOwner.
- Native return formats (resource, bitmap and URI), cancellation, fallback and visual monochrome/gradient appearance still require completion on this candidate. The fixture's action bar obscured its first controls; no successful return should be inferred from the recorded tap.
- On resuming, the emulator was no longer running and /dev/kvm was absent. Restart failed before boot (work/emulator-native-final.log). Do not describe this candidate as fully runtime-tested.
- Monochrome conversion uses luminance-dependent alpha to retain detail in flattened icons; it does not extract a foreground symbol or remove embedded backgrounds. Flat silhouettes preserve alpha. Outline still follows the image's alpha boundary.
- Signature and 16 KB alignment verified. APK SHA-256: `07fb8b93b881ecda80691c85d0d04e3a14b94ab5ae37e9962212ea3cf73b76d4`; unchanged on resumption. No rebuild after signing.

## Candidate 1.16.1 — 2026-10-15

- VersionCode 39. Release build/lint passed; 76 JVM tests passed, including external-image asset round-trip, independent action/icon theme links and retained icon styling.
- Signed release APK installed on Android API 37.1 emulator. Tested shortcut selection (Calendar/New event), expanding icon options, selecting the test ADW pack and image, saving, reopening and applying an outline gradient. No debug APK used for UI tests.
- Original Chrome app icon, direct Appearance pack browser and original-color/outline/monochrome rendering were exercised on candidate 1.16.0; the final candidate additionally has a scrollable card and transparent picker-image exterior.
- Final launcher confirmation preview visually verified: English placeholder, complete outer capsule, transparent exterior. Image is cropped from actual emulator rendering, not AI-generated.
- Pack fixture: a local resource-only APK with a vector star and res/xml/appfilter.xml. This is targeted compatibility coverage, not testing every third-party pack or private Nova icon customization.
- UI screenshots in workspace work/: launcher-preview-final.png, icon-selection-final.png, icon-gradient-final.png. No crash-buffer entries during these checks.
- Signed APK SHA-256, unchanged after runtime tests: `8bcf3141c8ed54b14aadc2ccc4e5f8be0d03529558a67f7eaf3f3007f07b87f2`.

## Candidate 1.15.4 — 2026-10-15

- VersionCode 37. Release assembly and lint passed. Header actions use one Row with 8 dp spacing.
- Installed and tested the signed release APK on the accelerated API 37.1 emulator (1080×2400, 420 dpi). No debug APK used and no rebuild after testing.
- Visually checked English/light and Italian/dark headers. Italian accessibility bounds: Home [529,149][655,275], Save [676,149][891,275], language [912,149][1038,275]. Both gaps are 21 px (8 dp), all heights 126 px (48 dp).
- Added a widget through the app and launcher confirmation. Save persisted a test placeholder after reopening configuration; Home from APPWIDGET_CONFIGURE reached the app library. Unsaved edits displayed the discard confirmation. These are targeted checks, not a full regression suite.
- Signature and 16 KB alignment passed. SHA-256 before and after UI tests: `33a36553064a98b976885e854c953e8322d3a342850ddaa509b27b506d0adb12`.
- Screenshots retained locally: work/header-light.png and work/header-dark-it.png (workspace root).

## Candidate 1.15.3 — 2026-10-15

- VersionCode 36. Release assembly and lint passed. Home, Save and language controls now explicitly share outline color, content color and 48 dp height.
- Existing signing certificate and 16 KB alignment verified. APK SHA-256: `aa99973af9d6b37575e7225ff57f994639a06c3505975861d0df4d9257270a47`.
- Runtime UI verification remains blocked: the resumed environment does not expose `/dev/kvm`. An unaccelerated API 37.1 emulator starts but remains offline without completing Android boot. No successful installation or visual/Save-navigation test is claimed for this APK. Emulator stopped after the attempt.
- The signed APK was retained unchanged for subsequent runtime testing and delivery; no separate debug APK was built.

## Candidate 1.15.2 — 2026-10-14

- VersionCode 35. Release build/lint passed; 74 JVM tests passed. No device/emulator UI tests in this iteration.
- Current preset has an empty placeholder. Reset coverage verifies automatic text restoration, preservation of the selected engine/general text style, no mutation of existing text, and use of updated defaults.
- Home/Save use outlined controls; installed version uses BuildConfig.VERSION_NAME.
- Existing signing certificate and 16 KB alignment verified. APK SHA-256: `032abcf94d444ad3ef660fde9e4eb4171d49240004fd89d78bf3254d1f80eff1`.

## Candidate 1.15.1 — 2026-10-14

- VersionCode 34. Release build and lint passed; signature and 16 KB alignment verified.
- Home icon navigation reviewed for normal app entry, launcher configuration, and discard/cancel branches. No device/emulator UI test performed in this iteration.
- APK SHA-256: `f8440806dac21292528a82fc4bba617273fb16ff956e09d979dfad56a93ff3ae`.

## Candidate 1.15.0 — 2026-10-14

- VersionCode 33. 73 JVM tests passed, including nine selective reset tests covering individual colors, gradient angle, rich-text ranges, asset restoration, linked/separate modes, haptics/actions, coordinates/layers, adaptive field width and undo.
- Release build and lint passed; existing warnings remain. One release APK generated, signed with the existing certificate and verified for 16 KB ZIP alignment. No debug APK generated.
- SHA-256: `4ba1077a55f79519dfdaeee775a5027c53011e6ea2856ee18f16cbc58fbdb97c`.
- No emulator or physical-device UI tests performed in this iteration. Manual verification of the reset cards remains pending.

## Candidate 1.14.2 — 2026-10-14

- Release build and lint passed; existing warnings remain. Signature matches the previous APK and 16 KB alignment is verified. APK SHA-256: `1f24c4c0e868f3fd8b59df5b0a9fa4c58cd84ac04c2e97fb44b3e13bb5bef9a4`.

- VersionCode 32. Horizontal launcher resizing reflows the search field while preserving height, outer margins, right-anchored buttons and manual offsets; saved configuration values are not rewritten.
- 64 JVM tests passed, including widths 220/280/320/374/391/600 dp with 0–3 buttons, invariant field margins and height, button bounds and tap regions. Tall configurations up to 256 dp also construct valid geometry at 220/280/356 dp.
- Seven DefaultWidgetTest Android tests passed on the corrected debug APK in the Android 17 QPR1 emulator, including native RemoteViews heights and rendered narrow/wide images. Eight navigation/pinning tests passed earlier in this iteration.
- Removed targetCellHeight=1 so the initial 68 dp minimum height is not overridden by a one-cell sizing hint. An insufficient vertical host allocation still scales the composition to fit.
- Physical Pixel/Nova validation remains pending; emulator rendering is not a test of Nova’s padding option on the user’s device.

## Candidate 1.14.1 — 2026-10-14

- APK SHA-256: `54fbed501c44f8976805b5d8fb0aab1a4cd925150c205a434851014da929d046`. Signature matches previous releases; 16 KB alignment verified.
- VersionCode 31. 64 JVM tests and 14 Android tests passed (DefaultWidgetTest, CardNavigationUiTest, WidgetPinningTest) on Android 17 QPR1.
- Creation adapts the preset horizontally to the initial launcher width without scaling its 68 dp height. Existing saved configurations are not rebased. Widths 280/320/356/374/400/500 dp are covered; absent IDs remain unsaved.
- Global height keeps the visible width and updates the stored ratio instead of expanding width and being fitted back down. JVM cases cover 48–256 dp, including 184.49/200/256; Android RemoteViews frame heights match 68/80/120/200 dp. The launcher’s actual height cap remains respected and is reported in the preview.
- Creation/reset defaults and automatic field width follow the initial design width. Widget provider minimum and initial frame height are 68 dp.
- Physical Pixel/Nova confirmation is still pending.

## Candidate 1.14.0 — 2026-10-13

- VersionCode 30, minimum API 31, target API 37.
- Release build and lint passed (0 errors; existing warnings remain). Signing certificate matches published updates and 16 KB ZIP alignment was verified.
- APK SHA-256: `a805f80f799f4be49c38768c4d2fab6a78f20211035aee5f0e8bb453ee0a2d3d`.
- 62 JVM tests passed. Coverage includes shared formatting across placeholders of different lengths and automatic color values when relinking.
- Theme separation is opt-in per element/property, except existing color pairs. Tests exercise shared edits, independent edits, copying the selected mode when relinking, per-element layer order, text span font/size/paint separation, haptics and strict override validation. Old presets and backups remain unchanged.
- Android 17 QPR1 (API 37.1): 14 targeted tests passed (`ThemeModesTest`, `DefaultWidgetTest`, `CardNavigationUiTest`, `UpdateInstallerTest`). Serialized RemoteViews select the entire layout and hit targets for each host theme. Backup import retains both modes and both search engines; drafts are not saved implicitly.
- Button-count layout: preset top inset (10.5 dp at 68 dp outer height / 47 dp field height) is mirrored on all automatic edges; count transitions 3→2→1→0→3 preserve field height, linked icon alignment and hidden actions. Manual dimensions/offsets survive; loading existing widgets does not migrate geometry. Field proportion locks remain valid after selecting automatic width.
- Latest Android run: 13 tests passed (CardNavigationUiTest, DefaultWidgetTest, ThemeModesTest); screenshots verified for 0/2 buttons and single-line EN/IT navigation. Widget picker uses the current preset image via both previewImage and an aspect-preserving previewLayout.
- After the linking UI refinement, all 5 CardNavigationUiTest tests passed again: keep light/dark values, cancel relinking, undo and draft-only changes. The editing selector is absent from the linking page; full Italian navigation labels were visually verified with unchanged font size and aligned icons. Manual colors and Material You retain their existing relationship.
- The opt-in network test downloaded the actual public 1.13.4 APK using Android DownloadManager, verified its SHA-256 and rejected it as an older update. A local manifest-only versionCode 31 APK, signed with the test installation certificate, was accepted and opened in Android’s native installer; installation was not confirmed. The currently installed version is also rejected as an update.
- Update integration tests use `-e networkUpdates true`. The native installer test is optional: provide a manifest-only APK with this package ID, a higher versionCode and the same signing certificate at the target app’s `files/test-update.apk`, then allow REQUEST_INSTALL_PACKAGES in the test emulator. This fixture is never a release artifact.
- Startup checks are lifecycle-bound; no app service, scheduled job or periodic update polling was added. Explicit downloads use Android’s DownloadManager. Completed downloads are checked on resume, so closing the configurator does not lose the installation offer.
- No physical-device/Nova validation was performed for this candidate.

## Candidate 1.13.4 — 2026-10-12

- VersionCode 29, minimum API 31, target API 37.
- Final release build and lintVital passed. Signature matches previous updates; 16 KB ZIP alignment verified.
- Final APK SHA-256: `df408b201390be1b8bbd43a723483b46862664be7db4b31e6bddaf4c74e84e30`.
- 45 JVM tests and 14 targeted Android instrumentation tests passed on Android 17 QPR1 (API 37.1): `DefaultWidgetTest`, `LibraryAndThemeTest`, `WidgetPinningTest`, `CardNavigationUiTest`.
- The creation preset is compared against the provided backup, with `theme` changed to `system` and the third icon’s horizontal offset centered; embedded assets are validated. Its bounds share the button’s horizontal center.
- Missing widget IDs use the preset without writing settings or changing its reference width. Existing saved widgets, sparse legacy configurations and old backups retain their values.
- Serialized RemoteViews switch between light and dark with the host configuration; both renders match the renderer. The README image is the actual dark rendering of the preset.
- Dimension/position resets use the creation preset (68 dp height, 193 × 47 dp field, 12.5 sp placeholder / 15 sp query). Tests cover isolated changes, idempotence, retained formatting and links, draft-only editing, UI height reset and undo.
- A saved copy of the original supplied backup retains its dark theme and original third-icon offset after reading and launcher-size changes.
- Manual emulator check passed: Add widget → launcher confirmation → configurator showing 68 dp → Save. No physical-device/Nova test was performed.

## Candidate 1.13.3 — 2026-10-10

- VersionCode 28, minimum API 31, target API 37.
- Release build and lintVital passed. APK signature matches previous updates; 16 KB ZIP alignment verified.
- APK SHA-256: `eb4b06a0b1ec568e133d0b511c2f57a89eeda0177980d6d6b50a156bf830e488`.
- 45 JVM tests passed. Four targeted instrumentation tests passed on Android 17 QPR1 (API 37.1): `WidgetPinningTest` and `CardNavigationUiTest`.
- Pin callback checks cover the widget ID, draft-only configuration, distinct one-shot callbacks and the app home screen. Card navigation checks cover existing editing controls.
- Manual emulator launcher check: Add widget → Add to home screen → new widget configurator → Save. English Save and Italian Salva verified; relaunching the app displays the library with the saved widget.
- The final release also updates the empty-library help text to refer to the card above. The full instrumentation suite and physical-device/Nova flow were not run.

## Release 1.13.2 — 2026-10-08

- VersionCode 27, minimum API 31, target API 37.
- Release build and lintVital successful; signature matches previous updates and 16 KB ZIP alignment is verified.
- APK SHA-256: `527d9df0844bbdd5bb6f5efa1795ec56e26c6d03b76395da0d90b7b988f36709`.
- 45 JVM tests and 10 targeted Android instrumentation tests passed on the preceding 1.13.1 candidate. The final change relocates the existing button-count control to Layout without altering its data update. Instrumentation was not repeated for this relocation; the full Android suite was not run.
- Android test classes: `CardNavigationUiTest`, `EditingUpgradeUiTest`, `AutomaticPlaceholderTest`, `WidgetViewportTest` and `WidgetHeightTest`.
- Coverage includes grouped-card navigation, dimension/position controls and resets, per-area haptic settings without committing the draft, undo/redo, automatic/manual preview themes, proportional rendering and RemoteViews touch geometry.
- Automatic placeholder tests cover built-in/custom engine names, English/Italian, Google-app search, custom-text precedence and unchanged formatting. Bitmap equality checks automatic versus identical explicit text with formatting.
- Visual checks covered Italian/English and font scale 1.3. Navigation labels stay on one line with ellipsis when necessary; accessibility retains the full label.
- Nova Launcher 8.9.2 was exercised on an Android 17 QPR1 / API 37.1 emulator at 1080×2404 / 390 dpi. Home screenshots and accessibility bounds were compared with padding on/off (374 and 391 dp): sizes, spacing and hit areas scale together. Restoring the test layout required completion of Nova’s pending widget setup state before exercising the regular host.
- An initial instrumentation launch was killed for low memory; the targeted tests subsequently passed. Physical haptic feedback remains to be checked on the Pixel.

## Runtime behavior

Changes remain drafts until Save. Back returns from detail pages to the section list; leaving the editor prompts before discarding unsaved changes. Button count changes preserve hidden button settings. Existing backups remain importable.

The widget uses `updatePeriodMillis="0"`. No persistent service, worker, alarm or polling loop is used. System-theme rendering relies on the launcher resolving the light/dark RemoteViews images.

A launcher reports bound widget IDs rather than its visible Home layout. Saved IDs retained by a launcher can be hidden from the library without deleting the Home widget settings.
