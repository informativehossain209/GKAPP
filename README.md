# 📱 ঘর খরচ (Ghor Khoroch) — Android APK

> **Web App → Native Android APK wrapper**
> Built with ❤️ by **SAKIB HOSSAIN**

Ad-free, completely free Android APK for [g-happk.vercel.app](https://g-happk.vercel.app/)

---

## ✨ Features

- ✅ Splash screen with developer branding
- ✅ No advertisements
- ✅ Back button navigation works
- ✅ Full screen (no action bar)
- ✅ File upload / camera support
- ✅ WhatsApp image sharing bridge
- ✅ Gallery save support
- ✅ Auto-build via GitHub Actions

---

## ⚡ Easiest Method — Auto Build with GitHub

### Step 1 — Clone or Upload this repo

Create a new GitHub repository and upload all these files.

### Step 2 — Create GitHub Repository

1. Go to **github.com** → New Repository → name it: `ghappk-android`
2. Keep it **Public** (Private repos need Actions enabled separately)

### Step 3 — Upload Code

```bash
cd ghappk-android
git init
git add .
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/ghappk-android.git
git push -u origin main
```

### Step 4 — Download APK

After pushing, wait **5–7 minutes**, then:

```
Your Repository → Actions tab → latest workflow run
→ "GhorKhoroch-APK" artifact → Download
```

---

## 📲 Install APK on Phone

1. Extract the ZIP → get `GhorKhoroch.apk`
2. Send to phone (WhatsApp / USB / Google Drive)
3. Phone Settings → **"Unknown sources"** or **"Install unknown apps"** → Enable
4. Tap the APK → Install

---

## 🔄 How to Update the App

Just change files and push to GitHub:
```bash
git add .
git commit -m "update"
git push
```
**A new APK will be ready in 5–7 minutes.**

---

## 🌐 Web App URL

The APK wraps this URL:
```
https://g-happk.vercel.app/
```
To change it, edit line in `app/src/main/java/com/ghappk/app/MainActivity.java`:
```java
private static final String APP_URL = "https://g-happk.vercel.app/";
```

---

## 🎨 Splash Screen

When the app opens, users see a branded splash screen for **3 seconds** featuring:
- App logo (replace `mipmap-*/ic_launcher.png` with your logo)
- App name **ঘর খরচ (Ghor Khoroch)**
- Developer credit: **✨ SAKIB HOSSAIN ✨**

---

## 📁 Project Structure

```
GHAPPK-APP-main/
├── .github/workflows/build.yml          ← Auto APK builder
├── app/src/main/
│   ├── java/com/ghappk/app/
│   │   ├── SplashActivity.java          ← 👈 Splash screen (3s)
│   │   └── MainActivity.java            ← 👈 WebView (URL here)
│   ├── res/
│   │   ├── drawable/splash_background.xml ← Gradient background
│   │   ├── layout/activity_splash.xml   ← Splash screen layout
│   │   ├── mipmap-*/                    ← 👈 Replace with your logo
│   │   └── values/strings.xml           ← App name
│   └── AndroidManifest.xml
├── app/build.gradle                     ← Package: com.ghappk.app
├── build.gradle
├── settings.gradle
└── gradlew
```

---

## 👨‍💻 Developer

**SAKIB HOSSAIN**
Full Stack Developer

---

## 📄 License

Free to use and modify for your own web app wrapper.
