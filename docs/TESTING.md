# Validation of version 1.6.1

The Android suite was rerun after updating Google Sans to the official OFL
14.000 Android static build. The debug build and lint passed. The 7 JVM
unit tests remain passing (Gradle reused their unchanged results). Adding
license and credit text files afterward required only APK repackaging.

| Check | Result |
| --- | --- |
| Debug APK, unit tests and lint build | Successful |
| JVM unit tests | 7 passed; 0 failures; 0 errors |
| Android instrumentation tests | 33 passed on an AOSP API 35 emulator |
| Lint | 0 errors; 62 warnings |
| APK version | 1.6.1 (versionCode 11) |
| Minimum / target API | 31 / 37 |

Covered areas include backup validation, language switching, formatting,
widget integration, wallpaper access, temporary launcher access and search modes.

The AOSP emulator does not include the Google app. Exact keyboard/focus behavior
inside Google and external shortcut destinations still require testing with the
installed app versions on a physical device. Launcher sizing and palette refresh
behavior can vary by launcher and manufacturer.

The release APK is debug signed. Its SHA-256 is:

```text
05760279f9d5a6bae29d919a84817f176d92a3afb058139663dd0d99e0991dd7
```
