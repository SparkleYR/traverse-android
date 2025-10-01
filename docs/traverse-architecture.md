# Traverse Android App Architecture

Traverse is a Jetpack Compose, single-activity Android application that integrates with the LeetFeedback backend. The app is intentionally minimal yet feature-complete with respect to the REST API. This document outlines the high-level architecture.

## Layers

```
app/
├── data/
│   ├── local/            // Token persistence via DataStore
│   ├── model/            // Kotlin data classes mirroring backend payloads
│   ├── remote/           // Retrofit API definitions + DTO mappers
│   └── repository/       // Single TraverseRepository entry point
├── ui/
│   ├── TraverseApp.kt    // Theme + navigation host + dark/light toggle
│   ├── components/       // Reusable composables (toolbar, list cells, forms)
│   ├── navigation/       // Typed destinations and NavHost
│   ├── screens/          // Feature screens (Auth, Dashboard, Problems, Friends, Admin, Settings)
│   └── state/            // Immutable view state + events per screen
└── viewmodel/
    └── TraverseViewModel.kt // Handles UI state, orchestrates repository calls
```

## State Management

- `TraverseViewModel` maintains a `StateFlow<TraverseUiState>` that captures:
  - Session (token, current user details, theme)
  - Loading/error status per feature section
  - Collections (problems, friends, commits, gamification, users)
- Screen-level composables collect the relevant slices of the state and dispatch `TraverseEvent` intents back to the ViewModel.

## Networking

- `Retrofit` with the Kotlin serialization converter is used for HTTP calls.
- `OkHttp` provides logging and an `AuthInterceptor` that injects the persisted JWT from `TokenStore` into every request when available.
- `TraverseApi` defines suspend functions for each backend endpoint, returning DTOs that are mapped into domain models.

## Persistence

- `TokenStore` (DataStore Preferences) persists the JWT and dark/light preference so the session survives process death.
- Session refresh happens automatically at app launch by reading DataStore before making API calls.

## Navigation & UI

- `Navigation Compose` handles screen navigation. After authentication, the user lands on the `Home` graph which exposes a bottom navigation bar with tabs:
  - Dashboard (gamification summary + latest commits)
  - Problems (list + add form)
  - Friends (list + add + friend leaderboards)
  - Admin (only visible when `user.role == admin`)
- The top app bar displays a placeholder Traverse logo and a theme-toggle action.

## Theming

- Material 3 design system.
- Light/dark palettes defined in `ui/theme` based on Material baseline tokens.
- Toggle writes to DataStore and re-composes the entire app via state hoisting.

## Error Handling & UX

- Each action renders `Snackbar` messages for errors/success via `SnackbarHostState`.
- Loading states render inline `CircularProgressIndicator`s as needed.
- Forms validate required values locally before firing network requests.

## Testing Strategy (Future Work)

- UI tests could assert navigation and state transitions with `compose-ui-test`.
- Repository tests could mock Retrofit via `OkHttp MockWebServer`.

This lightweight architecture keeps the code modular and understandable, yet it covers all backend capabilities required by Traverse.
