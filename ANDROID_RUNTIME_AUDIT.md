# Android Runtime Audit

> Historical Firebase audit. Superseded by `ANDROID_IOS_PARITY_MATRIX.md` after Android was migrated to the current iOS REST contracts.

- [~] Home / getMyPets — partially fixed
  - Issue: "Serializer for class 'Any' is not found" when decoding Firebase callable response
  - Root cause: Using kotlinx.serialization.encodeToString on arbitrary Map/Any returned from Firebase; kotlinx.serialization needs concrete serializers
  - Fix: Convert Firebase result.data into kotlinx.serialization.json.JsonElement recursively and decodeFromJsonElement<T>()
  - Files changed: app/src/main/java/com/cadena/pataspacasa/data/datasource/CloudFunctionDataSource.kt
  - Validation: ./gradlew assembleDebug && ./gradlew test

Continue audit feature-by-feature against iOS implementation.
