# Validation of version 1.8.0

- Debug build and 11 JVM tests completed successfully.
- Lint: 0 errors and 64 warnings.
- Android suite: 43 tests on an AOSP API 35 emulator.
- New tests cover all shape values in backup round trips, independent per-button choices, legacy Flower values and rejection of unknown shapes.
- Android tests verify distinct solid contours, filled bounds at different sizes, cached path isolation, and rendering every shape in light/dark themes with gradients at three widget widths.
- The configuration test selects Clover for one button, checks that another stays Circle, and switches to Italian to verify **Forma pulsante**.
- The actual Android renderer gallery was inspected against the supplied references.
- APK version: 1.8.0 (versionCode 13); minimum API 31, target API 37.
- Debug signing certificate matches the published 1.7.0 release. Installation over that version succeeded on the emulator.

The new geometry test checks filled path bounds: Android `computeBounds` also includes Bezier control points, which can extend beyond the visible contour. The UI test scrolls back to the subtab before verifying its translated label.

External Google destinations still depend on installed apps and were not retested on a physical device. The screenshot-derived silhouettes are approximations; Scallop’s cropped lower tip follows its upper contour’s symmetry. Circle and Squircle retain their previous rendering.

APK SHA-256:

```text
a7e1346d2277b956d23dd2c13c43cde1e23a104642f47265d66ac60f18ce75f6
```
