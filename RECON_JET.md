# Open Headunit on Recon Jet

## Build

The tested APK came from the `recom3/open-headunit` fork at `976fa05850bfe08eb9d15201463b1ba187b10219`. The local branch is `codex/recon-jet-api16`. `origin` points to the fork and `upstream` points to `andreknieriem/open-headunit`.

Build the existing `githubDebug` variant using JDK 21, which is also used by the fork’s main Android CI workflow. The app and contract modules emit Java 8 bytecode. Gradle 8.14.4, Android Gradle Plugin 8.13.2 and Kotlin 2.4.20 are pinned in the checkout. Install these SDK packages and point `ANDROID_HOME` at that SDK.

```powershell
sdkmanager "platform-tools" "platforms;android-34" "platforms;android-36" "build-tools;35.0.0" "ndk;29.0.14206865" "cmake;3.22.1"
.\gradlew.bat :app:assembleGithubDebug
```

The app compiles and targets API 36. The contract module compiles against API 34. The `github` variant has `minSdk` 16 and uses Conscrypt Android 2.5.3 with an API 16 ARMv7 JNI library. The Play Store variant has `minSdk` 21. The existing manifest overrides library minimums for some optional integrations, including Google Nearby and Shizuku. A successful startup does not verify those integrations on Android 4.1.2.

Android Studio 3.0.1 uses the AGP 3.0.1 and Gradle 4.1 generation. Importing or building the current fork there would require a substantial backport of the Kotlin source and AndroidX dependencies, API 34/36 resources and manifest processing, and the native build. Even the oldest Gradle wrapper in this fork’s history used Gradle 8.5. This test used the pinned build on Windows. See the [Android Studio release archive](https://developer.android.com/studio/releases/archives) and [AGP 8.13 release notes](https://developer.android.com/build/releases/agp-8-13-0-release-notes).

The debug build completed with D8 warnings about Kotlin 2.4 metadata and Java 8 compiler deprecation warnings. Signature verification confirmed v1 (JAR) signing for API 16. The APK contained ARMv7, ARM64, x86 and x86-64 libraries. The bundled ARMv7 FFmpeg, native USB libraries and other app JNI binaries carry an NDK API 21 platform note. The app uses the platform decoder on Android 4.x. Loading optional native paths on the Jet remains a separate check. Conscrypt loaded and registered its security provider during startup.

- Gradle output `app/build/outputs/apk/github/debug/com.andrerinas.headunitrevived_4.0.0_debug.apk`
- Local copy `OpenHeadunit-4.0.0-Recon-Jet-debug.apk`
- SHA-256 `36c1dfea185189f1f242baaec21c141cc3e9835a37da881787f9dc03d0899b7c`

## Recon Jet USB smoke test

Tested on 16 November 2026. ADB identified Recon Instruments `JET` running Android 4.1.2 (API 16) on OMAP4 with `armeabi-v7a`, NEON, two ARMv7 cores, about 613 MiB reported RAM, and about 5 GB available on `/data`. The kernel reported an 800 MHz maximum frequency for CPU 0. The HUD framebuffer is 428 × 240 at density 160. The app was installed using `adb install` and opened via `adb shell am start -W -n com.andrerinas.headunitrevived/com.andrerinas.openheadunit.main.MainActivity`.

- Installation returned `Success`. The first launch exceeded the `am start -W` wait while Dalvik extracted and optimized three secondary DEX files. ActivityManager recorded the onboarding screen after 64.145 seconds.
- A fresh process with cached secondary DEX files displayed onboarding after 19.519 seconds. The app process stayed alive and had focus. No fatal exception was attributed to Open Headunit during those launch checks.
- Logcat reported Dalvik verifier warnings for newer framework classes and package-manager warnings for permissions added after API 16. The welcome screen rendered, and the safety page scrolled on the 428 × 240 display. Text extends below the visible area. The app requires acceptance of its terms before continuing to its built-in video test and the main UI.
- Device evidence is in `user_logs/recon-jet-launch.txt`, `user_logs/recon-jet-launch.png` and `user_logs/recon-jet-h264.txt`. The APK and device evidence are excluded by the existing Git ignore rules.

## Android Auto feasibility

- **Video and performance.** Stagefright forced `OMX.TI.DUCATI1.VIDEO.DECODER` and decoded 149 frames from the bundled 800 × 480 H.264 Baseline Level 3.1 fixture, remuxed to MP4 without re-encoding. It reported a 104.51 fps average in this short local decode. Start with H.264 at 800 × 480 and 30 fps for live projection. Wireless transport, rendering to the HUD, thermal load and steady-state performance remain unmeasured. The APK’s bundled FFmpeg decoder requires API 21. Stagefright also lists a Google software H.264 decoder, with performance unmeasured.
- **USB.** Android advertises USB host and accessory, and USB ADB worked with the Jet as a peripheral. Test the Jet as USB host with a data-capable OTG adapter and a phone. Start with the app’s framework USB path because the bundled libusb and JNI wrapper are built against API 21. Host power, enumeration and the Android Open Accessory handshake remain untested.
- **Wi-Fi and phones.** `iw list` exposed usable 2.4 GHz channels and AP and P2P modes. Its second-band entry listed no usable frequencies. The first wireless test should use an Android 11+ phone’s Android Auto developer Headunit Server over a shared 2.4 GHz network on TCP port 5277. The upstream project documents Android Auto 17.4+ restrictions on legacy automatic wireless launches. Google lists 5 GHz phone Wi-Fi support for standard wireless projection. Native wireless pairing on the Jet remains unverified. See the [Open Headunit connection instructions](https://github.com/andreknieriem/open-headunit#how-to-use) and [Google’s Android Auto requirements](https://support.google.com/androidauto/answer/6348019).
- **HUD and controls.** The existing onboarding renders on the HUD. Android Auto’s 800 × 480 minimum frame fits into 400 × 240 at its original aspect ratio, so phone-projected text and controls need a readability check at that scale. Test the Jet’s optical pad and buttons against the existing focus and key handling before adapting the UI.
- **Audio and voice.** The Jet’s audio policy lists a built-in speaker and microphone, 48 kHz PCM output and 16 kHz mono capture among the available input formats. The app has legacy `AudioTrack` output and `AudioRecord` microphone support. Audio routing, microphone capture and push-to-talk to the phone’s assistant remain end-to-end tests. The Jet did not list Google Play Services.

## Phone and Wi-Fi Direct test

ADB identified a Huawei P10 (VTR-L29), Android 7.0 (API 24), alongside the Android 4.1.2 Jet. The P10 was connected through its `wlan0` interface at 2427 MHz. The Jet had a Wi-Fi Direct group on `p2p-p2p0-0` and no IPv4 address on `wlan0`. Open Headunit had reached its main screen.

The P10 discovered the Jet's Wi-Fi Direct group at 2412 MHz. I initiated the connection in the P10's Wi-Fi Direct settings and accepted the invitation on the Jet using their respective ADB serials. The Jet logged WPS success and client association. The P10 obtained an IPv4 address on `p2p-wlan0-0` and exchanged three ICMP replies in each direction with zero loss. I then disconnected the P2P test link. The P10 retained its original Wi-Fi connection.

The P10 had no `com.google.android.projection.gearhead` package or TCP 5277 listener. Its Google Play listing marked the phone incompatible with the Android Auto version on offer. The current Android Auto requirements call for Android 11 or higher on the phone. The official test flow starts the Head Unit Server from Android Auto's developer menu on the phone. `adb forward` can expose an already-running server to the PC. See [Google's phone requirements](https://support.google.com/androidauto/answer/6348019?hl=en-UM) and [the Head Unit Server setup](https://developer.android.com/training/cars/testing/dhu#adb-tunneling).

This test confirmed a working 2.4 GHz Wi-Fi Direct link. A shared-router projection test, Android Auto negotiation and live HUD video require a compatible phone with the server running. Local evidence is in `user_logs/android-auto-phone-initial.txt` and `user_logs/android-auto-jet-initial.txt`.

### Redmi 9AT follow-up

ADB authorized the Redmi 9AT (M2006C3LVG), Android 11 (API 30), with Android Auto `17.8.663873-release` installed. Its WLAN interface had an IPv4 address on the router network. Open Headunit's targeted Native-mode attempt reached the Redmi over Bluetooth HFP, then timed out without Android Auto transport negotiation, P2P association or HUD projection.

The installed Android Auto manifest declares `DeveloperHeadUnitNetworkService` with `android:exported="false"`. ADB opened Android Auto settings using the app's preferences intent. The Redmi was locked and the developer service and TCP 5277 listener were absent on inspection. Unlock the phone, open Android Auto settings, enable developer mode through the Version section and choose **Start head unit server** from the overflow menu. Check for the server notification. The next connection attempt should use the confirmed 2.4 GHz Wi-Fi Direct path and the Redmi's address on that link. The phone and Jet evidence are in `user_logs/android-auto-redmi.txt` and `user_logs/android-auto-jet-redmi.txt`.

### Live Android Auto projection

The Redmi user enabled Android Auto developer mode and started the Head Unit Server. Its service listened on TCP 5277. The Redmi joined the 2.4 GHz Jet Wi-Fi Direct group and the Jet received both test pings. The first projection attempt opened TCP and timed out before a version response. On retry, Open Headunit's connection arbiter stood down its native wireless stack before opening the socket. `WifiDirectManager.stop` removed the group carrying that socket's destination. The Redmi returned to discovery and the Jet timed out while opening TCP. The shared-router path remains untested.

Google Desktop Head Unit 2.1 on the PC completed protocol 1.7 and TLS 1.2 over an ADB forward to the Redmi. A temporary USB relay through the PC then connected the existing Open Headunit APK on the Jet to the same Android Auto server. The Jet negotiated H.264 at 800 × 480, selected `OMX.TI.DUCATI1.VIDEO.DECODER`, and logged the first hardware-rendered frame. After the user tapped **Weiter** on the Redmi, the Jet HUD displayed the live Android Auto dashboard with app icons and media controls. The user confirmed seeing it. Google Maps displayed a location-permission prompt.

Live dashboard sessions eventually ended with a socket idle timeout after 15 seconds without incoming traffic. Continuous wireless projection and turn-by-turn navigation remain unverified. The diagnostic relay and ADB forwards were removed, and the previous saved destination was restored. Wi-Fi is now enabled along with USB in Open Headunit's connection modes. The tested APK and source code are unchanged.

The saved evidence is `user_logs/android-auto-jet-dashboard.png`, `user_logs/android-auto-jet-hud.png`, `user_logs/android-auto-live-jet.txt` and `user_logs/android-auto-live-redmi.txt`. For a wireless retest, put both devices on the same 2.4 GHz network and connect to the Redmi's LAN address while the Head Unit Server is running. This leaves the link independent of the app-owned Wi-Fi Direct group that the arbiter removed.
