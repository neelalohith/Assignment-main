# Assignment-main

# **Authentication and Onboarding App**

This project demonstrates a Kotlin-based Android application implementing authentication and onboarding screens using **Jetpack Compose** and **MVVM architecture**. The app supports Google Sign-In and Phone Authentication via Firebase and provides a seamless user onboarding experience.

---

## **Features**

1. **Authentication**
   - **Google Sign-In** using `CredentialManager` and Firebase.
   - **Phone Number Authentication** with OTP using Firebase Authentication.

2. **Onboarding**
   - Automated onboarding slides with a 3-second timeout for inactivity.
   - Clean and modern UI implemented with Jetpack Compose.

3. **Splash Screen**
   - Animated splash screen for a better user experience.

4. **Responsiveness**
   - Supports multiple screen sizes with a responsive design.

5. **Architecture**
   - Built using **MVVM architecture** and leverages **ViewModel** for state management.

---

## **Tech Stack**

- **Language:** Kotlin  
- **UI Framework:** Jetpack Compose  
- **Architecture:** MVVM (Model-View-ViewModel)  
- **Authentication:** Firebase Authentication  
- **Credential API:** AndroidX Credentials API  
- **Asynchronous Programming:** Kotlin Coroutines  

---

## **Setup Instructions**

### **Prerequisites**

1. **Android Studio Flamingo** or later.  
2. A Firebase project with the following enabled:  
   - **Google Authentication**  
   - **Phone Authentication**  
3. A properly configured `google-services.json` file placed in the `app/` directory.

---

### **Step 1: Firebase Setup**

1. Navigate to the **[Firebase Console](https://console.firebase.google.com/)**.  
2. Create a project and enable the **Google Authentication** and **Phone Authentication** methods.  
3. Download the `google-services.json` file and place it in the `app/` directory.  

---

### **Step 2: Configure Dependencies**

Ensure the following dependencies are added to your `build.gradle` file:  

```gradle
implementation "androidx.compose.ui:ui:1.5.0"
implementation "androidx.navigation:navigation-compose:2.5.3"
implementation "androidx.credentials:credentials:1.2.0-alpha01"
implementation "com.google.firebase:firebase-auth-ktx:21.1.0"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
```

### **Step 3: Run the app**

Open the project in Android Studio.
Select a physical device or emulator.
Click Run ▶️.

---

## **How It Works**

### 1. **Splash Screen**
- Displays an animated vector drawable as a branding element.  
- Automatically transitions to the onboarding screen after a delay of 2-3 seconds.  
- Managed by `SplashViewModel`, which handles time-based navigation.

---

### 2. **Onboarding Screen**
- Provides a series of slides highlighting app features and benefits.  
- Slides automatically transition after 3 seconds of inactivity or can be manually swiped by the user.  
- `OnboardingViewModel` is responsible for tracking the current slide and handling the auto-transition logic.

---

### 3. **Authentication Screen**

#### **Google Sign-In**
- Utilizes the Android Credentials API (`CredentialManager`) to fetch Google credentials.  
- Integrates with Firebase for backend authentication.  

#### **Phone Authentication**
- Sends an OTP to the user’s phone using Firebase Authentication.  
- Verifies the OTP entered by the user to authenticate and log them in.  

- Displays error messages for invalid credentials or failed OTP verification.  
- Navigates to the Home screen on successful login.  

---

### 4. **Home Screen**
- Displays a simple welcome message to authenticated users.  
- Includes a logout button that navigates back to the Authentication screen.
  
---

## **Implementation Details**

### 1. **Splash Screen**
- Managed by `SplashViewModel`.  
- The screen uses an animated vector drawable for branding.  
- Automatically transitions to the Onboarding screen after a fixed delay.

---

### 2. **Onboarding Screen**
- Utilizes a `Pager` component to provide seamless slide transitions.  
- Auto-transition is implemented using a coroutine, ensuring slides advance after 3 seconds of user inactivity.  
- Supports user interaction to temporarily pause the auto-transition.

---

### 3. **Authentication**

#### **Google Sign-In**
- Leverages `CredentialManager` for secure credential management.  
- Integrates with Firebase Authentication to verify Google ID tokens.  

#### **Phone Authentication**
- Sends an OTP to the user’s phone using Firebase Authentication.  
- Verifies the OTP entered by the user to authenticate and log them in.  

---
