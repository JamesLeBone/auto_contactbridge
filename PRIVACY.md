# Privacy Policy for AutoContactBridge

**Last updated:** 20 November 2026

AutoContactBridge ("the App") is developed by James LeBone. This Privacy Policy explains what information the App accesses, how it is used, and what choices you have.

## 1. Information We Access

### 1.1 Contact Information
The App requests the **READ_CONTACTS** permission on your phone to allow you to browse and select contacts from your device's address book. The App stores the following information for each contact you add to your car dashboard:

- Display name
- Custom title / relationship label
- Phone number
- Facebook Messenger handle
- Preferred messaging channel (SMS or Messenger)
- Contact avatar (if set)

### 1.2 Facebook / Meta Login Session
If you choose to authenticate with Meta (Facebook), the App uses the **Facebook Login SDK** to establish a session. A session token is stored locally on your device so that you remain logged in between app launches.

### 1.3 SMS Messages
When you use the in-car messaging feature, the App may send SMS text messages on your behalf via your device's native SMS service. The content of these messages is not stored by the App after sending.

## 2. How Your Data Is Stored

All data is stored **locally on your device** using Android SharedPreferences. Specifically:

- Your contact list is saved as JSON in a local preferences file on the device.
- Your Meta/Facebook login session is managed by the Facebook SDK and stored locally.

**No data is uploaded to any server owned or operated by the developer.** The App does not have a backend server or cloud database.

## 3. Third-Party Services

### 3.1 Meta (Facebook) SDK
The App integrates the Meta (Facebook) Login SDK for optional authentication. When you log in with Facebook, Meta may collect information in accordance with their own privacy policy. Please review [Meta's Privacy Policy](https://www.facebook.com/privacy/policy/) for details on their data practices.

The App itself does **not** transmit your contact data to Meta or any other third party.

### 3.2 Android Auto / Android Automotive OS
The App integrates with Android Auto and Android Automotive OS to display your prioritised contacts on your car's dashboard. This communication happens entirely on-device between the App and the car's Android system. No data is sent to Google or any other third party as a result of this integration.

## 4. Data Sharing

The App does **not** sell, trade, or share your personal data with any third parties. Your contact information remains on your device.

## 5. Data Retention and Deletion

Your data is retained on your device for as long as the App is installed. You can delete your data at any time using the following options:

### In-App (via the ⋮ menu on the main screen)

- **Log Out of Facebook** — ends your Facebook session. Your saved contacts are not affected.
- **Clear All Contacts** — permanently deletes all saved car contacts from the App.
- **Erase All Data & Log Out** — permanently deletes all contacts AND logs you out of Facebook. This is a full data wipe within the App.

### Via your device

- **Clear app data** via your device's Settings → Apps → AutoContactBridge → Storage → Clear Data.
- **Uninstall the App**, which removes all locally stored data.

## 6. Permissions Summary

| Permission | Purpose |
|---|---|
| **READ_CONTACTS** | Browse your device's address book to add contacts to your car dashboard |
| **SEND_SMS** | Send text messages on your behalf to contacts via the in-car messaging feature |
| **INTERNET** | Required by the Facebook Login SDK for authentication |

## 7. Children's Privacy

The App is not directed at children under the age of 13 and does not knowingly collect personal information from children.

## 8. Changes to This Policy

We may update this Privacy Policy from time to time. Any changes will be reflected by updating the "Last updated" date at the top of this document. Continued use of the App after changes constitutes acceptance of the updated policy.

## 9. Contact

If you have any questions or concerns about this Privacy Policy, you can reach the developer by opening an issue on the project's GitHub repository:

[https://github.com/JamesLeBone/auto_contactbridge/issues](https://github.com/JamesLeBone/auto_contactbridge/issues)
