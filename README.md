# Mono Video 2.0

A minimal black-and-white Android WebView browser with a Home tab, Downloads tab, and floating Video Tool.

## Features
- Built-in web browsing and search.
- Bottom navigation for Home and Downloads.
- Floating Video Tool lists detected direct video-file URLs on the current page.
- Download buttons use Android DownloadManager and save supported files to the device Downloads folder.
- Monochrome interface.
- Session cookies and WebView browsing data are cleared when the app closes.

## Download limitations
The app only offers downloads for supported direct video files (MP4, WEBM, M4V, 3GP, MOV). It does not extract streams, bypass paywalls, DRM, login controls, or platform restrictions. For services such as YouTube, use their official playback and download features where available. Only save content you own or have permission to download.

## Build APK with GitHub Actions
1. Upload the contents of this project folder to the root of a GitHub repository.
2. Open the repository's **Actions** tab.
3. Select **Build Mono Video APK** and run the workflow, or push a commit to `main`/`master`.
4. Open the completed workflow run and download the `mono-video-debug-apk` artifact.

The workflow builds `app/build/outputs/apk/debug/app-debug.apk` using Java 17, Gradle 8.7, and Android Gradle Plugin 8.6.1.
