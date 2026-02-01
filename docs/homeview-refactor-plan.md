# HomeView Refactor Plan (Incremental)

## Goals
- Reduce `frontend/src/views/HomeView.vue` size and cognitive load.
- Separate concerns: state/persistence, recommendations, map, and UI blocks.
- Keep behavior identical at each step.

## Ground Rules
- One step per PR/commit.
- No UI/behavior change unless explicitly noted.
- Each step should be verifiable by loading Home page and clicking recommendations.

## Step 0: Baseline Snapshot
**Purpose:** Establish quick validation notes.
**Actions:**
- Note current behavior for each recommendation: cafeteria, budget, taste, weather, trending.
- Confirm that trending hides base list (current change).
**Files:** none.

## Step 1: Consolidate Home State Restore
**Purpose:** Remove duplicate `onMounted` and restore logic.
**Actions:**
- Merge the two `onMounted` blocks into one.
- Extract functions:
  - `restoreHomeState()` for sessionStorage parsing and selection restore.
  - `initializeHomeView()` for initial fetch and map init.
- Ensure restore flow runs once and in a predictable order.
**Files:**
- `frontend/src/views/HomeView.vue`

## Step 2: Extract Persistence Helper
**Purpose:** Isolate sessionStorage logic.
**Actions:**
- Create composable `useHomePersistence`.
- Move:
  - `homeListStateStorageKey`
  - `persistHomeListState()`
  - sessionStorage parse/serialize helpers
**Files:**
- `frontend/src/composables/useHomePersistence.js` (new)
- `frontend/src/views/HomeView.vue`

## Step 3: Normalize Recommendation Source
**Purpose:** Reduce conditional duplication.
**Actions:**
- Create computed `activeRestaurantSource` that returns a single list (cafeteria excluded).
- Remove redundant RECOMMEND_TASTE override in `processedRestaurants`.
**Files:**
- `frontend/src/views/HomeView.vue`

## Step 4: Recommendation UI Block Extraction
**Purpose:** Isolate header/notice/clear actions into a component.
**Actions:**
- Create `HomeRecommendationHeader.vue`.
- Props: `type`, `subtitle`, `onClear`, `statusText`.
- Render for budget/taste/trending/weather.
**Files:**
- `frontend/src/components/ui/HomeRecommendationHeader.vue` (new)
- `frontend/src/views/HomeView.vue`

## Step 5: Map Logic Extraction
**Purpose:** Separate map lifecycle and markers.
**Actions:**
- Create `useHomeMap` composable.
- Move map init, markers, `applyUserMapCenter`, `initializeMap`, `resetMapToHome`.
**Files:**
- `frontend/src/composables/useHomeMap.js` (new)
- `frontend/src/views/HomeView.vue`

## Step 6: Recommendation Orchestration Cleanup
**Purpose:** Keep all recommendation switching logic in one place.
**Actions:**
- Expand `useHomeRecommendations` to accept minimal inputs and return:
  - `selectRecommendation`
  - `clearRecommendation`
- Remove recommendation branching from HomeView where possible.
**Files:**
- `frontend/src/composables/useHomeRecommendations.js`
- `frontend/src/views/HomeView.vue`

## Step 7: Split Long Template Sections
**Purpose:** Make HomeView template smaller.
**Actions:**
- Extract:
  - Search + filter bar
  - Recommendation content block
  - Pagination block
**Files:**
- `frontend/src/components/ui/HomeSearchBar.vue` (new)
- `frontend/src/components/ui/HomeRecommendationContent.vue` (new)
- `frontend/src/components/ui/HomePagination.vue` (new)
- `frontend/src/views/HomeView.vue`

## Step 8: Cleanup and Remove Dead Code
**Purpose:** Final pass to reduce noise.
**Actions:**
- Remove unused refs/computed after extractions.
- Ensure all watchers remain necessary.
**Files:**
- `frontend/src/views/HomeView.vue`

## Rollout Plan
- Start with Step 1 and Step 2 (safe, no UI change).
- Continue step-by-step and verify each stage with Home page manual smoke test:
  - Switch recommendations
  - Open filter modal
  - Pagination
  - Map pin selection

