---
name: yipe-htmx
description: HTMX interaction patterns for YIPE Personal Finances. Fragment endpoints, form submission with CSRF, filter tables, inline edit, chart refresh.
---

## YIPE HTMX Conventions

### Fragment Endpoints
- Controller methods returning HTMX fragments return `String` (template fragment path), NOT `@ResponseBody`
- Fragments are Thymeleaf template fragments: `"transactions/fragments :: table"`
- Full-page requests return `ModelAndView`

### HTMX Attributes Used
| Attribute | Usage |
|-----------|-------|
| `hx-get` | Load fragment on event (filter change, edit click) |
| `hx-post` | Submit form, get updated fragment back |
| `hx-put` / `hx-delete` | Update/delete single entity |
| `hx-target` | Target element ID for swap (e.g. `#table-body`, `#edit-form`) |
| `hx-swap` | `innerHTML` (default), `outerHTML` for row replacement |
| `hx-trigger` | `change` for filters, `click` for buttons, `load` for auto-load |
| `hx-indicator` | CSS class for loading states (skeleton animation) |

### CSRF + HTMX
- Every form using `hx-post`/`hx-put`/`hx-delete` must include CSRF token
- Use Thymeleaf's `th:action` on `<form>` — it adds `_csrf` automatically
- For raw `<form>` without `th:action`, add hidden input: `<input type="hidden" name="_csrf" th:value="${_csrf.token}" />`

### Common Patterns

#### Pattern 1: Filtered Table
```html
<select name="year" hx-get="/statement/table" hx-target="#table-body" hx-trigger="change">
```

#### Pattern 2: Inline Edit
```html
<button hx-get="/statement/{id}/edit" hx-target="#edit-form">Edit</button>
<form hx-put="/statement/{id}" hx-target="#row-{id}" hx-swap="outerHTML">
```

#### Pattern 3: Chart Refresh
```html
<select hx-get="/dashboard/charts?ano=X&mes=Y" hx-target="#chart-panel" hx-trigger="change">
```

### Loading States
- Add `hx-indicator=".skeleton-loader"` to HTMX elements
- Skeleton classes in `yipe.css`: `.skeleton`, `.skeleton-chart`, `.skeleton-row`
- Show/hide with `hx-indicator` class toggling
