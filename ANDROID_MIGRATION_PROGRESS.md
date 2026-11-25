# Android Migration Progress — Patas pa' Casa

> Historical migration record. Current REST parity status and verified blockers are in `ANDROID_IOS_PARITY_MATRIX.md` (2026-11-10).

> Source of truth: iOS repository at `../pataspacasaios`  
> Target: `../pataspacasaandroid`  
> Architecture: Kotlin + Jetpack Compose + Material 3 + MVVM + Clean Architecture + Hilt + Firebase + Google Play Billing

---

## Build Status

- [x] `./gradlew clean` — passes
- [x] `./gradlew assembleDebug` — passes
- [x] `./gradlew test` — passes

---

## Feature Implementation

### Project Infrastructure
- [x] `build.gradle.kts` updated with all required dependencies
- [x] `libs.versions.toml` — full dependency catalog (Compose, Hilt, Firebase, Billing, Coil, etc.)
- [x] Hilt dependency injection configured (Application class + modules)
- [x] `google-services.json` placeholder (requires real Firebase project config)
- [x] AndroidManifest.xml — permissions, activities, FCM service, deep links
- [x] Material 3 theme with brand colors from iOS design system

### Domain Layer
- [x] `Pet` entity (all fields from iOS including isPremiumActive, phones[3], allergies, treatment)
- [x] `User` entity
- [x] `Subscription` entity
- [x] `NFCTag` entity
- [x] `LostPetScanEvent` entity
- [x] `SubscriptionProduct` entity
- [x] `PlanType` enum (basic/premium)
- [x] `PetStatus` enum (normal/lost)
- [x] `PetSpecies` enum (dog/cat/other)
- [x] `SubscriptionStatus` enum
- [x] `TagActivationStatus` enum
- [x] `AuthRepository` interface + `SessionState`
- [x] `PetRepository` interface
- [x] `SubscriptionRepository` interface
- [x] `TagRepository` interface
- [x] `LostPetScanRepository` interface
- [x] `SubscriptionService` interface
- [x] Auth use cases: Login, Register, SignOut, RecoverPassword, LoginWithGoogle
- [x] Pet use cases: GetPets, GetPetDetail, CreatePet, UpdatePetInfo, UpdatePetPhoto, ClaimPet
- [x] Lost mode use cases: EnableLostMode, DisableLostMode
- [x] Premium use cases: GetSubscription

### Data Layer
- [x] `PetDTO` + all request/response DTOs
- [x] `VerifySubscriptionRequestDTO` / `VerifySubscriptionResponseDTO`
- [x] `LostPetScanDTO`
- [x] `PetMapper` (DTO → Domain, including paidUntil calendar date parsing)
- [x] `UserMapper` (FirebaseUser → Domain User)
- [x] `LostPetScanMapper`
- [x] `FirebaseAuthDataSource` — signIn, register, Google credential, signOut, observeAuthState
- [x] `CloudFunctionDataSource` — getMyPets, claimPet, createPet, updateOwnPet, updateOwnPetPhoto, setOwnPetLostMode, verifySubscription, getLostPetScans
- [x] `AuthRepositoryImpl`
- [x] `FirebasePetRepository` — full JPEG conversion, Base64 encoding for photos
- [x] `FirebaseLostPetScanRepository`
- [x] `StubTagRepository`
- [x] `SubscriptionRepositoryImpl`
- [x] `GooglePlayBillingService` — loadProducts, currentSubscription, purchase, restorePurchases, onPurchasesUpdated
- [x] `PatasFcmService` — push notification handling

### Core
- [x] `AppError` sealed class (all cases from iOS, with userMessage)
- [x] `AppErrorMapper` (Firebase exceptions → AppError)
- [x] `AppColors` (exact brand colors from iOS)
- [x] `AppTypography` (Material 3 typography scale)
- [x] `PatasPaCasaTheme`

### Navigation
- [x] Type-safe routes using `@Serializable` data classes
- [x] `AppNavHost` — all routes wired
- [x] Deep link support (pataspacasa.com/p/{tagCode})

### Screens

#### Authentication
- [x] **Splash** — Firebase auth state observation, 500ms grace period for token restore
- [x] **Login** — email/password, validation, error dialogs
- [x] **Register** — name/email/password, validation (min 8 chars, uppercase required)
- [x] **ForgotPassword** — sends reset email, success state

#### Home
- [x] **HomeScreen** — greeting with first name, pull-to-refresh, pet list, empty/loading/error/offline/unauthorized states
- [x] **PetCard** composable — photo, name, species, breed, lost badge, premium badge, NFC status

#### Pet
- [x] **PetDetailScreen** — full pet info, photo update, lost mode controls, NFC section, scan history link, premium upsell
- [x] **EditPetScreen** — name, breed, ownerName, 3 phone fields, allergies, treatment; validation; permission denied state
- [x] **AddPetScreen** — NFC scan option + manual link + create new pet
- [x] **LinkTagScreen** — URL/code parsing (extracts tagCode from pataspacasa.com URLs), claim flow, success/failure states

#### Lost Mode
- [x] **LostModeScreen** — inactive/active states with confirmation dialogs, activation/deactivation success screens
- [x] **LostPetScanHistoryScreen** — scan list sorted by date
- [x] **LostPetScanDetailScreen** — date, coordinates, location description

#### User
- [x] **ProfileScreen** — user info, change password (reset email), logout with confirmation
- [x] **PremiumScreen** — already premium / available products / purchasing / success / restored / error states, restore purchases button

### Push Notifications (FCM)
- [x] `PatasFcmService` — handles incoming FCM messages, shows system notifications
- [x] `onNewToken` stub (requires backend integration to save token)
- [x] Notification channel creation (Android O+)

### Premium / Google Play Billing
- [x] `GooglePlayBillingService` replaces StoreKit
- [x] Product ID centralized: `com.pataspacasa.premium.yearly`
- [x] `loadProducts` — queries Play Store
- [x] `purchase` — launches billing flow
- [x] `restorePurchases` — queries active subscriptions
- [x] `onPurchasesUpdated` — handles purchase result, acknowledges purchase
- [x] `SubscriptionRepositoryImpl` bridges billing service to domain

### Unit Tests
- [x] `AuthValidationTest` — login/register validation rules
- [x] `PetPremiumEntitlementTest` — canEditProfile, canToggleLostMode, canUpdatePhoto, isLost, hasNFCTag
- [x] `PetActivationTest` — claimPet, enableLostMode, disableLostMode
- [x] `SubscriptionStateTest` — isActive, isExpired, status display names
- [x] `PetMapperTest` — species, status, lostMode flag, phones, isPremiumActive, paidUntil

---

## Blocked (External Dependencies)

- [!] **google-services.json** — placeholder in place; requires real Firebase project configuration from Firebase Console. Replace `app/google-services.json` with the file downloaded from Firebase Console for package `com.cadena.pataspacasa`.
- [!] **Google Sign-In** — `default_web_client_id` in `res/values/strings.xml` must be replaced with the Web OAuth Client ID from Firebase Console.
- [!] **Google Play Billing products** — `com.pataspacasa.premium.yearly` must be configured in Google Play Console with a matching subscription product.
- [!] **FCM push token registration** — `onNewToken` in `PatasFcmService` needs to persist the token to Firestore under `users/{uid}/devices/{deviceId}`.
- [!] **Cloud Functions** — All backend calls use Firebase Cloud Functions (same as iOS); no local API server required; requires Firebase project to be live.
- [!] **NFC tag scanning** — Physical NFC hardware required for end-to-end testing; the app gracefully handles absence of NFC hardware at install time.
- [!] **App signing** — Release keystore required for Play Store submission.

---

## iOS → Android Mapping Reference

| iOS | Android |
|-----|---------|
| SwiftUI NavigationStack | Navigation Compose (type-safe routes) |
| SwiftUI sheets | AlertDialog / ModalBottomSheet |
| SwiftUI alerts | AlertDialog |
| SwiftUI loading | CircularProgressIndicator |
| LazyVStack / List | LazyColumn |
| TextField | OutlinedTextField |
| StoreKit | Google Play Billing |
| UNUserNotificationCenter | FirebaseMessagingService + NotificationManager |
| AppContainer (ObservableObject) | Hilt SingletonComponent |
| @StateObject / @ObservedObject | viewModel() / collectAsState() |
| AsyncImage (Kingfisher) | Coil AsyncImage |
| PhotosPicker | ActivityResultContracts.GetContent |
| CoreNFC | Android NFC (nfc permission, optional) |
| UserDefaults | DataStore Preferences |

---

*Last updated: During migration*
