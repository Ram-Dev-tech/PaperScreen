# Phase 9J: PaperScreen System App Launcher & App Drawer

## Checklist
- [x] Configure AndroidManifest.xml for ACTION_MAIN and CATEGORY_HOME.
- [x] Create `LauncherItem` sealed interface for external apps and internal PaperApps.
- [x] Create `LauncherRepository` using `LauncherApps` to detect installed apps.
- [x] Implement real-time app list refresh using `LauncherApps.Callback`.
- [x] Implement filtering to remove PaperScreen itself from the external app list.
- [x] Create `IconEngine` in launcher package to extract original app icons.
- [x] Make sure original app icons are NOT filtered with PaperScreen colors.
- [x] Create `AppLauncherViewModel` for managing state and search queries.
- [x] Create `AppLauncherScreen` UI using `LazyVerticalGrid`.
- [x] Integrate `AppLauncherScreen` into `LauncherPager`.
- [x] Write JVM tests for launcher model sorting, filtering, and separation.
- [x] Update Navigation to support the Settings destination.
- [x] Compile, build, and test successfully.
- [x] Perform final manual validation (marked as NOT TESTABLE due to no Android device attached).

## Implementation Status
Done.
