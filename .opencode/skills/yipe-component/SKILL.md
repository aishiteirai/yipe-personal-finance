---
name: yipe-component
description: Reusable Thymeleaf + Bootstrap 5 UI component patterns for YIPE Personal Finances — tables, forms, modals, toasts, tabs, alerts.
---

## YIPE UI Component Conventions

### Data Table Pattern
```html
<div class="table-responsive mb-4" th:if="${not #lists.isEmpty(items)}">
    <table class="table">
        <thead><tr>
            <th>Column</th>
        </tr></thead>
        <tbody>
            <tr th:each="item : ${items}">
                <td th:text="${item.field}"></td>
            </tr>
        </tbody>
    </table>
</div>
<p th:if="${#lists.isEmpty(items)}" class="text-secondary"><i class="bi bi-inbox"></i> Nenhum registro.</p>
```
- Always wrap in `table-responsive`
- Table headers: uppercase (Bootstrap dark mode), font-size 0.8rem
- Striped rows via CSS nth-child(even)
- Hover highlight

### Form Section Pattern
```html
<div class="row g-3 mb-4">
    <div class="col-md-4">
        <label class="form-label">Field Name</label>
        <input type="text" name="field" class="form-control" required>
    </div>
</div>
```
- `form-label` for labels (secondary color, 0.85rem)
- `.form-control` / `.form-select` for inputs
- `row g-3` for grid layout, `col-md-{n}` for responsive sizing
- Always include `required` attribute and `step="0.01"` for currency inputs
- Currency: wrap in `input-group` with `R$` prefix

### Modal Pattern
```html
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-modal="true"
     aria-labelledby="myModalLabel" aria-describedby="myModalDesc">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <h6 class="modal-title" id="myModalLabel"><i class="bi bi-icon"></i> Title</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body">
                <p id="myModalDesc" class="text-secondary small">Description for accessibility.</p>
                <!-- form fields -->
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary btn-sm"><i class="bi bi-check-lg"></i> Action</button>
            </div>
        </div>
    </div>
</div>
```
- Always include `aria-labelledby`, `aria-describedby`, and `aria-modal="true"`
- `modal-sm` for small dialogs, default for normal
- Header: icon + title, close button
- Footer: primary action button

### Alert Pattern
```html
<div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2" role="alert">
    <i class="bi bi-check-circle-fill"></i>
    <span th:text="${message}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```
- Alert classes: `alert-success`, `alert-danger`, `alert-warning`, `alert-info`
- Always `d-flex align-items-center gap-2` for icon + text layout
- Include dismiss button with `btn-close`
- `role="alert"` for accessibility

### Tabs Pattern
```html
<ul class="nav nav-tabs" id="myTabs" role="tablist">
    <li class="nav-item" role="presentation">
        <button class="nav-link active" id="tab1-tab" data-bs-toggle="tab" data-bs-target="#tab1" type="button">
            <i class="bi bi-icon"></i> Tab 1
        </button>
    </li>
</ul>
<div class="tab-content">
    <div class="tab-pane fade show active" id="tab1" role="tabpanel">
        <!-- content -->
    </div>
</div>
```
- Active tab: `nav-link active`
- Active pane: `tab-pane fade show active`
- Connect via `data-bs-target` / `id` pairing

### Toast Notification
```html
<div x-data="{ toasts: [] }"
     @notify.window="add($event.detail)">
    <template x-for="t in toasts" :key="t.id">
        <div class="toast show" :class="'toast-' + (t.type || 'info')" role="alert"
             @mouseenter="pause(t)" @mouseleave="resume(t)">
            <div class="toast-body d-flex align-items-center gap-2">
                <i :class="'bi bi-' + (t.icon || 'info-circle') + ' fs-5'"></i>
                <span x-text="t.message"></span>
                <button type="button" class="btn-close ms-auto" @click="remove(t)"></button>
            </div>
        </div>
    </template>
</div>
```
- Container: fixed position bottom-right, z-index 9999
- Alpines.js x-data manages array of toasts
- Auto-dismiss after 5 seconds with pause-on-hover via `@mouseenter` / `@mouseleave`
- Types: `toast-info`, `toast-success`, `toast-warning`, `toast-danger`
- Send from server: redirect with `?success=msg` or use HTMX HX-Trigger response header

### Metric Card
```html
<div class="metric-card compact">
    <h6><i class="bi bi-icon text-color"></i> Label</h6>
    <h5>R$ <span th:text="${#numbers.formatDecimal(value, 0, 'COMMA', 2, 'POINT')}"></span></h5>
</div>
```
- Compact padding: 0.75rem 1rem
- Label: 0.7rem, secondary color
- Value: bold, primary color
- Use `#numbers.formatDecimal(value, 0, 'COMMA', 2, 'POINT')` for currency formatting

### HTMX Fragment Pattern (Budget-style)
```html
<form th:action="@{/path}" method="post" id="formId"
      hx-post="/path" hx-target="#results" hx-indicator="#indicator">
    <!-- form fields -->
</form>
<div id="indicator" class="htmx-indicator text-center py-3">
    <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
    </div>
</div>
<div id="results" th:fragment="results">
    <!-- dynamic content -->
</div>
```

### Empty State Pattern
```html
<p class="text-secondary"><i class="bi bi-inbox"></i> Nenhum registro encontrado.</p>
```
Use when collections are empty — consistent empty state across all screens.

### Page Title Pattern
```html
<h1><i class="bi bi-icon"></i> Page Title</h1>
```
Every page starts with an h1 containing the relevant Bootstrap icon.
