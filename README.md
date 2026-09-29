# VisiDose-H2S

An industrial Android application designed for colorimetric exposure dosimetry and field badge verification.

---

## Overview

**VisiDose-H2S** provides an enterprise-grade mobile interface for industrial field personnel to authenticate, capture, and inspect colorimetric sensor wristbands for cumulative hydrogen sulfide ($H_2S$) exposure monitoring. The system combines secure local authentication, real-time optical frame alignment, and immediate telemetry readouts.

---

## Key Features

- **Field Worker Authentication Portal**: Secure sign-in gate pre-populated with active shift credentials (`W-1042`).
- **Optical Sensor Verification**: Built-in visual alignment guide for rapid strip comparison against integrated reference swatches.
- **Native Camera Integration**: Modern Android CameraX and Activity Result contracts utilizing secure `FileProvider` handling for reliable photo capture.
- **Telemetry Readout Dashboard**: Instant estimation of cumulative exposure ($28.5\text{ ppm}\cdot\text{h}$) alongside badge shelf-life validation and ambient lighting checks.

---

## Tech Stack & Architecture

- **Platform**: Native Android (API Level 26+)
- **Language**: Kotlin / Java
- **UI Framework**: Material Design components with a responsive industrial teal/navy color palette
- **Camera Pipeline**: AndroidX `ActivityResultContracts.TakePicture` & `FileProvider`

---

## Getting Started & Build Instructions

### Prerequisites

- **Android Studio** (Hedgehog or newer recommended)
- Android SDK (Target API 33+)
- Physical Android device with USB debugging enabled (recommended for hardware camera testing)

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Devendra-Shrote/Hack__Forge-VisiDose-H2S.git](https://github.com/Devendra-Shrote/Hack__Forge-VisiDose-H2S.git)
