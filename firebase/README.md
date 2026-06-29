# Firebase configuration — nextgenbuildpro

ConstructPro AI talks to the **nextgenbuildpro** Firebase project (see
`FirebaseInit.kt`). These are the security rules that make the in-app data
persist correctly in the cloud.

## Data model

Everything is partitioned per user:

```
users/{uid}/projects/{id}      # full project records
users/{uid}/materials/{id}     # full material records
users/{uid}/workers/{id}       # workers
users/{uid}/photos/{id}        # photo metadata (image bytes in Storage)
users/{uid}                    # profile doc (name, company, email)
```

Storage mirrors the same shape:

```
users/{uid}/projects/{projectId}/photos/{file}.jpg
```

`{uid}` is the Firebase Auth uid for a signed-in user. The built-in **demo**
login is a local-only session (no Firebase Auth) and uses the fixed workspace
`demo_user_001`; with the production rules below it does **not** sync to the
cloud (it still works fully offline via the local Room database).

## Deploy

```bash
firebase deploy --only firestore:rules,storage --project nextgenbuildpro
```

## Persistence model (how data is stored, short + long term)

- **Short term / local:** Room (projects, materials) and SharedPreferences
  (settings, chat history) persist on-device across restarts. Firestore's own
  on-device cache covers offline reads.
- **Long term / cloud:** full records are written to Firestore (and image bytes
  to Storage). On load, the app pulls cloud records back into Room, so data
  survives reinstall and shows up on other devices once signed in.
- **Assistant memory:** recent chat is stored locally (short term); mem0 via the
  Wade backend holds long-term semantic memory of conversations and agent
  actions.

## Note on the API key

The Firebase **Android API key** in `FirebaseInit.kt` is not a secret — Firebase
Android keys ship inside every APK. Access is controlled by these rules, not by
key secrecy. Keep the rules tight (the default below requires auth and scopes
each user to their own data).
