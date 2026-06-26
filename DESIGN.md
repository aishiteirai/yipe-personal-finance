# Design

> Design system for YIPE Personal Finances. Dark-theme, Bootstrap 5.3 native, data-focused financial dashboard.

## Theme

**Dark** (`data-bs-theme="dark"`). GitHub-dark inspired palette. Professional, technical, no-nonsense.

## Color Palette

| Token | OKLCH | Hex | Usage |
|-------|-------|-----|-------|
| `--yipe-bg` | `oklch(0.13 0.01 250)` | `#0D1117` | Page background |
| `--yipe-surface` | `oklch(0.17 0.015 250)` | `#161B22` | Cards, sidebar, modals |
| `--yipe-surface-hover` | `oklch(0.19 0.015 250)` | `#1C2128` | Hovered surfaces |
| `--yipe-border` | `oklch(0.22 0.015 250)` | `#21262D` | Borders, dividers |
| `--yipe-text-primary` | `oklch(0.85 0.02 250)` | `#E6EDF3` | Headings, body text |
| `--yipe-text-secondary` | `oklch(0.55 0.02 250)` | `#8B949E` | Muted text, labels |
| `--yipe-accent` | `oklch(0.62 0.17 250)` | `#58A6FF` | Links, active states |
| `--yipe-accent-hover` | `oklch(0.70 0.17 250)` | `#79C0FF` | Hover states |
| `--yipe-success` | `oklch(0.65 0.18 145)` | `#3FB950` | Positive values |
| `--yipe-danger` | `oklch(0.55 0.22 25)` | `#F85149` | Negative values, errors |
| `--yipe-warning` | `oklch(0.60 0.16 85)` | `#D29922` | Warnings, mid-threshold |
| `--yipe-info` | `oklch(0.70 0.12 220)` | `#79C0FF` | Info messages |

### Color strategy

Committed — the dark surface carries 60%+ of the UI. Accent blue is the single interactive color. Semantic colors (green/red/yellow) reserved for financial values only.

## Typography

| Element | Size | Weight | Color | Letter-spacing |
|---------|------|--------|-------|---------------|
| Page title (h1) | 1.75rem | 700 | `--yipe-text-primary` | 0 |
| Section title (h3) | 1.35rem | 600 | `--yipe-text-primary` | 0 |
| Card title (h5) | 1rem | 600 | `--yipe-text-primary` | 0 |
| Body | 0.9rem | 400 | `--yipe-text-primary` | 0 |
| Small/muted | 0.8rem | 400 | `--yipe-text-secondary` | 0 |
| Metric value | 1.4rem | 700 | `--yipe-text-primary` | 0 |
| Metric label | 0.7rem | 600 | `--yipe-text-secondary` | 0.4px |
| Table header | 0.8rem | 600 | `--yipe-text-secondary` | 0.3px |
| Chart label | 0.72rem | 600 | `--yipe-text-secondary` | 0.4px |

**Font stack:** `system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif`

## Spacing scale

Bootstrap 5 native. Core units:
- Card padding: 1rem–1.25rem
- Section margin: 1.5rem
- Grid gap: 0.75rem
- Sidebar items: 0.6rem 1.25rem
- Form groups: 1rem

## Components

### Sidebar

Fixed 260px left rail. Surface bg, right border divider. Active item gets accent left border (3px) + tinted bg. Items animate translateX(4px) on hover. Mobile: offcanvas overlay with hamburger toggle.

### Metric Cards

Surface bg + border + 12px radius + subtle shadow. Icon in top-right at 30% opacity. Label uppercase 0.7rem. Value 1.4rem 700 weight. Sublabel below for secondary metric.

### Data Tables

Clean horizontal-only borders. Alternating row tint (2% white). Hover tint (5% white). Header: bg matches page bg, uppercase labels. Responsive wrapper.

### Chart Grid

2-column grid (stacks to 1 on mobile). Each cell: surface card with 320px height, chart label top, canvas fills remaining space. Full-width cell spans both columns. Empty state icon + message centered.

### Forms

Dark input bg, subtle border, accent glow on focus. Labels in muted secondary. Validation: red border + red feedback text.

### Buttons

8px radius. Primary: accent blue at 10% bg, solid text. Semantic variants (success/danger) follow same pattern. Outline: border only, no bg. Transitions 0.2s ease.

### Modals

Surface bg + border + 12px radius. Header/footer have bottom/top dividers. Backdrop at 60% opacity.

### Toasts

Bottom-right stacked. Surface bg + left 4px color border (semantic). 5s auto-dismiss via Alpine.js with pause-on-hover.

### Loading Skeletons

Shimmer animation (surface → surface-hover → surface). Used on chart grid and table rows during HTMX swaps.

## Icons

Bootstrap Icons 1.11. Full mapping in `documentation/design.md`. Key icons:
- Logo: `bi-lightning-fill`
- Dashboard: `bi-bar-chart-fill`
- Transactions: `bi-cash-stack`
- Statement: `bi-receipt`
- Invoices: `bi-credit-card`
- Budget: `bi-bullseye`
- Settings: `bi-gear-fill`
- Import/Export: `bi-folder`

## Interaction patterns

### HTMX

- **Filtered table:** `hx-get="/path?..." hx-target="#table-body" hx-trigger="change"`
- **Inline edit:** `hx-get` loads form, `hx-put` submits and swaps row
- **Chart refresh:** `hx-get` replaces chart area, Alpine re-inits Chart.js
- **Loading states:** `hx-indicator` targets skeleton elements

### Alpine.js

- Sidebar toggle (mobile): `x-data="{ open: false }"` + hamburger
- Toast notifications: event-driven `@notify.window` with auto-dismiss
- Password visibility toggle

## Charts

Chart.js 4.4.7. Five chart types on dashboard:
1. Daily evolution (line, filled)
2. Expenses by category (horizontal bar)
3. Money flow (doughnut)
4. Balance waterfall (bar)
5. Yearly comparison (stacked bar)

Chart colors use a 10-color categorical palette (`#1f77b4`, `#ff7f0e`, etc.). Charts reinitialize on HTMX swap via `htmx:afterSwap` event.
