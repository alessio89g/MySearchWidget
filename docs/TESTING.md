# Validation of version 1.9.0

- Debug APK: version 1.9.0, versionCode 15; minimum API 31, target API 37.
- 11 JVM tests passed.
- Final build and lint completed: 0 errors and 67 warnings.
- Complete Android suite: 49 tests passed on an AOSP API 35 emulator.
- New coverage checks saved/bound library filtering, side-effect-free default reads, atomic pin confirmation, bulk template deletion, hidden widget persistence, and retention of unselected entries and active Home settings.
- UI coverage checks long-press selection, selecting a second template, cancelling the confirmation, and deleting only the selected templates. All 6 configuration UI tests passed again after the final presentation refinements.
- Theme tests serialize the RemoteViews and apply the same object in light, dark and light host contexts without provider updates. They cover both Material You and manual colors, and fixed Light/Dark modes.
- Manual Launcher3 check: pin a default widget through the app, return Home, terminate the MySearchWidget process with `am kill`, and switch system night mode on and off. Screenshots show the widget following both switches; `pidof` confirms the app process stays absent.
- The widget still uses `updatePeriodMillis="0"`; no service, alarm, worker or polling was introduced. Theme switching does not use a configuration-change receiver.
- Debug signing certificate matches 1.8.1, and installation as an update succeeds.

Android reports bound widget IDs rather than the launcher's visible layout. Saved IDs retained by a launcher cannot be automatically identified as abandoned; their library entries can be hidden manually without discarding the Home settings. Manufacturer-specific launcher behavior still needs confirmation on the user's device.

APK SHA-256:

```text
a552bf15a55b70d117ff86ba1f2d2e48f7d6f8eb5f8d5b7de573fade67d02f3b
```
