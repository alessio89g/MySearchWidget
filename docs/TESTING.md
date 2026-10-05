# Validation

## Release artifact

- Version: **1.12.0**, versionCode **23**, minimum API 31, target API 37.
- Release build, including `lintVitalRelease`: successful.
- APK signature verified with the same certificate used for published updates.
- APK ZIP alignment checked with a 16 KB page size.
- SHA-256: `16fa4a5f855386f84e49a1979681bd47d511455c3276b8aa024761790133d154`.

## Automated checks

- **40 JVM tests passed**, with zero failures or errors. Coverage includes backup validation, default settings, dimension limits, aspect ratios, stable element anchors, linked and independent movement, offscreen clipping, layer order preservation of child geometry when unlinking a small field, and independent resets of dimensions and positions.
- The release lint check (`lintVitalRelease`) completed successfully.
- The Android suite has **not been executed on a device or emulator for this release**.
- Instrumentation coverage includes editor confirmation, layout controls, rendering and RemoteViews geometry, configuration language, search modes, shortcuts, library selection and theme behavior.

## Device checks

Verify appearance and touch order with overlapping elements, coordinates outside the widget, linked/unlinked movement, launcher resizing, and switching system light/dark mode. Confirm that Add/Save applies a draft while Cancel/Back discards it after confirmation. Check the separate dimension and position reset buttons for each element, including linked elements and formatted text. Check backup import/export on the target device.

The widget uses `updatePeriodMillis="0"`. It does not use a persistent service, worker, alarm or polling loop. System-theme rendering relies on the launcher resolving the light/dark RemoteViews images.

A launcher reports bound widget IDs rather than the visible Home layout. Saved IDs retained by a launcher can be hidden from the library without deleting the Home widget settings.
