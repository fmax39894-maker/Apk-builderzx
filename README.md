# Detector Pro

An Android WebView browser with a clean white interface, Google as its home/search page, a private-session mode, official YouTube website playback, basic direct-video URL detection for supported other sites, and Android DownloadManager integration.

## Important limitations

- This is a starter project, not a clone of any other downloader app.
- YouTube opens in the built-in browser for official website playback. Detector Pro does not extract or download YouTube streams.
- Detection is best-effort on other sites. It recognises common direct file URLs (MP4, WebM, M4V, 3GP, MOV) and video elements exposed by the webpage.
- It will not reliably detect every site's segmented HLS/DASH streams, encrypted media, DRM-protected content, or media hidden behind authentication.
- It does not bypass DRM, paywalls, login checks, website restrictions, or access controls. Download only content you own or are authorised to save.
- "Private session" is a privacy-oriented WebView configuration, not a guarantee of anonymity. Websites, your network provider, and Google may still see requests. Session cookies are enabled for site compatibility (including YouTube) and the app clears cookies, cache, and history when it closes. Android WebView may retain some site data outside the app's direct control.
- The app uses Android's DownloadManager and saves files in the public Downloads folder.

## Build on GitHub using an Android phone

1. Download and extract `detector-pro.zip` on your phone.
2. Open GitHub in Chrome and sign in.
3. Create a new repository named `detector-pro`. Set it to **Public** or **Private** as you prefer.
4. In the repository, choose **Add file → Upload files**.
5. Upload the contents of the extracted `detector-pro` folder (upload the folders and files, including `.github`). Do not upload the outer ZIP.
6. Commit the upload to the `main` branch.
7. Open the repository's **Actions** tab.
8. Select **Build Detector Pro APK** and tap **Run workflow**. If the workflow already started after your commit, open that run instead.
9. Wait for the green check mark. Open the completed run, scroll to **Artifacts**, and download `detector-pro-debug-apk`.
10. Extract the downloaded artifact ZIP. Install `app-debug.apk` on your Android phone. If Android asks, allow Chrome or your file manager to install apps from that source.

## If GitHub does not show `.github`

Some mobile file pickers hide folders beginning with a dot. If the `.github/workflows/build-apk.yml` file is missing after upload, create those folders in GitHub's web interface:
- Create `.github/workflows/build-apk.yml`
- Paste the workflow contents from this project
- Commit the file, then run the workflow from Actions.

## App settings

- Home page: Google (`https://www.google.com/`)
- YouTube: official website playback in the built-in browser; no YouTube downloading.
- New browsing session: Google opens by default; no tabs are persisted; cookies/cache/history are cleared at launch and exit.
- YouTube: official website playback only; no YouTube stream downloading.
- Embedded HTML5 players: recognised on supported, age-appropriate sites. A player being detected does not mean the site provides a downloadable direct file. The Download button's Show video link option only appears for a supported direct video URL.
- Supported direct video extensions: `.mp4`, `.webm`, `.m4v`, `.3gp`, `.mov`

## Requirements

- Android Gradle Plugin 8.6.1
- Gradle 8.7 or newer (GitHub runner's installed Gradle is used by the workflow)
- JDK 17
- Android SDK Platform 35
