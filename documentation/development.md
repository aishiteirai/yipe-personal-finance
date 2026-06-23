# YIPE — Development Roadmap

Living document. Reflects current priorities, in-progress work, known issues, and future plans.

---

## Completed

| Sprint | Status |
|--------|--------|
| **Sprint 1 — Correções Críticas** | ✅ |
| Sprint 2 — Test Coverage | ⏳ Pending |
| Sprint 3 — Performance + UX | ⏳ Pending |
| Sprint 4 — New Features | ⏳ Pending |
| Sprint 5 — Infrastructure | ⏳ Pending |
| Sprint 6 — Advanced Features | ⏳ Pending |

## Current Sprint

| Item | Status | Priority |
|------|--------|----------|
| (none) | ⏳ Waiting for next sprint definition | High |

---

## Known Issues & Tech Debt

### 🚨 Critical (All Resolved in Sprint 1 ✅)

| Issue | Fix | Status |
|-------|-----|--------|
| `findAll()` in Dashboard yearly chart | → `findByYearAndTypes()` @Query | ✅ |
| `findAll()` in Budget available years | → `findDistinctYears()` @Query | ✅ |
| `findAll()` in Statement available years | → `findDistinctYears()` via TransactionService | ✅ |
| `findAll()` in InvoiceService | Kept original (invoice period logic spans months) | ✅ De-prioritized |
| Settings rename not `@Transactional` | Added `@Transactional` to saveCard/Account/Category | ✅ |
| ImportExportController mixed concerns | Extracted `ImportExportService` | ✅ |
| Individual `save()` per CSV row | → `saveAll()` with `@Transactional` in service | ✅ |
| MapStruct unused | Created `TransactionMapper`, replaced `applyDto()` | ✅ |
| `yipe.js` missing | Created at `static/js/yipe.js` | ✅ |
| `lang="pt-BR"` missing | Added to `layout.html` | ✅ |
| BigDecimal logic in controller | Moved `computeSaldoGeral()`, `computeGastoCreditoHoje/deb` to DashboardService | ✅ |

### ⚡ Performance

| Issue | File | Detail |
|-------|------|--------|
| O(n²) Sankey index lookup | `DashboardService.java:92` | `nodeList.indexOf()` inside loop — use `Map<String, Integer>` |
| Full controller re-execution on HTMX chart filter | `dashboard.html` uses `hx-get="/dashboard"` | Should target `/dashboard/charts` fragment endpoint |
| Yearly chart sends ALL transactions to client | `DashboardService.java:99-108` | Aggregation should happen server-side, not in JavaScript |

### 🧪 Test Coverage Gaps

| Component | Tests Missing |
|-----------|---------------|
| StatementController, TransactionController, BudgetController, InvoiceController, SettingsController, ImportExportController | **Zero tests** — only DashboardController has tests |
| SecurityConfig | Login, logout, unauthorized redirect, CSRF |
| GlobalExceptionHandler | 404, 500, generic error paths |
| DataInitializer | Seed data logic |
| All repositories | Zero `@DataJpaTest` tests |
| Edge cases | Division by zero, null categories, empty data, CSV with BOM/malformed lines |
| Installment restructure | `TransactionService.restructure()` not tested |
| Sankey / Yearly data generation | `DashboardService.getSankeyData()`, `getYearlyExpenses()` not tested |

**Current coverage:** ~30% (23 assertions across 6 test classes)

### 🎨 UX / Frontend

| Issue | Detail |
|-------|--------|
| Skeleton loading CSS defined but never used | `.skeleton`, `.skeleton-chart`, `.skeleton-row` in `yipe.css` — no template applies them |
| `lang="pt-BR"` | ✅ Fixed — added to `layout.html` |
| Modal accessibility | Missing `aria-describedby`, no focus management after HTMX swaps |
| Toast auto-dismiss without pause | 5s timeout with no pause-on-hover |
| Statement default edit selection | `transactions.get(0)` auto-selected — confusing UX |
| Budget uses full page reload | `onchange="this.form.submit()"` instead of HTMX partial update |

### 🔒 Security

| Issue | Detail |
|-------|--------|
| Hardcoded credentials in source | `SecurityConfig` has `admin`/`admin` — fine for dev, but needs env vars for prod |
| In-memory user store | Resets on restart — `InMemoryUserDetailsManager` |
| No HTTPS enforcement | Acceptable for local/personal use |
| No rate limiting | No brute force protection (acceptable for personal app) |

### 🏗️ Architecture

| Issue | Detail |
|-------|--------|
| BigDecimal/stream logic in controller | ✅ Fixed — moved to `DashboardService` (Sprint 1) |
| `ResourceNotFoundException` never thrown | Defined in `exception/` but no service uses it |
| V3 test data has hardcoded years (2026) | Will become stale over time — should be relative |

---

## Future Features (Prioritized)

### Sprint 1: 🔧 Correções Críticas ✅

- [x] Replace `findAll()` with `@Query` in 4 locations (3 optimized, 1 kept due to invoice period logic)
- [x] Add `@Transactional` to Settings rename flow
- [x] Extract `ImportExportService` from controller
- [x] Use `saveAll()` in CSV import
- [x] Create MapStruct mappers and replace manual mapping
- [x] Create `yipe.js` with shared JS utilities
- [x] Documentation restructuring + AI tooling (opencode.json, project skills, CLAUDE.md)
- [x] Bonus: `lang="pt-BR"` in layout.html
- [x] Bonus: Move BigDecimal logic from DashboardController to DashboardService

### Sprint 2: 🧪 Cobertura de Testes

- [ ] `@DataJpaTest` for all repository custom queries
- [ ] Controller tests for all 7 controllers
- [ ] Edge case tests (division by zero, empty data, malformed CSV)
- [ ] Service tests for Sankey, yearly expenses, restructure
- [ ] Integration tests with `@SpringBootTest`

### Sprint 3: ⚡ Performance + UX

- [ ] Create `/dashboard/charts` HTMX fragment endpoint (lighter than full controller)
- [ ] Add skeleton loading states (`hx-indicator`) to all HTMX replacements
- [x] ~~Add `lang="pt-BR"` to layout~~ ✅ (done in Sprint 1)
- [ ] Fix modal accessibility (`aria-describedby`, focus management)
- [ ] Convert Budget form to HTMX partial update
- [ ] Fix Statement default edit selection UX
- [ ] Add pause-on-hover to toast notifications

### Sprint 4: 🆕 Novas Funcionalidades

- [ ] **Relatórios** — gastos por período, top categorias, evolução patrimonial, comparativo mês-a-mês (Chart.js + export CSV/PDF)
- [ ] **Pesquisa/Ordenação no Extrato** — buscar por descrição, ordenar por valor/data (HTMX + Spring Data `Sort`)
- [ ] **Previsto vs Realizado** — projetar gastos futuros baseado em transações recorrentes

### Sprint 5: 🔧 Infraestrutura

- [ ] Docker Compose (app + PostgreSQL)
- [ ] Variáveis de ambiente para credenciais (remover hardcoded `SecurityConfig`)
- [ ] Spring Boot Actuator (health, metrics)
- [ ] GitHub Actions deploy workflow (build → test → docker → VPS)
- [ ] Backup automático do banco

### Sprint 6: 🚀 Features Avançadas

- [ ] **Metas Financeiras** — definir objetivo (valor + prazo), progresso baseado em saldo/investimentos
- [ ] **Tags/Categorias Hierárquicas** — `Alimentação > Supermercado`, `Alimentação > Restaurante` (auto-referência JPA)
- [ ] **Timeline Visual de Faturas** — gráfico de barras mostrando evolução da fatura ao longo dos meses
- [ ] **Dashboard Customizável** — reordenar/ocultar cards com Alpine.js + localStorage
- [ ] **Notificações/Lembretes** — vencimento de fatura via HTMX SSE ou Spring `@Scheduled`
- [ ] **OCR de Comprovantes** — escanear foto e preencher formulário via API Gemini/Claude
- [ ] **Multi-usuário** — preparar entidades com `userId` para suporte futuro

---

## Points of Attention

### When Deploying to Production

1. **Credentials**: Replace hardcoded `admin`/`admin` with environment variables (`ADMIN_USER`, `ADMIN_PASSWORD`)
2. **Database**: Use PostgreSQL with persistent volume — H2 in-memory loses data on restart
3. **Flyway**: `baseline-on-migrate: true` — ensure production DB is baselined before first deploy
4. **HTTPS**: Add TLS termination (reverse proxy or Spring Boot SSL config)
5. **Session**: `InMemoryUserDetailsManager` resets on restart — consider `Spring Session JDBC` for persistent sessions
6. **Backup**: Set up cron for periodic PostgreSQL dumps

### Performance Thresholds

| Metric | Current | Warning Level | Action |
|--------|---------|---------------|--------|
| Transaction count | ~90 (test data) | >10,000 | Add `@Query` filters (see Sprint 1) |
| Dashboard load | <100ms | >1s | Implement `/dashboard/charts` fragment |
| CSV import rows | ~90 | >1,000 | Use `saveAll()` with batch inserts |

### Design Decisions Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-23 | Documentation restructured into `documentation/` dir | Keep root clean; separate stable reference from living roadmap |
| 2026-06-23 | Created project skills `.opencode/skills/yipe-*` | Teach AI YIPE-specific patterns without loading full docs |
| 2026-06-23 | CLAUDE.md slimmed to session memory only | Reduce token overhead — full conventions in skills + documentation.md |
| 2026-06-23 | Added opencode.json commands for test/run/compile | Faster dev loop with `/test`, `/run`, `/compile` |
| 2026-06-23 | Chart.js inside `layout:fragment` + `th:inline="javascript"` | Fixes three issues preventing chart rendering (see commit `b68247a`) |
| 2026-06-22 | Bootstrap Icons replace emojis | Professional look, better scaling, consistent icon library |
| 2026-06-22 | `data-bs-theme="dark"` native dark mode | Removes custom dark CSS, uses Bootstrap's built-in dark theme |
| 2026-06-23 | Sprint 1: Correções Críticas | 11 issues fixed: findAll @Queries, @Transactional Settings, ImportExportService, MapStruct, yipe.js, lang, BigDecimal refactor |
