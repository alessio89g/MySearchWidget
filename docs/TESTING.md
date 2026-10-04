# Validation

## Release artifact

- Version: **1.11.0**, versionCode **21**, minimum API 31, target API 37.
- Release build, including `lintVitalRelease`: successful.
- APK signature verified with the same certificate used for published updates.
- APK ZIP alignment checked with a 16 KB page size.
- SHA-256: `f0275f04e2f11e496063bb3e8bb80a48d2c90318d865666b95516d8a4a37c116`.

## Automated checks

- **37 JVM tests passed**, with zero failures or errors. Coverage includes backup validation, default settings, dimension limits, aspect ratios, stable element anchors, linked and independent movement, offscreen clipping, layer order and preservation of child geometry when unlinking a small field.
- Debug lint completed without errors (85 warnings and one hint). The release lint check also completed successfully.
- Android instrumentation sources compile. The Android suite has **not been executed on a device or emulator for this release**; compilation is not a substitute for device validation.
- Instrumentation coverage includes editor confirmation, layout controls, rendering and RemoteViews geometry, configuration language, search modes, shortcuts, library selection and theme behavior.

## Device checks

Verify appearance and touch order with overlapping elements, coordinates outside the widget, linked/unlinked movement, launcher resizing, and switching system light/dark mode. Confirm that Add/Save applies a draft while Cancel/Back discards it after confirmation. Check backup import/export on the target device.

The widget uses `updatePeriodMillis="0"`. It does not use a persistent service, worker, alarm or polling loop. System-theme rendering relies on the launcher resolving the light/dark RemoteViews images.

A launcher reports bound widget IDs rather than the visible Home layout. Saved IDs retained by a launcher can be hidden from the library without deleting the Home widget settings.
