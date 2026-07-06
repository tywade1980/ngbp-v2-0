# ConstructPro AI — The Master App

This repository now hosts the **master Android application** for the Wade ecosystem: a
single APK that unifies the construction-management product with the AI assistant, memory,
and telephony capabilities that previously lived in separate repositories.

The app is built so it is **always functional** — it runs fully on-device in offline mode and
"lights up" additional, live capabilities when the Wade backend services are reachable.

---

## Why these repositories, and what each one contributes

| Repository | What it is | Strength | Role in the master app |
|---|---|---|---|
| **ngbp-v2-0** (this repo) | Native Android construction manager (Jetpack Compose, Hilt, Room, MVVM) | A polished, real, buildable mobile app shell with projects, materials, labor, documentation, reports | **The shell.** Everything is integrated here and ships as one APK. |
| **telephony_agent** | Standalone Android call-screening prototype (CallScreeningService, ONNX Phi-3, WorkflowAgent) | On-device telephony integration with Android's call framework | **Merged in** as the `telephony` package + Voice tab. The heavy ONNX model dependency was replaced with a fast heuristic so the master APK stays lean; live AI screening is delegated to Caroline. |
| **unified-agentic-ai-foundation** | Multi-layer agent framework with a FastAPI **Orchestrator** (`/chat`, `/voice/*`, `/wcc/*`) and the **Caroline receptionist** (`/screen`, `/calls`) | Reasoning, multi-agent orchestration, and construction-specific tools (estimating, hours, pricebook, briefings) | **Backend brain.** The app's `OrchestratorApi` + `CarolineApi` clients call it for chat, estimates, briefings, and live call screening. |
| **mem0** | Memory layer with a FastAPI server (`/memories`, `/search`) | Persistent, semantic long-term memory keyed by user | **Backend memory.** The assistant recalls context before answering and persists each exchange via `MemoryApi`. |
| **wade-global-state** | Git-file-based source of truth + orchestrator/hermes routing scripts | Cross-session continuity and inter-agent routing | Conceptual source of the `user_id` / agent identity the app sends; not a runtime dependency of the APK. |
| **constructprobms** | Centauri Interlock stub (Manus API v2 manifest, no app code) | Integration scaffolding only | Documented; no runtime code to merge. |
| **Constructpro** | README-only stub ("Created by Blitzy") | — | Nothing to merge; superseded by this master app. |
| **manus-master-archive** | Archive of skills, webapp builds, configs, session outputs | Reference/knowledge dump | Source material and reference; not compiled into the APK. |

**Net:** only **ngbp-v2-0** and **telephony_agent** were buildable Android code, so they are
merged into one APK. **unified-agentic-ai-foundation**, **mem0**, and **Caroline** are live
backends the app talks to over HTTP. The remaining repos are stubs, archives, or git-based
state with nothing to compile.

---

## Architecture

```
                ┌─────────────────────────────────────────────┐
                │            ConstructPro AI (one APK)         │
                │                                              │
  Construction  │  Dashboard · Projects · Materials · Labor    │
     core ──────▶  Documentation · Reports · Workflows         │
                │                                              │
   New unified  │  Caroline (Assistant tab)   Voice/Calls tab  │
    AI layer ───▶  └── AssistantRepository ──┐  └── on-device  │
                │       WadeServiceFactory    │     screening  │
                └───────────────┬─────────────┼────────────────┘
                                │             │
                  HTTP (configurable, optional)
                                │             │
        ┌───────────────┐ ┌───────────────┐ ┌─────────────────┐
        │ Orchestrator  │ │   mem0         │ │ Caroline        │
        │ /chat /wcc/*  │ │ /memories      │ │ /screen /calls  │
        │ (agentic)     │ │ /search        │ │ (telephony)     │
        └───────────────┘ └───────────────┘ └─────────────────┘
```

Key code added under `app/src/main/java/com/constructionmanager/`:

- `data/network/wade/` — `WadeBackendConfig` (runtime-editable endpoints + identity),
  `WadeApiModels`, `WadeApiServices` (Retrofit interfaces), `WadeServiceFactory`.
- `ai/` — `AssistantRepository` (the hub that unifies orchestrator + memory + calls with
  graceful fallback) and `OfflineAssistant` (on-device responses).
- `ui/screens/assistant/` — Caroline chat screen + in-app backend settings sheet.
- `ui/screens/voice/` — call-screening controls and live call log.
- `telephony/CarolineCallScreeningService` — Android `CallScreeningService` implementation.

---

## Running it

### Build the APK
```bash
# Requires Android SDK (platform-34, build-tools 34.0.0) and JDK 17.
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

### Offline mode (default)
Install and use immediately. The Assistant answers on-device; call screening uses the
heuristic threshold in the Voice tab. No servers required.

### Live mode (full capability)
1. Start the backends from `unified-agentic-ai-foundation` (Orchestrator + Caroline) and
   `mem0` (memory server).
2. In the app, open **Assistant → settings (gear)**, toggle **Use live backend**, and set the
   three URLs (defaults assume the Android emulator: `http://10.0.2.2:8000/`,
   `:8888/` for mem0, `:8001/` for Caroline).
3. The assistant now reasons via the orchestrator, recalls/stores context in mem0, and the
   Voice tab shows qualified leads and transcripts from Caroline.

---

## End-to-end feature completion

The unified app is wired through end to end, not just stubbed:

- **Live call screening.** `CarolineCallScreeningService` is now a Hilt `@AndroidEntryPoint`. When
  the Wade backend is enabled it asks the live Caroline receptionist (`/screen`) within a short time
  budget and honors its `allow`/`block`/`transfer` decision; otherwise (or on timeout) it falls back
  to the on-device risk heuristic, so screening always works offline.
- **One-tap activation.** The Voice tab can request Android's call-screening role
  (`RoleManager.ROLE_CALL_SCREENING`) and reflects whether Caroline currently holds it.
- **Backend connectivity.** `AssistantRepository.ping()` probes the orchestrator `/health` for the
  settings UI.

## Over-the-air (OTA) updates

Because this is a native Kotlin app (not Expo/React Native), it ships **native** OTA rather than a
JS-bundle swap — two complementary paths under `update/`:

- **Google Play In-App Updates** (`com.google.android.play:app-update`) — `MainActivity` offers a
  flexible update when the app was installed from Play, with a "Restart to install" prompt.
- **Self-hosted APK channel** — the **Updates** screen (Dashboard → *Updates*, Settings →
  *App Updates*) checks a configurable JSON manifest, and on a newer `versionCode` downloads the APK
  with `DownloadManager` and installs it via a `FileProvider`. This is the relevant path for an
  internal/sideloaded pro tool. See [`ota/README.md`](./ota/README.md) for the release process and
  manifest format.
