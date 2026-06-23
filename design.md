# YIPE Design System

## Tech Stack

- **CSS:** Bootstrap 5.3.3 (native dark mode via `data-bs-theme="dark"`) + custom `yipe.css`
- **Icons:** Bootstrap Icons 1.11 (SVG, 1900+ icons)
- **Charts:** Chart.js 4.4.7 with `chartjs-plugin-datalabels`
- **Interactivity:** HTMX 2.x (server-driven AJAX) + Alpine.js 3.x (lightweight reactive)
- **Backend:** Spring Boot 3.4.4 + Thymeleaf 3 + Layout Dialect

---

## Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| `--yipe-bg` | `#0D1117` | Page background (GitHub dark) |
| `--yipe-surface` | `#161B22` | Cards, sidebar, modals |
| `--yipe-border` | `#21262D` | Borders, dividers |
| `--yipe-text-primary` | `#E6EDF3` | Headings, primary text |
| `--yipe-text-secondary` | `#8B949E` | Muted text, labels |
| `--yipe-accent` | `#58A6FF` | Links, active states |
| `--yipe-accent-hover` | `#79C0FF` | Hover states |
| `--yipe-success` | `#3FB950` | Positive values, success |
| `--yipe-danger` | `#F85149` | Negative values, errors |
| `--yipe-warning` | `#D29922` | Warnings, mid-threshold |
| `--yipe-info` | `#79C0FF` | Info messages |

---

## Typography

| Element | Font | Size | Weight | Color |
|---------|------|------|--------|-------|
| Page title (h1) | system-ui | 1.75rem | 700 | `--yipe-text-primary` |
| Section title (h3) | system-ui | 1.35rem | 600 | `--yipe-text-primary` |
| Card title (h5) | system-ui | 1rem | 600 | `--yipe-text-primary` |
| Body | system-ui | 0.9rem | 400 | `--yipe-text-primary` |
| Small/muted | system-ui | 0.8rem | 400 | `--yipe-text-secondary` |
| Metric value | system-ui | 1.75rem | 700 | `--yipe-text-primary` |
| Metric label | system-ui | 0.75rem | 500 | `--yipe-text-secondary` |

---

## Spacing Scale

Following Bootstrap 5 spacing: `p-1` through `p-5` (0.25rem to 3rem).

- **Card padding:** `p-3` (1rem) inside, `g-3` (1rem) between
- **Section margin:** `mb-4` (1.5rem) below sections
- **Sidebar items:** `px-3 py-2` (1rem horizontal, 0.5rem vertical)
- **Form groups:** `mb-3` (1rem) between fields

---

## Component Specs

### Sidebar

- **Width:** 260px desktop, full overlay mobile
- **Background:** `--yipe-surface`
- **Border-right:** 1px solid `--yipe-border`
- **Item padding:** 0.6rem 1.25rem
- **Item hover:** background `rgba(255,255,255,0.05)`, translateX(4px)
- **Active item:** background `--yipe-accent` at 15% opacity, accent left border 3px
- **Bottom:** logout section with border-top, user name displayed
- **Mobile:** hamburger button toggles offcanvas overlay

### Metric Cards

- **Background:** `--yipe-surface`
- **Border:** 1px solid `--yipe-border`
- **Border-radius:** 12px
- **Padding:** 1.25rem
- **Shadow:** `0 2px 8px rgba(0,0,0,0.2)`
- **Label:** `--yipe-text-secondary`, 0.75rem, uppercase
- **Value:** 1.75rem, 700 weight

### Data Tables

- **Header:** `--yipe-bg` background, `--yipe-text-secondary` text, 0.8rem, uppercase
- **Rows:** alternating `transparent` / `rgba(255,255,255,0.02)`
- **Row hover:** `rgba(255,255,255,0.05)`
- **Border:** bottom border 1px solid `--yipe-border`
- **No vertical borders** (clean look)
- **Responsive:** `table-responsive` wrapper

### Forms

- **Input bg:** `--yipe-bg`
- **Input border:** `--yipe-border` (1px)
- **Input focus:** border `--yipe-accent`, glow `rgba(88,166,255,0.15)`
- **Input text:** `--yipe-text-primary`
- **Label:** `--yipe-text-secondary`, 0.85rem, mb-1
- **Validation:** red border + red message for errors

### Buttons

- **Primary:** bg `--yipe-accent` (10% opacity), text `--yipe-accent`, hover bg 20% opacity
- **Danger:** bg `--yipe-danger` (10% opacity), text `--yipe-danger`
- **Success:** bg `--yipe-success` (10% opacity), text `--yipe-success`
- **Outline:** border `--yipe-border`, text `--yipe-text-primary`
- **Border-radius:** 8px
- **Transitions:** 0.2s ease

### Modals

- **Background:** `--yipe-surface`
- **Border:** 1px solid `--yipe-border`
- **Border-radius:** 12px
- **Header:** bottom border 1px solid `--yipe-border`
- **Footer:** top border 1px solid `--yipe-border`
- **Backdrop:** rgba(0,0,0,0.6)

### Toasts

- **Position:** bottom-right, stacked
- **Background:** `--yipe-surface` with border
- **Border-left:** 4px solid (success green, danger red, info blue)
- **Auto-dismiss:** 5 seconds via Alpine.js

### Loading Skeletons

- **Background:** linear gradient shimmer
- **Border-radius:** 6px
- **Height:** matches content placeholder
- **Used:** on dashboard charts, table rows during HTMX load

---

## Emoji → Bootstrap Icons Mapping

| Emoji | Bootstrap Icon | Component |
|-------|---------------|-----------|
| ⚡ | `bi-lightning-fill` | App logo/header |
| 📊 | `bi-bar-chart-fill` | Dashboard nav |
| 💸 | `bi-cash-stack` | Transactions nav |
| 📜 | `bi-receipt` | Statement nav |
| 💳 | `bi-credit-card` | Invoices nav |
| 🎯 | `bi-bullseye` | Budget nav |
| ⚙️ | `bi-gear-fill` | Settings nav |
| 📁 | `bi-folder` | Import/Export nav |
| 🚪 | `bi-box-arrow-right` | Logout |
| ➕ | `bi-plus-circle` | Add new |
| ✏️ | `bi-pencil` | Edit |
| 🗑️ | `bi-trash` | Delete |
| 🔍 | `bi-search` | Search/filter |
| 💾 | `bi-save` | Save/update |
| 🔄 | `bi-arrow-repeat` | Refresh/bulk |
| 📈 | `bi-graph-up-arrow` | Charts |
| 🌊 | `bi-water` | Waterfall chart |
| 🕸️ | `bi-diagram-3` | Radar/flow |
| 📅 | `bi-calendar` | Yearly |
| 🏠 | `bi-house` | Needs/essentials |
| 🍿 | `bi-tv` | Wants/leisure |
| 🏢 | `bi-building` | Bank/accounts |
| 🏷️ | `bi-tag` | Categories |
| 💰 | `bi-wallet` | Salary/income |
| ⬇️ | `bi-download` | Export |
| ⬆️ | `bi-upload` | Import |
| 🔒 | `bi-lock` | Login/password |
| ❌ | `bi-x-circle` | Close/error |
| ✅ | `bi-check-circle` | Success |
| ⚠️ | `bi-exclamation-triangle` | Warning |

---

## HTMX Interaction Patterns

### Pattern 1: Filtered Data Table

```
Select filters → hx-get="/statement?..." → hx-target="#table-body" → hx-trigger="change"
```

Filters send GET request, server returns only the table rows partial, HTMX swaps into table.

### Pattern 2: Inline Edit

```
Click edit icon → hx-get="/statement/{id}/edit" → hx-target="#edit-form" → swap innerHTML
Submit → hx-put="/statement/{id}" → hx-target="#row-{id}" → swap with updated row
```

### Pattern 3: Dynamic Chart Refresh

```
Select month → hx-get="/dashboard?ano=X&mes=Y" → hx-target="#chart-daily" → hx-trigger="change"
```

Server returns only the chart data script partial, Alpine.js reinitializes Chart.js.

---

## Alpine.js Component Patterns

### Sidebar Toggle

```html
<div x-data="{ open: false }">
  <button @click="open = !open">☰</button>
  <aside x-show="open" @click.outside="open = false">
    <!-- sidebar -->
  </aside>
</div>
```

### Toast Notifications

```html
<div x-data="{ toasts: [] }"
     @notify.window="toasts.push($event.detail); setTimeout(() => toasts.shift(), 5000)">
  <template x-for="t in toasts" :key="t.id">
    <div x-text="t.message" :class="'toast toast-' + t.type"></div>
  </template>
</div>
```

### Password Visibility

```html
<div x-data="{ show: false }">
  <input :type="show ? 'text' : 'password'">
  <button @click="show = !show" x-text="show ? 'Hide' : 'Show'"></button>
</div>
```
