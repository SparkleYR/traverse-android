# Traverse Android Client

Traverse is a minimal Jetpack Compose Android client that integrates with the LeetFeedback backend. The app offers authentication, problem tracking, social features, gamification, and admin tools that map directly to the backend API surface.

## Features

- **Authentication** – Register, sign in, and persist JWT sessions locally with DataStore.
- **GitHub integration** – Configure the linked repository/branch and pull recent commits via the backend proxy.
- **Problems dashboard** – Browse all problems, quickly add new ones (which also updates gamification), and view solved entries.
- **Gamification** – View XP, streak, level, rank, and earned badges. XP auto-refreshes after new problem submissions.
- **Friends** – Add friends by username, browse friend lists, and compare streaks/XP on the leaderboard.
- **Admin console** – When logged in as an admin, review and remove users from the system.
- **Light/Dark theme** – Material 3 theming with an always-visible toggle in the top bar.

## Architecture

- **UI** – Jetpack Compose (Material 3). Navigation Compose drives the auth/home flow. Feature screens live in `ui/screens`.
- **State management** – A single `TraverseViewModel` exposes a `StateFlow<TraverseUiState>` with feature slices and dispatch functions. One-shot events (snackbars) flow through `SharedFlow`.
- **Networking** – Retrofit + OkHttp with a custom auth interceptor that injects the stored JWT. Kotlin serialization powers the converters.
- **Persistence** – DataStore stores the current session, user profile, and theme preference.
- **Dependency container** – `TraverseAppContainer` wires together the repository, token store, and network module.

Refer to `docs/backend-overview.md` and `docs/traverse-architecture.md` for the API matrix and deeper design notes.

## Build & Run

```powershell
# From the project root
.\gradlew.bat assembleDebug
```

Open the project in Android Studio for a richer development experience. The first launch will automatically restore persisted sessions and kick off data refreshes.
