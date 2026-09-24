# ARKITAK — Flutter native rewrite: setup notes

Read this before running `flutter pub get` / building. This sandbox has no
Flutter SDK and no internet access, so **none of this has been compiled or
run** — it's written carefully by hand, but treat the first build as a real
first build, not a formality.

## 1. Create the project shell

This zip is the `lib/` + native pieces only, not a full `flutter create`
scaffold (Gradle wrapper, Xcode project, etc. — those are generated files
specific to your machine/SDK version). Steps:

```
flutter create --org com.arkitak arkitak
```

Then copy this zip's `lib/` folder over the generated one, and `pubspec.yaml`
over the generated one.

## 2. Wire in the native audio session

- `android_native/MainActivity.kt` → move its **contents** into
  `android/app/src/main/kotlin/<your/package/path>/MainActivity.kt`
  (the one `flutter create` generated). Change the `package` line to match
  your real `applicationId` from `android/app/build.gradle`.
- `ios_native/AppDelegate.swift` → replace the generated
  `ios/Runner/AppDelegate.swift` with this one (or merge if you've already
  customized it).

## 3. Permissions

**Android** — add to `android/app/src/main/AndroidManifest.xml`, inside `<manifest>`:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

**iOS** — add to `ios/Runner/Info.plist`:
```xml
<key>NSMicrophoneUsageDescription</key>
<string>ARKITAK listens continuously so it can hold a natural, real-time conversation.</string>
<key>NSSpeechRecognitionUsageDescription</key>
<string>ARKITAK uses speech recognition to understand what you say.</string>
```
Also enable the **Background Modes → Audio** capability in Xcode if you want
the mic session to survive backgrounding.

## 4. What's implemented vs. what's next

Implemented:
- Full particle-orb physics + rendering, ported 1:1 from `main-orb.js`
  (`lib/orb/`) — same states, same colors, same boot assembly.
- Voice state machine (`lib/voice/voice_controller.dart`) — boot → idle →
  listening → thinking → speaking, barge-in, "error"/"disable" test
  keywords. Same logic as the web build's `app.js`.
- Always-on, echo-cancelled duplex audio session (native Kotlin/Swift) —
  the actual fix for the "Google Assistant click on/off" feel.

Not implemented yet (same as the web build — UI-first, on purpose):
- No LLM/backend. Recognized speech that isn't a test keyword gets a fixed
  placeholder reply ("ARKITAK is alive, my king.").
- `speech_to_text`'s `listen()` call uses a fairly standard parameter set,
  but this package's API has shifted across major versions (some moved
  `partialResults`/`cancelOnError`/`listenMode` into a `listenOptions:`
  object). If it doesn't compile against the version `pub get` resolves,
  check the installed version's signature on pub.dev and adjust
  `lib/voice/voice_controller.dart`'s `_startRecognition()` — the rest of
  the state machine doesn't depend on which shape that call takes.
- The echo-cancellation quality (Android `AcousticEchoCanceler`, iOS
  built-in voice processing) depends on the device's hardware/DSP support.
  Most real phones have it; some emulators/low-end devices don't, and will
  fall back ungracefully (the mic will just hear the speaker). Test on a
  real device before judging the duplex feel.

## 5. CI

`android.yml` (already delivered) builds a debug APK on every push via
`subosito/flutter-action`, same pattern as your Cloudihub CI/CD setup —
drop it at `.github/workflows/android.yml` once this is a real repo.
