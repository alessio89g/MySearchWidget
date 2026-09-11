# Build MySearchWidget

Requirements: JDK 17, Android SDK Platform 37, Android SDK Build-Tools 37.0.0,
and Android SDK Platform-Tools. Minimum runtime: Android 12 (API 31).
The Gradle wrapper selects Gradle 9.3.1; dependencies are pinned in the build files.

1. Open this repository in Android Studio, or install the SDK packages with SDK Manager.
2. Configure your SDK location with `ANDROID_HOME` or a local, untracked
   `local.properties` file containing `sdk.dir=/absolute/path/to/Android/Sdk`.
3. From the repository root, run:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Windows: use `gradlew.bat` instead. The APK is written to
`app/build/outputs/apk/debug/app-debug.apk`.

To run device tests, connect an Android device or start an emulator, then run:

```sh
./gradlew connectedDebugAndroidTest
```

A local debug build uses your local Android debug signing key. No signing key
or password is included in this repository. A different certificate cannot
update an existing installation signed by the release author's key.
Export widget backups before any uninstall required to change certificates.

See [validation notes](TESTING.md) for the scope of the existing checks.
