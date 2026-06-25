---
name: yipe-theme
description: CSS customization, Bootstrap 5 dark mode, responsive design, and visual styling for YIPE Personal Finances.
---

## YIPE Theme & CSS Conventions

### Design Tokens (CSS Custom Properties)

All colors and spacing are defined as `--yipe-*` variables in `yipe.css`. Code must reference these — never hardcode hex values:

```css
--yipe-bg: #0D1117;             /* Page background */
--yipe-surface: #161B22;         /* Cards, sidebar, modals */
--yipe-surface-hover: #1C2128;  /* Hover states */
--yipe-border: #21262D;         /* Borders, dividers */
--yipe-text-primary: #E6EDF3;   /* Headings, body text */
--yipe-text-secondary: #8B949E; /* Muted text, labels */
--yipe-accent: #58A6FF;         /* Links, active states */
--yipe-accent-hover: #79C0FF;   /* Hover accent */
--yipe-success: #3FB950;        /* Positive values */
--yipe-danger: #F85149;         /* Negative values */
--yipe-warning: #D29922;        /* Warnings */
--yipe-info: #79C0FF;           /* Info */
--yipe-radius: 8px;             /* Standard border-radius */
--yipe-radius-lg: 12px;         /* Large border-radius */
--yipe-shadow: 0 2px 8px rgba(0,0,0,0.2);
--yipe-transition: 0.2s ease;
--sidebar-width: 260px;
```

### Bootstrap Dark Mode
- `data-bs-theme="dark"` on `<html>` element in `layout.html`
- Built-in Bootstrap dark mode handles form controls, tables, alerts, etc.
- Custom styles extend (not override) Bootstrap's dark theme
- Use Bootstrap 5.3.3 utility classes where possible

### Spacing Scale (Bootstrap)
- Card padding: `p-3` (1rem) or `1.25rem` in custom CSS
- Between cards: `g-3` (1rem), `mb-4` (1.5rem)
- Section margins: `mb-4`
- Form groups: `mb-3` (1rem)
- Sidebar items: `0.6rem 1.25rem`

### Button Styles
```css
.btn-primary    /* bg: accent at 10% opacity, text: accent, hover: 20% */
.btn-success   /* bg: success at 10% opacity */
.btn-danger    /* bg: danger at 10% opacity */
.btn-outline-secondary  /* border: --yipe-border, text: primary */
```

All buttons: `border-radius: var(--yipe-radius)`, `transition: all 0.2s ease`

### Card Pattern
```css
.card { background: var(--yipe-surface); border: 1px solid var(--yipe-border); border-radius: var(--yipe-radius-lg); box-shadow: var(--yipe-shadow); }
```
Use for: metric displays, form containers, data summaries.

### Form Pattern
- Inputs: `background: var(--yipe-bg)`, `border: 1px solid var(--yipe-border)`
- Focus: `border-color: var(--yipe-accent)`, `box-shadow: 0 0 0 3px rgba(88,166,255,0.15)`
- Labels: `color: var(--yipe-text-secondary)`, `font-size: 0.85rem`
- Validation errors: red border + `.invalid-feedback` in red

### Responsive Breakpoints
- **Mobile** (<768px): sidebar hidden (slide overlay), stacked layout, smaller controls
- **Desktop** (>=768px): sidebar fixed 260px, multi-column grid, `margin-left: var(--sidebar-width)` on content
- Tables: always wrapped in `table-responsive`
- Chart grid: 2-column on desktop, 1-column on mobile (use Bootstrap grid or CSS grid)

### Sidebar Pattern
- Fixed position on desktop, offcanvas overlay on mobile
- List-group items with icons + text
- Active item: accent left border, 15% accent bg
- Hover: translateX(4px) slide effect
- Bottom section: user name + logout form

### CSS File Organization (`static/css/yipe.css`)
1. Variables (`:root` block)
2. Base (body, typography)
3. Layout (#wrapper, #page-content-wrapper)
4. Sidebar (.sidebar, .sidebar-toggle, .sidebar-overlay)
5. Typography (h1-h6 overrides)
6. Component blocks (KPI, charts, tables, cards, forms, buttons, alerts, modals, tabs, toasts, skeletons)
7. Utilities (badges, dividers, links)

### Skeleton Loading Animation
```css
.skeleton { background: linear-gradient(90deg, var(--yipe-surface) 25%, var(--yipe-surface-hover) 50%, var(--yipe-surface) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 6px; }
.skeleton-chart { height: 320px; width: 100%; }
.skeleton-row { height: 40px; width: 100%; margin-bottom: 4px; }
```

### Icons
- Bootstrap Icons 1.11 via CDN
- `<i class="bi bi-{icon-name}"></i>` — always use icon classes, never emoji
- See `documentation/design.md` for full emoji → icon mapping
