# StarFloat — Corrected GitHub APK Build Project

This ZIP contains the complete Android project. Extract/copy **the contents of this ZIP directly into the root of your GitHub repository**. The repository root should contain `settings.gradle`, `build.gradle`, `gradle.properties`, `app/`, and `.github/` — do not leave an extra wrapper folder around them.

## Build the APK on GitHub
1. Commit and push the files to `main` or `master`.
2. Open **Actions → Build StarFloat APK → Run workflow**.
3. When the run succeeds, open the run summary and download the `StarFloat-debug-apk` artifact. It contains `app-debug.apk`.

## Required resource files included
- App icons in all standard mipmap density folders
- `app/src/main/res/values/styles.xml` defining `AppTheme`
- `app/src/main/res/values/strings.xml` defining the accessibility description
- `app/src/main/res/xml/accessibility_config.xml`

Package: `com.starfloat.overlay` · Version `1.0` · Java 17 · Android SDK 35.

The app uses overlay and accessibility features. Enable only the permissions/features you understand and intend to use on your device.
