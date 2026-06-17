# OTA (Over-the-Air) Updates

ConstructPro AI is a **native Kotlin/Jetpack-Compose app**, so it cannot use Expo / React-Native
JS-bundle OTA. Instead it ships two real native update mechanisms, both implemented under
`app/src/main/java/com/constructionmanager/update/`:

1. **Google Play In-App Updates** — used automatically when the app is installed from Google Play.
   Wired in `MainActivity` (flexible flow + "Restart to install" prompt) via
   `com.google.android.play:app-update`.
2. **Self-hosted APK channel** — for sideloaded / internal distribution. The app polls the JSON
   manifest below, and if a newer `versionCode` is published it downloads the APK with the system
   `DownloadManager` and hands it to the platform installer through a `FileProvider`.

The in-app **Updates** screen (Dashboard → *Updates*, or Settings → *App Updates*) drives the
self-hosted flow and lets the user point at any manifest URL (production, staging, or a LAN box).

## The manifest (`ota/update.json`)

```json
{
  "versionCode": 2,                       // must be > the installed BuildConfig.VERSION_CODE to offer an update
  "versionName": "1.1",
  "apkUrl": "https://.../app-release.apk", // direct download URL of the signed release APK
  "releaseNotes": "What changed in this build.",
  "mandatory": false,                      // true → user cannot skip
  "minSupportedVersionCode": 1,            // installs below this are forced to update
  "sizeBytes": 18874368,                   // optional, for display
  "sha256": null                           // optional integrity hint
}
```

The app reads this from `UpdateConfig.DEFAULT_MANIFEST_URL`
(`https://raw.githubusercontent.com/tywade1980/ngbp-v2-0/main/ota/update.json`) by default.

## Publishing a new release

1. Bump `versionCode` (and `versionName`) in `app/build.gradle.kts`.
2. Build and sign the release APK:
   ```bash
   ./gradlew :app:assembleRelease
   ```
3. Upload `app-release.apk` to a download host (e.g. a GitHub Release attachment) and copy its
   direct download URL.
4. Edit `ota/update.json`: set the new `versionCode`/`versionName`, point `apkUrl` at the uploaded
   APK, and write `releaseNotes`. Commit to `main`.
5. Installed apps pick up the change on their next update check (automatic on launch, or via the
   Updates screen). Existing installs whose `versionCode` is lower than the manifest's are offered
   the update; anything below `minSupportedVersionCode` is required to take it.

> Keep `versionCode` in `update.json` equal to the shipping build's `versionCode` between releases
> so current installs correctly report "up to date".
