---
target: dashboard
total_score: 29
p0_count: 0
p1_count: 2
p2_count: 2
p3_count: 1
timestamp: 2026-06-26T13-38-48Z
slug: src-main-resources-templates-dashboard-html
---
## Critique: Dashboard (`dashboard.html`)

---

### Anti-Patterns Verdict

**Not AI-generated.** The dark theme, Portuguese labels, purpose-specific chart layout, and Bootstrap-consistent patterns show hand-crafted intent. No eyebrow headers, no numbered sections, no gradient text, no glassmorphism, no card-grid reflex. The detector found zero issues in this file.

---

### Overall Impression

A solid, functional financial dashboard that does its job. Dark theme is cohesive, metric cards give quick answers, charts cover a useful range of views. The biggest opportunity: the visual hierarchy could guide attention better — metrics, actions, and charts compete for focus rather than leading the eye through a narrative. The data story ("how am I doing?" → "what changed?" → "what's the trend?") is buried under equal-weight elements.

---

### What's Working

1. **Empty states on every chart.** Each chart cell checks for data and shows a contextual empty state (`Sem despesas neste período`, `Nenhum movimento no período`) with an icon. Many dashboards skip this — this one doesn't.
2. **HTMX chart refresh + skeleton loading.** The year/month selector swaps only the charts area with a skeleton indicator. No full page reload, no spinner-in-the-middle. Fast and polished.
3. **Dark theme cohesion.** The color system (yipe.css tokens) is applied consistently. Metric cards, chart cells, and the sidebar share the same surface/border/accent vocabulary. Feels like one product.

---

### Priority Issues

**P1 — Chart colors ignore the design system.** The COLORS array in `dashboard.html:191` is a hardcoded Tableau 10 palette (`#1f77b4`, `#ff7f0e`, …). None match `--yipe-accent`, `--yipe-success`, `--yipe-danger`, or any design token. Charts look disconnected from the UI chrome around them.

*Why it matters:* The visual system says blue accent, green success, red danger. Charts use orange, purple, brown — making data feel like a different product.
*Fix:* Map chart series to design tokens (accent blue for primary line, green/red for financial semantics, muted tints for categories).
*Suggested command:* `/impeccable colorize dashboard`

**P1 — Yearly chart processes raw transactions client-side.** `dashboard.html:266-276` receives full transaction objects and aggregates them in JavaScript by iterating, remapping months, and summing per category.

*Why it matters:* Known issue in development.md. Sends all transaction data to the browser. For 90 test rows it's fine; for 10,000 it's slow and exposes data.
*Fix:* Aggregate yearly data server-side (DashboardService already has the data), send only the pre-aggregated arrays.
*Suggested command:* `/impeccable optimize dashboard`

**P2 — Metrics lack definition context.** "Saldo Geral", "Gasto Hoje", "Receitas do Mês", "Despesas do Mês" are bare numbers with no tooltip or description of what's included/excluded.

*Why it matters:* Financial terms can be ambiguous. "Saldo Geral" — does it include investments? Credit card pending? A tooltip or small "(?)" icon would remove doubt without adding clutter.
*Fix:* Add small info icons with tooltips explaining each metric's scope.
*Suggested command:* `/impeccable clarify dashboard`

**P2 — Quick actions row interrupts the visual flow.** Metrics sit above, charts below, but actions (`Novo Lançamento`, `Ver Extrato`, `Planejamento`, `Ajustar Saldo`) are sandwiched between them — same visual weight as everything else.

*Why it matters:* The user's primary job is reading their financial state. Action buttons add noise at this moment. A floating action button or a less prominent placement would keep focus on data.
*Fix:* Move actions below the charts, or collapse into a smaller toolbar.
*Suggested command:* `/impeccable layout dashboard`

**P3 — Modal for balance adjustment is overkill.** "Ajustar Saldo" opens a modal for a single numeric input + hidden fields. An inline editable field on the Saldo Geral card would be faster.

*Why it matters:* Modal adds a click to open, a click to close, and context switch. The action is simple enough for inline editing.
*Fix:* Make the Saldo Geral value inline-editable on hover/click.
*Suggested command:* `/impeccable distill dashboard`

---

### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | HTMX skeletons good; initial load has no progress indication |
| 2 | Match System / Real World | 4 | Portuguese financial language, clear terminology |
| 3 | User Control and Freedom | 3 | Navigation is clear; modal can be dismissed |
| 4 | Consistency and Standards | 3 | Consistent with wider app; chart colors break consistency |
| 5 | Error Prevention | 3 | Empty states handle no-data gracefully |
| 6 | Recognition Rather Than Recall | 3 | Metrics could use definitions; sidebar always visible |
| 7 | Flexibility and Efficiency | 2 | No shortcuts; no expand/collapse; no data drilldown |
| 8 | Aesthetic and Minimalist Design | 3 | Clean but action buttons interrupt metric-to-chart flow |
| 9 | Error Recovery | 3 | Transactions editable; balance adjustable |
| 10 | Help and Documentation | 2 | No metric explanations; no contextual help |
| **Total** | | **29/40** | **Good (28-35)** |

**Cognitive Load Assessment:** 0 failures — low cognitive load. Single focus is clear (read your snapshot), info is chunked into 4 metrics + 5 charts, hierarchy is functional if not optimal.

---

### Persona Red Flags

**Alex (Power User)**
- No way to customize which charts appear or reorder them
- Yearly chart data loads fully client-side — wasteful for power users with years of data
- No keyboard shortcuts for date navigation (arrows to move month?)
- "Ajustar Saldo" requires 3 clicks for a single number input

**Sam (Accessibility)**
- Chart.js canvas charts have no accessible data table fallback — screen readers get blank canvases
- Color-coded financial values (green/red for positive/negative) lack icon or text indicator alongside
- Modal uses proper `aria-describedby` ✓ but chart interaction is keyboard-limited

**Project-specific: Ryan (Daily Check-in User)**
- Wants a 30-second morning glance. The 5 charts all load at once — takes time to render.
- "Saldo do Mês" at the bottom of the waterfall chart (position 4) is the most relevant number but is the last metric the eye reaches.
- Year/month selector defaults to current period without showing "no data" differently from "zero data."

---

### Minor Observations

- Chart.js 4.4.7 ships with a treemap plugin not used — potential for investment allocation view
- `sankeyLinks` uses a generic Map-style data structure from the service — typing would prevent runtime errors
- 4 metric cards use `col-6 col-md-3` — on mobile they stack 2x2 which is tight for financial numbers

---

### Questions to Consider

- What if the 4 metric cards collapsed into 2 primary metrics (balance + today's spending) on page load, with details one click away?
- Does the Sankey/flow chart add daily value, or is it a "nice to have" that makes 5 charts instead of 4?
- Should the yearly chart be the default primary chart since the daily check-in user is most interested in trends?
