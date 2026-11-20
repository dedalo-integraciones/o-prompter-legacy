# Guidelines for AI Agents Working on O'Prompter

This document provides essential context, architecture notes, and strict compatibility constraints for any AI agent or developer working on the **O'Prompter** Android project.

---

## 1. Project Overview & Target
- **Project Name**: O'Prompter
- **Language**: Java
- **Target Platform**: Android (Optimized and tested for **Android 4.2 / API Level 17** upwards).
- **Primary Architecture**: Native Android Activities (`AppCompatActivity` with `appcompat:1.2.0`), MultiDex enabled, Core Library Desugaring active.

---

## 2. ⚠️ Critical Compatibility Constraints (Android 4.2 / API 17)
Any future changes **must** adhere to these rules to prevent runtime crashes (*"O'Prompter se ha detenido"*) on legacy devices:
1. **Minimum SDK**: `minSdk 17` must remain configured in `app/build.gradle`.
2. **AndroidX Library Versions**: Do **not** upgrade `appcompat` beyond `1.2.0` (or versions supporting API < 19). Newer AndroidX versions (like `1.5.0+`) drop API 17 support and cause `NoClassDefFoundError` on launch.
3. **WebP Image Limitation**: Android 4.2 does not reliably decode `.webp` images in UI layouts. All splash screens or image assets loaded via XML layouts must use shape drawables (`@drawable/...`) or standard PNGs, never `.webp`.
4. **MultiDex**: The custom application class `com.dedalo.oprompter.App` extends `androidx.multidex.MultiDexApplication` to ensure secondary DEX files load correctly on Dalvik (API 17).
5. **UI Inflation & Menus**: Avoid complex `app:showAsAction="always"` text menu items in toolbars. Use dedicated layout bars (like `layoutNavigation` in `activity_main.xml`) for toolbar buttons to prevent ActionBar inflation crashes on legacy Android versions.

---

## 3. Key Source Files & Structure
- **`app/build.gradle`**: Build script (`minSdk 17`, `compileSdk 34`, `appcompat:1.2.0`, `multidex`).
- **`AndroidManifest.xml`**: Declares `android:name=".App"`, `SplashActivity` (launcher), and `MainActivity`.
- **`app/src/main/java/com/dedalo/oprompter/`**:
  - `App.java`: `MultiDexApplication` subclass, initializes vector drawable support.
  - `SplashActivity.java`: 2-second splash screen transition.
  - `MainActivity.java`: Core prompter engine, file loader/saver, alphabetical file navigation (`navigateFile`), touch auto-scroll gesture detector (`GestureDetector`), and edit mode toggle.
  - `MarkdownParser.java`: Parses `*bold*`, `_italic_`, `~strikethrough~`, `+subrayado+` into `SpannableString`.
- **`app/src/main/res/`**:
  - `layout/activity_main.xml`: Main prompter UI with unifed navigation bar (`textViewFileName`, `buttonPrevious`, `buttonNext`).
  - `layout/activity_splash.xml`: Splash screen layout using shape drawables (WebP-free).
  - `menu/main_menu.xml`: Overflow menu actions (Load from OPrompter, Load External, Edit, Save, Exit).

---

## 4. Build & Verification Commands
When modifying the codebase, always verify compilation and tests using Gradle tasks:
- **Build Debug APK**: `:app:assembleDebug`
- **Run Unit Tests**: `:app:testDebugUnitTest`
