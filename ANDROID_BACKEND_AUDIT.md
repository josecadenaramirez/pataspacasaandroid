# Android ↔ Backend Audit

Feature | iOS function | Android function | Request parity | Response parity | Status | Notes
---|---|---|---|---|---|---
Home / List Pets | getMyPets (callable) | getMyPets (callable) | ✅ tokens payload | ✅ pets array expected | ✅ verified (serialization fix applied) | Previously Android serialized Firebase result using kotlinx.serialization on Map<String, Any> causing "Serializer for class 'Any'". Replaced with safe conversion to JsonElement.
Claim Pet | claimPet | claimPet | ✅ | ✅ | not runtime-tested | 
Create Pet | claimPet (iOS may use createPet) | claimPet | needs parity check | needs parity check | not tested |

(Complete this table as audit continues; iOS is source of truth.)
