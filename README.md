# Craft X AI Android Test (no backend)

Includes a Start/Stop home screen, foreground notification with Stop action, settings, local labeled-example storage, and an in-app draggable/lockable demo button with simulated target detection.

**Limit:** this starter does not capture/analyze Free Fire MAX, detect players in another app, or perform game actions. The training screen stores labels only; it does not train an ML model. Demo detection works only on the app's own demo screen.

## Build with GitHub Actions
1. Create a GitHub repository and upload the *contents* of this folder.
2. Open Actions and run **Build Android APK** (or push to main/master).
3. From a successful workflow, download artifact `craftx-ai-debug-apk`.
