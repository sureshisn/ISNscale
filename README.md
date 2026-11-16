# ISNscale

**ISNscale** is an Android app that measures real-world object lengths using your phone's camera. It uses Computer Vision techniques to calculate distances based on reference objects or user-defined calibration.

## Features

- 📏 **Real-time Measurement**: Point your camera at objects to measure their length
- 📱 **CameraX Integration**: Modern camera API for smooth preview and processing
- 🧮 **Computer Vision**: Edge detection and object recognition for accurate measurements
- ⚙️ **Calibration Mode**: Set up with reference objects of known size
- 📊 **Unit Conversion**: Measurements in centimeters (cm) with optional inches
- 🎨 **Modern UI**: Material Design 3 with light/dark theme support

## How It Works

1. Launch the app and grant camera permission
2. Place a reference object of known length in the camera view
3. Tap the object to calibrate the pixel-to-length ratio
4. Tap two points on any object to measure its distance
5. View the measurement in centimeters

## Building

### Requirements

- Android Studio Dolphin (2021.3.1) or later
- Android SDK 35 (Android 15) or later
- Java JDK 11+

### From Android Studio

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Click "Run"

### From Command Line

```bash
./gradlew assembleDebug
```

Release APK:
```bash
./gradlew assembleRelease
```

## Project Structure

```
ISNscale/
├── app/
│   ├── src/main/java/com/example/isnscale/
│   │   ├── MainActivity.kt          # Camera + measurement logic
│   │   └── ui/theme/                  # Material3 theme files
│   ├── src/main/res/
│   │   ├── layout/activity_main.xml
│   │   └── values/
│   ├── build.gradle.kts               # App-level Gradle
│   └── proguard-rules.pro
├── build.gradle.kts                   # Project-level Gradle
├── settings.gradle.kts
└── README.md
```

## Permissions

- `CAMERA`: Required for accessing the camera to measure objects
- `INTERNET`: For downloading ML models (optional)

## Configuration

You can customize these settings in `MainActivity.kt`:

- `referenceDistanceCm`: Default reference distance for calibration (default: 10 cm)

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

*Made with ❤️ for measuring the world, one pixel at a time.*