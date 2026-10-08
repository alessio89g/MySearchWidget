# Validation

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

Changes remain drafts until Add/Save. Back returns from detail pages to the section list; leaving the editor prompts before discarding unsaved changes. Button count changes preserve hidden button settings. Existing backups remain importable.

The widget uses `updatePeriodMillis="0"`. No persistent service, worker, alarm or polling loop is used. System-theme rendering relies on the launcher resolving the light/dark RemoteViews images.

A launcher reports bound widget IDs rather than its visible Home layout. Saved IDs retained by a launcher can be hidden from the library without deleting the Home widget settings.
