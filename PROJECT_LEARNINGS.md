# Project Notes & Learnings: Storybook Car App

## Architecture Overview
- **Shared Module (`:shared`)**: Houses Car App Library (CAL) components, domain models, text-to-speech, and data persistence so both Android Auto and Android Automotive OS (AAOS) share identical logic.
  - `Contact.kt`: Model supporting `customTitle`, `phoneNumber`, `messengerHandle`, `preferredChannel` (`SMS` vs `MESSENGER`), and `avatarUri`.
  - `ContactRepository.kt`: Persistent SharedPreferences repository for synced contacts between phone and car; supports reordering and channel toggling.
  - `CarTtsManager.kt`: Lifecycle-aware Text-to-Speech manager for in-car voice announcements and readback.
  - In-Car Screen Hierarchy:
    - `MyCarAppScreen.kt` (Root `ListTemplate`): Displays the top 6 prioritized contacts with custom titles and quick channel indicators.
    - `ContactActionScreen.kt` (`ListTemplate`): Choice between voice Call (`ACTION_DIAL`), Voice Message, or in-car channel switching.
    - `VoiceComposeScreen.kt` (`SearchTemplate`): Voice dictation / preset selection with TTS prompt.
    - `ConfirmSendScreen.kt` (`MessageTemplate`): TTS readback and confirmation to send via `SmsManager`.
    - `MessageSentScreen.kt` (`MessageTemplate`): Sent confirmation and `popToRoot()`.
- **Mobile Module (`:mobile`)**: Jetpack Compose companion app.
  - `MainActivity.kt`: Features contact management, Android photo picker integration (`PickVisualMedia`), drag-and-drop & arrow reordering with priority badges (`#1` to `#6`), and direct Call/Message testing.
  - Service declaration: `MyCarAppService` registered with `androidx.car.app.category.MESSAGING` for Android Auto discovery.
- **Automotive Module (`:automotive`)**: Standalone AAOS target using `androidx.car.app.activity.CarAppActivity`.

## Car App Library (CAL) Critical Constraints
1. **Driving vs. Parked Restrictions**:
   - In motion, lists must not exceed 6 items (`MAX_DRIVING_ITEMS = 6`).
   - Freeform software keyboard is suppressed (`setShowKeyboardByDefault(false)` on `SearchTemplate` is used for voice speech-to-text).
2. **Messaging Interop Limits**:
   - Direct background SMS is supported via `SmsManager`.
   - Facebook Messenger does not provide a public silent dispatch SDK; interaction is routed through `fb-messenger://user-thread/{handle}` or native Android Auto notification messaging style.

## Testing & Hardware Setup
- **Linux USB Detection**:
   - Charge-only cables will fail silent detection (`lsusb` will not list the phone).
   - Use **Wireless Debugging** (`adb-tls-connect`) over local Wi-Fi when USB data cables are unavailable.
- **Android Auto Sideloading Requirement**:
   - To show debug CAL apps on a real car display, Android Auto developer mode must be unlocked (tap *Version* 10 times in Android Auto settings) and **"Unknown sources"** must be enabled.
