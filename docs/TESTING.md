# Validation of version 1.7.0

- Debug build, 9 JVM tests and lint completed successfully.
- Lint: 0 errors and 64 warnings.
- Android suite: 38 tests on an AOSP API 35 emulator.
- New tests cover default/legacy backup behavior, independent per-icon settings,
  malformed mode values, original image colors and transparency, stored tint alpha,
  Material You, gradient bypass, cached bitmap reuse and the configuration switch.
- Original-color light/dark widget previews were inspected visually.
- APK version: 1.7.0 (versionCode 12); minimum API 31, target API 37.
- Debug signing certificate matches the published 1.6.1 release.

A UI test was adjusted to wait for Compose enabled-state semantics after a
switch event rather than checking before the UI update was complete.

External Google destinations still depend on installed apps and were not
retested on a physical device. Original monochrome Material vectors retain
source fill colors (normally black), so use monochrome tint when contrast is needed.

APK SHA-256:

```text
e725a6ffd792ddba81e15d3168cf4f13302331345b65e1f08d7c8471407be528
```
