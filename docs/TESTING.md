# Validation

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
