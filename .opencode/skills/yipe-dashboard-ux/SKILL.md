---
name: yipe-dashboard-ux
description: Financial dashboard UX patterns, Chart.js best practices, KPI layouts, and data visualization for YIPE Personal Finances.
---

## YIPE Dashboard UX Conventions

### Chart.js Setup
- Loaded from CDN in `layout.html` via `<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js">`
- Charts initialized in `<script th:inline="javascript">` block
- Data injected via Thymeleaf: `/*[[${labels}]]*/`
- Reinitialize on HTMX swap: destroy existing charts first, then `initCharts()`
- Standard colors array defined at top of inline script:

```javascript
const COLORS = [
    '#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd',
    '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf'
];
```

### Chart Types for Financial Data

| Chart Type | Use Case | Recommended Config |
|-----------|----------|-------------------|
| `line` | Daily/monthly evolution | `tension: 0.3`, `fill: true`, `pointRadius: 2`, single dataset |
| `bar` | Expenses by category | `indexAxis: 'y'` (horizontal), `borderRadius: 4` |
| `doughnut` | Money flow by account | `legend.position: 'right'`, `cutout: '50%'` |
| `bar` (waterfall) | Balance formation | mixed positive/negative values, distinct colors |
| `radar` | Consumption profile | `scales.r.ticks.display: false`, subtle fill |
| `bar` (stacked) | Yearly comparison | `scales.x.stacked: true`, `scales.y.stacked: true` |

### KPI Bar Pattern
- Horizontal bar at top of dashboard with key metrics
- Items separated by thin dividers
- Each item: label (uppercase, tiny) + value (bold, larger)
- Last item right-aligned (actions/selects)
- Uses `.kpi-bar`, `.kpi-item`, `.kpi-label`, `.kpi-value` classes

### Chart Grid Layout
- 2-column CSS grid: `.chart-grid { grid-template-columns: 1fr 1fr; }`
- Each cell at 220px height (200px for full-width)
- Labels: uppercase, tiny, text-secondary above canvas
- `.chart-cell.full` spans both columns (`grid-column: 1 / -1`)
- Chart canvas fills remaining cell height

### Responsive Chart Behavior
- `maintainAspectRatio: false` on all charts
- Canvas sized via CSS: `height: calc(100% - 1.2rem)`
- On HTMX swap, destroy old Chart instances before re-initializing
- Use `Chart.getChart(canvas)` to safely destroy existing charts

### Color Usage by Financial Context
- **Green** (`--yipe-success`): income, positive values, goals achieved
- **Red** (`--yipe-danger`): expenses, negative values, over budget
- **Blue** (`--yipe-info`): investments, informational metrics
- **Yellow** (`--yipe-warning`): mid-threshold warnings
- Chart COLORS array: 10 distinct colors for category differentiation

### Loading States for Dashboard
- `hx-indicator` with CSS class toggling
- Skeleton shimmer animation for chart placeholders
- Bootstrap spinner for inline loading: `spinner-border spinner-border-sm`

### Monthly Strip Pattern
- Horizontal row below KPI bar: entries, expenses, investments
- Color-coded icons: green (entries), red (expenses), blue (investments)
- Compact: 0.35rem padding, 0.8rem font, flex with 1rem gap
