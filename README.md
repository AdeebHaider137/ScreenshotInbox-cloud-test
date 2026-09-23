# Screenshot Inbox

*"Save it for a reason. Find it when you need it."*

Screenshot Inbox turns your phone's screenshot pile into a searchable personal library.
Import screenshots, tag them with a category, add a short note about why you saved it,
favorite the important ones, and find anything again in seconds — all fully offline.

## Features (MVP)

- **Import** — multi-select screenshots via Android's system Photo Picker (no storage
  permission required). Persistable URI permissions are requested so imports survive
  app/device restarts. Re-selecting an already-imported image is a no-op (no duplicate rows).
- **Home grid** — 2-column Material 3 grid with thumbnail, category chip, favorite star,
  and note preview.
- **Search** — case-insensitive, matches note, filename, and category. Live-updates the grid.
- **Filters** — All / Favorites / per-category, as a horizontally scrollable chip row.
- **Sorting** — Newest, Oldest, Favorites-first.
- **Detail screen** — full-size image, editable category (dropdown) and note, favorite
  toggle, share (`ACTION_SEND`), and delete with confirmation.
- **Delete + Undo** — confirmation dialog, then a Snackbar with Undo.
- **Categories** — 9 built-in categories seeded once on first launch; users can create,
  rename, and delete custom categories. Deleting a category moves its screenshots to
  "Other" (screenshots are never deleted). Built-in categories can't be deleted.
- **Onboarding** — single screen, shown once, tracked via DataStore.
- **Settings** — theme (System / Light / Dark, with dynamic color on Android 12+), manage
  categories shortcut, and an About section.
- **Empty states** — first-run empty inbox state and "no results" search state, built
  entirely from Compose shapes/Material icons (no image assets needed).

No backend, no accounts, no Firebase/Supabase, no cloud storage, no analytics, no ads,
no AI APIs. Everything lives in a local Room database and DataStore preferences file on
the device.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Room (local database) + Flow
- Navigation Compose
- DataStore Preferences (onboarding flag, theme)
- Coil (image loading, with graceful fallback for broken/unavailable URIs)
- Coroutines + StateFlow, ViewModel
- Manual dependency wiring (`ScreenshotInboxApp` + `ViewModelFactory`) — no DI framework,
  kept intentionally simple for an app this size.

AGP 8.5.2 / Kotlin 1.9.24 / KSP for Room's annotation processing / compileSdk & targetSdk 34
/ minSdk 24.

## Architecture

```
data/
  local/        Room entities, DAOs, AppDatabase (with one-time category seeding),
                 DataStore-backed PreferencesManager, debug-only sample data helper
  repository/   ScreenshotRepository, CategoryRepository — the only things ViewModels talk to
domain/
  model/        SortOrder, FilterType — small shared UI-facing enums/sealed classes
ui/
  screens/      One Composable per screen (Onboarding, Home, Detail, Categories,
                 CategoryEditor, Settings)
  components/   Reusable pieces: ScreenshotCard, search bar, category filter row, empty states
  theme/        Color / Type / Theme (dynamic color + light/dark/system)
navigation/    Screen routes + NavGraph
viewmodel/     One ViewModel per screen + a manual ViewModelFactory
```

UI never talks to Room directly — always through a repository. Business logic (search
matching, filtering, category reassignment, duplicate-URI checks) lives in
repositories/ViewModels, not inside Composables.

## Opening and running

1. Open the project root folder in Android Studio (Koala or newer recommended).
2. Let Gradle sync — it will download AGP 8.5.2, Kotlin 1.9.24, and the dependencies
   listed in `app/build.gradle`.
3. Run the `app` configuration on a device or emulator running API 24+.
   - The Photo Picker works out of the box on API 33+; on API 24–32 the same
     `PickMultipleVisualMedia` contract transparently falls back to a picker backed by
     Google Play services, so no extra permission handling is needed either way.

There's no `gradle-wrapper.jar` checked in here (binary files aren't practical to hand
you through this channel) — Android Studio will offer to regenerate the wrapper on
first open, or run `gradle wrapper` once if you have a local Gradle install.

**A note on this build**: this code was written and organized in an environment without
an Android SDK, emulator, or network access, so it hasn't been run through an actual
Gradle/Kotlin compile here. It's written the way I'd write production Kotlin/Compose —
correct imports, no TODOs on required features, consistent Room/Compose/Navigation
patterns — but please do a build in Android Studio and fix anything the compiler flags
before treating it as final; multi-file Compose/Room projects this size can have small
issues (an inferred generic type, a missed import) that only a real compiler catches.

## Minimum Android version

API 24 (Android 7.0).

## Permissions

None declared beyond what the Photo Picker and FileProvider need implicitly — no
`READ_EXTERNAL_STORAGE`/`READ_MEDIA_IMAGES`, no `MANAGE_EXTERNAL_STORAGE`. Screenshot
Inbox never asks for camera, contacts, location, or microphone access.

## Database structure

**screenshots**
| column | type | notes |
|---|---|---|
| id | Long (PK, autogen) | |
| uri | String | content URI of the picked image |
| fileName | String | resolved via `OpenableColumns.DISPLAY_NAME`, falls back to the URI's last path segment |
| category | String | denormalized category name (matches a `categories.name`) |
| note | String | optional, searchable |
| isFavorite | Boolean | |
| createdAt | Long | epoch millis, used for sort |

**categories**
| column | type | notes |
|---|---|---|
| id | Long (PK, autogen) | |
| name | String | unique in practice (checked in `CategoryRepository`) |
| isBuiltIn | Boolean | built-ins can't be deleted |

Both tables are exposed as `Flow` from their DAOs so the UI updates automatically.

## Known limitations (MVP)

- No OCR — search only matches note text, filename, and category, not text *inside* the
  screenshot.
- Category is stored as a denormalized string on each screenshot rather than a foreign
  key, which keeps rename/reassign logic simple at this scale but wouldn't scale to a
  huge category count with complex relations.
- No automated tests included.
- Sample data helper (`DebugSampleData`) exists for development convenience but is never
  invoked automatically — the shipped app always starts empty.

## Future roadmap (not implemented)

**Phase 2** — OCR / searchable in-image text, automatic category suggestions, duplicate
screenshot detection, direct import from the device's Screenshots folder, smart date
extraction.

**Phase 3** — on-device AI categorization; detecting products, addresses, dates, phone
numbers, and URLs inside screenshots; a reminder system.

**Phase 4** — optional cloud backup, cross-device sync, export/import.

This MVP intentionally does not use a backend or AI — everything above is future work,
not a gap in what's here today.
