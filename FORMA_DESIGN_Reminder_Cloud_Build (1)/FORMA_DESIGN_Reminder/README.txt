FORMA DESIGN – SMART REMINDER
================================

This is an Android Studio source project for an offline reminder app.

Features:
- Add task title, date, time and optional message
- Schedules Android alarms
- Displays an ongoing alarm notification with STOP action
- Uses the phone's default alarm ringtone
- Stores reminders locally
- Reschedules future reminders after device reboot
- Mark tasks done/undo and delete

IMPORTANT:
- This is the Android source project, NOT a compiled APK.
- APK compilation requires Android Studio, Android SDK Platform 35, and JDK 17.
- Open this folder in Android Studio, allow Gradle sync, then choose:
  Build > Build Bundle(s) / APK(s) > Build APK(s)
- Android Studio will show the APK location after a successful build.
- On the phone, allow notifications and exact alarms. Some phone brands also require
  allowing the app to run in the background / disabling battery optimization.
- Android APKs do not install on iPhones.
- Alarm behavior can vary by Android version and manufacturer power-saving rules.


NO ANDROID STUDIO? CLOUD BUILD USING GITHUB
============================================
1. Create/sign in to a GitHub account.
2. Create a NEW repository and upload the contents of this ZIP into it.
   Make sure the .github/workflows/build-apk.yml file is included.
3. Open the repository's Actions tab.
4. Select "Build FORMA DESIGN Reminder APK".
5. Press "Run workflow".
6. Wait for the workflow to finish successfully.
7. Open the completed workflow run and download the artifact named
   "FORMA-DESIGN-Reminder-APK".
8. Extract the downloaded artifact; it contains app-debug.apk.
9. Transfer app-debug.apk to your Android phone and install it.
This requires internet and a GitHub account. The build runs on GitHub's servers.
