# Traverse Android Client

Traverse is a minimal Jetpack Compose Android client that integrates with the LeetFeedback backend contained in this workspace. The app offers authentication, problem tracking, social features, gamification, and admin tools that map directly to the backend API surface.

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

## Configuration

1. The app now targets the hosted backend at `https://leetfeedback-backend.onrender.com/api/` by default. If you want to point at a local server instead, change `TRAVERSE_API_BASE_URL` in `app/build.gradle.kts` (use `http://10.0.2.2:5000/api/` for Android emulators).
2. Provide valid environment variables for the backend (`MONGODB_URI`, `JWT_SECRET`, GitHub OAuth keys) as required.
3. Launch the Android app on an emulator or device. Use the authentication form to create/sign in to an account.

## Build & Run

```powershell
# From the project root
.\gradlew.bat assembleDebug
```

Open the project in Android Studio for a richer development experience. The first launch will automatically restore persisted sessions and kick off data refreshes.

## Next Steps

- Add paginated lists with lazy loading.
- Provide input validation and inline error messages for forms.
- Expand testing coverage with Compose UI tests and repository-level unit tests.
```}