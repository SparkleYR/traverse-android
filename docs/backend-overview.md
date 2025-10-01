# LeetFeedback Backend Overview

This note summarizes the REST API exposed by the accompanying Node/Express backend so that the Traverse Android client can interact with it accurately.

## Base URL

All endpoints are exposed under `http://<server-host>:5000/api`. The server also exposes `/health` for diagnostics.

## Authentication

| Method | Path | Body | Response |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | `{ username, email, password, github_username?, github_repo?, github_branch? }` | `{ token, user: { id, username, email, role } }` |
| `POST` | `/auth/login` | `{ email, password }` | `{ token, user: { id, username, email, role, github } }` |
| `PUT` | `/auth/github` | `{ github: { username?, repo?, branch?, linked? } }` | `{ message, github }` |

- Successful login returns a long-lived JWT token and also sets it as an HTTP-only cookie. Mobile clients should store the token and send it in the `Authorization: Bearer <token>` header.
- GitHub OAuth flows are initiated at `/auth/github` but aren’t required for the mobile client; the client can rely on the `/auth/github` update endpoint instead.

## Problems

| Method | Path | Query | Body | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/problems/push` | — | Problem payload (see below) | `{ message, problem }` |
| `GET` | `/problems` | `page?`, `limit?` | — | `{ problems: Problem[], total, page, limit }` |
| `GET` | `/problems/solved` | `page?`, `limit?` | — | `{ problems: Problem[], total, page, limit }` |

**Problem payload**
```json
{
  "name": "Two Sum",
  "platform": "LeetCode",
  "difficulty": 1,
  "solved": { "value": true, "date": 0, "tries": 1 },
  "ignored": false,
  "parent_topic": "Arrays",
  "grandparent": "Data Structures",
  "problem_link": "https://leetcode.com/problems/two-sum/"
}
```

Gamification XP is incremented automatically when a problem is pushed.

## Gamification

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/gamification` | `Gamification` document for the authenticated user. |

## Friends

| Method | Path | Body | Response |
| --- | --- | --- | --- |
| `POST` | `/friends/add` | `{ username }` | `{ message }` |
| `GET` | `/friends` | — | `User[]` (friends list) |
| `GET` | `/friends/gamification` | — | `Gamification[]` (with populated `user_id`) |

## Git Integrations

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/git/getGitSolvedQuestions` | Pass-through of GitHub commit data for the linked repo. |

The endpoint requires the authenticated user to have `github.username` and `github.repo` set.

## Admin

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/admin/users` | All users (without password hashes). |
| `DELETE` | `/admin/users/:userId` | `{ message }` |

These endpoints require the user’s JWT to include `role = admin`.

## Middleware Expectations

- All protected endpoints require an `Authorization: Bearer <JWT>` header.
- Rate limiting is configured server-wide (100 requests / 15 minutes).
- Helmet, CORS (with credentials), and cookie parsing are enabled by default.

## Data Models (Simplified)

### User
```json
{
  "_id": "...",
  "username": "Alice",
  "email": "alice@example.com",
  "role": "student" | "admin",
  "github": {
    "id": "<oauth-id>",
    "username": "octocat",
    "repo": "solutions",
    "branch": "main",
    "linked": true
  },
  "friends": ["<userId>"]
}
```

### Gamification
```json
{
  "user_id": "<userId>",
  "xp": 120,
  "streak_days": 5,
  "last_streak_date": "2023-09-16T00:00:00.000Z",
  "badges": ["Week Warrior"],
  "level": 2,
  "rank": 4
}
```

### Problem
```json
{
  "name": "Two Sum",
  "platform": "LeetCode",
  "difficulty": 1,
  "solved": { "value": true, "date": 1694800000, "tries": 1 },
  "ignored": false,
  "parent_topic": "Arrays",
  "grandparent": "Data Structures",
  "problem_link": "https://leetcode.com/problems/two-sum/",
  "created_at": "2023-09-16T12:00:00.000Z"
}
```

This overview will inform both the networking layer (Retrofit service) and the UI flows in Traverse.
