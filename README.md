# Kodo Bluetooth Remote (prototype)

Android host for **Redmi 8A Dual / Android 10** + iPad BLE viewer/controller.

## What is included
- Android Kotlin app with a polished host screen, BLE GATT peripheral, MediaProjection foreground service, H.264 encoder setup, and AccessibilityService-based key/touch input.
- iPad SwiftUI app with CoreBluetooth central, a 4:3 viewer area, PC-style keyboard, and BLE command sending.
- GitHub Actions workflows for Android APK builds and iOS simulator builds.
- A documented BLE packet protocol.

## Important limitations (read before testing)
This is a **source prototype**, not a verified finished remote-desktop product.
- BLE GATT is not a high-bandwidth video transport. 480p at a stable 15–24 FPS is not guaranteed and is unlikely on BLE GATT. The code sends H.264 access units in BLE chunks as an experimental path; expect low frame rates / dropped frames.
- iPadOS CoreBluetooth does not provide a general Bluetooth Classic RFCOMM/SPP client. This project uses BLE GATT.
- Android MediaProjection is user-consented. Android 10 can keep an already-authorized projection session alive in a foreground service while that session remains valid. If the service/session is stopped, revoked, or the process is killed, consent may be needed again. It does not silently restart capture after force-stop/reboot.
- Android requires a visible foreground-service notification while capture is active. The notification text is “Thanks for using my app - KodoTechnology”; it intentionally has no custom Stop action. Stop sharing is available inside the Android app and iPad app.
- AccessibilityService must be enabled by the device owner in Android Settings. This is not silently enabled by the app.
- Android accessibility-based keyboard injection is not equivalent to a hardware HID keyboard. Some apps/games will ignore keys or multi-key combinations. Gaming-grade multi-touch is not guaranteed.
- iOS workflow builds for the simulator. A physical iPad build/install requires macOS signing/provisioning and an appropriate Apple developer setup.

## Android build in GitHub Codespaces
Use JDK 17 (recommended for this pinned Gradle/Android Gradle Plugin combination):

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk unzip
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Install Gradle 8.9 (or use a preinstalled Gradle 8.9), then:

```bash
cd KodoBluetoothRemote/AndroidHost
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## iPad project
Open `iPadViewer/KodoBluetoothRemote.xcodeproj` in Xcode on macOS.
Select an iPad simulator and Build. For a physical iPad, configure signing in Xcode.
The GitHub Actions iOS workflow builds the simulator target only.

## First-run setup
1. Install Android debug APK on Redmi 8A Dual.
2. Open the app and tap **Start screen sharing**.
3. Accept Android's MediaProjection system prompt.
4. Enable the app's accessibility service in Android Settings if remote key/touch input is wanted.
5. On iPad, open the viewer and tap **Connect**. Keep the Android foreground service running.
6. Stop sharing from the Android app or iPad app.

## 4:3 layout
The iPad viewer reserves a 4:3 preview area and aspect-fits the phone's portrait video inside it. This avoids stretching the phone screen; letterboxing is expected.
