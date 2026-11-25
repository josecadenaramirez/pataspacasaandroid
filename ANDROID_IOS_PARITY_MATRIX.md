# Android ↔ iOS parity matrix

Audit date: 2026-11-10. iOS is the behavioral source of truth. “Implemented” means the Android code follows the observed iOS contract; “runtime verified” means it was exercised on the `Pixel_9_Pro` AVD or against the public QA API.

| Module / flow | iOS source of truth | Android before this audit | Android parity change | Validation and status |
|---|---|---|---|---|
| Environment and API selection | `Configuration/{QA,PROD}.xcconfig`, `AppConfiguration.swift`: QA `https://qa.pataspacasa.com/api/v1`, PROD `https://pataspacasa.com/api/v1` | Firebase Callable Functions; no REST base URL | BuildConfig now selects the same QA/PROD versioned REST base | Unit build + QA `/breeds` HTTP 200. Implemented and runtime verified |
| Email login | `BackendAuthDataSource.login`, `BackendAuthRepositoryImpl` | Firebase Auth | `POST /auth/login`, strict response decoding, access token in memory, encrypted refresh token | Authenticated QA login reached Home on AVD. Implemented and runtime verified |
| Registration | `POST /auth/register`; `RegisterView` returns to login because registration does not issue tokens | Firebase registration implicitly authenticated and navigated Home; no confirmation field | REST 201 contract, confirm-password validation, return to login | Unit/build verified; live registration not run to avoid creating data. Implemented, not live-mutated |
| Password reset | `POST /auth/reset-password` | Firebase password reset | REST endpoint and iOS error mapping | Build verified; not invoked to avoid sending a real email |
| Session restore / refresh / logout | iOS keeps access token in memory, refresh token in Keychain, serializes refresh, retries requests once after 401, clears session on invalid refresh | Firebase session only; artificial 500 ms splash delay | Android Keystore AES/GCM refresh token, environment-scoped cached user, serialized refresh, 401 retry, `/auth/logout`, global auth navigation, account-scoped state reset | Cold start, update install, clean-data start, login and Home verified on AVD. Invalid-token/server-expiry requires controlled fixture |
| User-data backup / fresh install | `FreshInstallResetter`, Keychain migration and environment-scoped state | Template backup rules could restore session preferences | Session, installation ID and permission state excluded from cloud backup/device transfer; undecryptable restored state is cleared | Clean app-data launch verified; backup/restore transport not available |
| Pet list / detail | `GET /me/pets`, `GET /me/pets/:id`, no-cache, backend DTO is authoritative | Callable functions; decode failure returned empty list | REST requests with bearer auth, no-cache headers, strict decoding; no empty-list fallback | Empty authorized Home response verified on QA AVD; non-empty/detail blocked by lack of a disposable QA pet |
| Claim / add pet | iOS Add flow only links a tag; `POST /me/pets/claim/:tagId`, then refetches the real pet | Add UI only navigated elsewhere; dead local create VM; repository fabricated “Mascota vinculada” if refetch failed | One manual/NFC flow, confirmation required, REST claim then mandatory authoritative refetch; no fabricated success | UI and NFC-unavailable retry verified; repository tests verify no fallback. Real claim not run to avoid modifying pet ownership |
| NFC | `NFCScanningService` reads NDEF payload and passes it through the tag parser | Button only; no hardware integration | `NfcAdapter.enableReaderMode`, NDEF URI/text decoding, cancel on pause/dispose | No-hardware error/retry verified on AVD. Physical-tag read blocked by missing NFC device/tag |
| Pet edit and breed catalog | `EditPetViewModel`, `GET /breeds?type=`, `PATCH /me/pets/:id`; all nullable fields are sent so values can be cleared | Fixed local breed arrays; omitted pet fields; different phone rules; update fallback returned local pet | Live breed catalog, stale-request protection, all iOS fields, 10–15 digit optional phones, explicit JSON nulls, exact server pet response | Public dog catalog HTTP 200 and compile verified. Save not runtime-tested without disposable pet |
| Create pet / photo update | Live `BackendPetRepository` returns `permissionDenied` for both; iOS UI currently still exposes photo selection | Callable create/photo endpoints | Android live repository now matches iOS and never reports local success | Repository/build verified. See iOS issue note below |
| Lost mode | `PATCH /me/pets/:id/lost-mode` with `{lostMode: Bool}`, then refetch | Callable `{enable: Bool}` | Exact REST method/body, confirmation states and authoritative refetch | Unit/build verified; mutation not run without disposable pet |
| Lost scan history/detail | `GET /me/lost-pet-scans?petId=&limit=`, detail by event ID, no-cache, 401 retry | Callable list; detail searched a cached list; missing location states | Exact list/detail REST endpoints, strict ownership check, no-cache, denied/missing/shared location states, accuracy and Maps intent; missing dates remain “No disponible” | Build verified; no scan fixture in QA account |
| Deep links and push routes | `/p`, `/claim`, `/pet`, `/profile`, custom `pataspacasa://tag`, `lostPetScan` push; pending auth route is not auto-claimed | Only `/p` filters; partial push extras | All observable routes parsed; pending routes wait for auth; claim is prefilled and requires tap; logout clears pending account data | Parser tests pass; post-install deep-link run pending final APK verification |
| Push registration | `PushManager`, `/me/devices`, notification permission after auth, delete device at logout | FCM token callback was a TODO | Permission requested once after auth, token upsert with stable installation ID/platform, delete before logout, tap routes retained | Permission prompt verified on AVD; server registration side effect exercised as part of login but backend contents were not inspected |
| Premium / billing | StoreKit current entitlement, products, purchase, restore; pet permissions still come from backend pet fields | Google Play analog swallowed Billing errors and acknowledged asynchronously after reporting success | Billing errors propagate, purchase success waits for acknowledgement, pending/cancel states remain distinct, no false `isPremium=false` fallback | Unit/build verified. Product availability/purchase blocked by Play Console test configuration/account |
| Lifecycle / duplicate work | iOS session generation and request generation prevent stale account/response updates | Home requests could overlap; global expiration was not observed outside splash | Home cancels prior fetch and validates user ID, breed loads use generation, destination VMs guard repeated IDs, NFC cancels on pause, global session observer clears navigation | Compile/unit tests and foreground navigation verified; OS process-kill restore included in final runtime checklist |

## Removed mocks, fixed data and false fallbacks

- Removed the production Firebase Auth/Callable pet and scan data sources. They represented an obsolete backend contract.
- Removed the fixed dog/cat `BreedCatalog`; editing now uses the public backend catalog.
- Removed the fabricated pet returned after a successful claim when detail loading failed.
- Removed update fallback that returned the locally edited pet when the server refetch failed.
- Removed `getMyPets` decoding fallback to an empty list, which previously made malformed responses look like a valid empty account.
- Replaced the live `StubTagRepository` binding with an explicitly unavailable live repository; actual linking uses the authoritative claim endpoint.
- Removed the placeholder Google OAuth client ID because Google login is not exposed by the current iOS live REST flow.
- Removed silent Play Billing `emptyList`, `null`, and `false` error fallbacks.

## Cache and account-isolation corrections

- Pet and scan REST calls disable HTTP caching and send `Cache-Control`/`Pragma` headers equivalent to iOS.
- Refresh tokens and cached users are environment scoped and cleared together on logout, invalid refresh, undecryptable restore, or account transition.
- Home responses are ignored if the authenticated user changes while a request is in flight.
- Home reloads on resume, including after claim/edit navigation.
- Session and installation preferences are excluded from Android backup and device transfer.

## Possible iOS implementation issues (documented, iOS unchanged)

- The live iOS repository exposes photo selection but `BackendPetRepository.updatePetPhoto` always throws `permissionDenied`.
- `TagCodeExtractor` accepts production hosts but not `qa.pataspacasa.com`, while the separate Add flow accepts only the QA host. This can reject a QA universal link in `TagActivationViewModel`; Android uses the environment-compatible accepted host set and records this intentional discrepancy here.
- The iOS Google sign-in implementation is unavailable in the live REST repository and its button is commented out. Android likewise does not expose Google login.
- StoreKit/Play Billing can prove store entitlement, while effective pet capabilities still depend on backend-returned pet flags. No client-side purchase is treated as authorization for pet mutations.

## External blockers / deliberately unverified mutations

- A physical NFC-capable Android device and disposable tag are required for a real NDEF scan.
- A disposable QA pet/tag/scan fixture is required to runtime-test claim, edit, photo denial, lost-mode mutation, scan history and account switching without risking real data.
- A licensed Play test account and configured subscription offer are required for purchase/restore verification.
- The previous Android build used Firebase Auth. There is no iOS/backend token-exchange contract that can convert a legacy Firebase session into REST refresh tokens, so an update correctly requires one re-login; it does not reuse cross-backend credentials.
