# YIPE Personal Finances — Spring Boot Migration

## 1. Current State (Python / Streamlit)

| Layer | Tech |
|-------|------|
| Frontend | Streamlit (Python) |
| Backend | Python (same process) |
| Database | SQLite (raw SQL + Pandas) |
| Charts | Plotly |
| Auth | None |

**Current modules (7 screens):**

1. **Dashboard** — metrics cards, daily X-ray, interactive charts (line, bar, Sankey, waterfall, radar, stacked bar)
2. **Lançamentos** — add transactions (debit, credit, VR, investment, reserve, income) with recurring/installment logic
3. **Extrato** — filtered statement table with inline edit, bulk edit, installment restructuring
4. **Faturas** — credit card invoice calculation based on closing/ due dates
5. **Planejamento** — 50/30/20 budget rule, category mapping, progress bars
6. **Configurações** — CRUD for cards, accounts, categories, salaries
7. **Importar/Exportar** — CSV export/import

**Current DB schema:**

```sql
transacoes (id, data, tipo, valor, categoria, conta, descricao, parcela)
categorias (nome)
contas (nome, tipo)
cartoes (nome, banco, dia_fechamento, dia_vencimento)
salarios (nome, dia, valor, conta)
```

---

## 2. Target Technology Stack

| Layer | Choice | Rationale |
|-------|--------|-----------|
| Language | Java 21 | LTS, modern features (records, pattern matching, virtual threads) |
| Framework | Spring Boot 3.4+ | Mature, ecosystem, auto-configuration |
| Build | Maven | Standard for Spring Boot |
| ORM | Spring Data JPA + Hibernate | Automatic schema generation, repository pattern |
| Database | H2 (dev) / PostgreSQL (prod) | H2 for rapid development, PostgreSQL for production |
| Migrations | Flyway | Version-controlled schema evolution |
| Frontend | Thymeleaf + Bootstrap 5 + HTMX | Server-side rendering, minimal JS, good DX for single developer |
| Charts | Chart.js (via Thymeleaf) | Lightweight JS charting, no heavy Python dependencies |
| Security | Spring Security (form login) | Built-in, sufficient for personal/single-user app |
| Validation | Jakarta Validation | Bean validation annotations |
| DTO Mapping | MapStruct | Compile-time mapping, zero runtime overhead |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Standard Spring testing stack |
| CI | GitHub Actions | Runs tests on push/PR |

**Why Thymeleaf + HTMX over a SPA?**
- Single developer maintaining the project
- No need for a separate Node.js build pipeline
- HTMX allows dynamic interactions (inline editing, modal forms) without writing JS-heavy frontend code
- Server-side rendering means business logic stays in one place
- Faster initial development velocity

---

## 3. High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│                   Browser                       │
│  Thymeleaf Templates + Bootstrap 5 + HTMX +     │
│  Chart.js                                       │
└──────────────┬──────────────────────────────────┘
               │ HTTP (form submits + HTMX requests)
               ▼
┌─────────────────────────────────────────────────┐
│            Spring Boot Application              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │
│  │Controller│ │  Service │ │  Repository (JPA)│ │
│  │  Layer   │◄──► Layer  │◄──►     Layer      │ │
│  └──────────┘ └──────────┘ └──────────────────┘ │
│                      │                          │
│               ┌──────┴──────┐                   │
│               │   Model     │                   │
│               │   (Entity)  │                   │
│               └─────────────┘                   │
└──────────────────────┬──────────────────────────┘
                       │ JDBC
               ┌───────┴────────┐
               │H2 / PostgreSQL │
               └────────────────┘
```

**Layer responsibilities:**

- **Controller** — handles HTTP requests, validates input, returns Thymeleaf views or HTMX fragments
- **Service** — business logic (invoice calculation, budget rules, installment restructuring, etc.)
- **Repository** — Spring Data JPA interfaces for DB access
- **Model (Entity)** — JPA entities mapped to database tables

---

## 4. Project Structure (Maven)

```
yipe-personal-finances/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/yipe/finance/
│   │   │   ├── YipeApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── DataInitializer.java        # seed default categories/accounts
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── StatementController.java
│   │   │   │   ├── InvoiceController.java
│   │   │   │   ├── BudgetController.java
│   │   │   │   ├── SettingsController.java
│   │   │   │   └── ImportExportController.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionDTO.java
│   │   │   │   ├── TransactionFilterDTO.java
│   │   │   │   ├── InvoiceDTO.java
│   │   │   │   ├── BudgetPlanDTO.java
│   │   │   │   └── DashboardSummaryDTO.java
│   │   │   ├── entity/
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Account.java
│   │   │   │   ├── Card.java
│   │   │   │   └── Salary.java
│   │   │   ├── mapper/
│   │   │   │   ├── TransactionMapper.java
│   │   │   │   └── CardMapper.java
│   │   │   ├── repository/
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── CardRepository.java
│   │   │   │   └── SalaryRepository.java
│   │   │   ├── service/
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── InvoiceService.java
│   │   │   │   ├── BudgetService.java
│   │   │   │   └── ImportExportService.java
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   ├── application-dev.yml
│   │   │   ├── application-prod.yml
│   │   │   ├── db/migration/
│   │   │   │   ├── V1__create_initial_schema.sql
│   │   │   │   └── V2__seed_default_data.sql
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── yipe.css
│   │   │   │   └── js/
│   │   │   │       └── yipe.js
│   │   │   └── templates/
│   │   │       ├── layout.html                    # layout fragment (header, sidebar)
│   │   │       ├── dashboard.html
│   │   │       ├── transactions/
│   │   │       │   ├── list.html
│   │   │       │   ├── form.html
│   │   │       │   └── fragments.html
│   │   │       ├── statement.html
│   │   │       ├── invoices.html
│   │   │       ├── budget.html
│   │   │       ├── settings.html
│   │   │       └── import-export.html
│   └── test/
│       └── java/com/yipe/finance/
│           ├── YipeApplicationTests.java
│           ├── service/
│           │   ├── TransactionServiceTest.java
│           │   ├── InvoiceServiceTest.java
│           │   └── BudgetServiceTest.java
│           └── controller/
│               └── DashboardControllerTest.java
```

---

## 5. Database Schema (JPA Entities)

### 5.1 Transaction (`transacoes`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long (PK, auto) | |
| date | LocalDate | |
| type | String (enum-like) | DEBIT, CREDIT, VR, INVESTMENT, RESERVE, INCOME |
| amount | BigDecimal | |
| category | String | FK to Category name |
| account | String | FK to Account/Card name |
| description | String | |
| installment | String | e.g. "1/12", "Recurring", "Single" |

### 5.2 Category (`categorias`)

| Column | Type |
|--------|------|
| name | String (PK) |

### 5.3 Account (`contas`)

| Column | Type |
|--------|------|
| name | String (PK) |
| type | String (BANK, VR) |

### 5.4 Card (`cartoes`)

| Column | Type |
|--------|------|
| name | String (PK) |
| bank | String |
| closingDay | Integer |
| dueDay | Integer |

### 5.5 Salary (`salarios`)

| Column | Type |
|--------|------|
| id | Long (PK, auto) |
| name | String |
| day | Integer |
| amount | BigDecimal |
| account | String |

---

## 6. Screen Designs

### 6.1 Dashboard (`/dashboard`)

**Layout:**
- Top row: 3 metric cards (General Balance, Total Invested, Adjust Balance button)
- Section "Raio-X de Hoje": 3 info cards (Credit spent today, Debit spent today, End-of-day balance) with "View in Statement" links
- Divider
- Section "Central de Análises":
  - Year/Month selector (2 dropdowns)
  - Configurable chart panel: user can toggle 6 chart types (same as current: line, bar, sankey, waterfall, radar, stacked bar)
  - Charts rendered via Chart.js in a 2-column grid
- Sidebar: navigation menu (same 7 items as current app)

**HTMX interactions:**
- Month/year selector → re-renders chart panel via `hx-get="/dashboard/charts?year=X&month=Y"`
- Chart toggle → re-renders chart panel
- Adjust balance → form submit with confirmation

### 6.2 Transactions (`/transactions`)

**Layout:**
- Form with fields: type (dropdown), amount, date, description, account/card (dynamic based on type), category (dynamic based on type), recurring checkbox
- If recurring: radio (installment / indefinite monthly), number of months
- Submit button

**HTMX:**
- Type change → updates account dropdown and category visibility (`hx-get="/transactions/fields?type=X"`)
- Save → redirects to statement or shows success message

### 6.3 Statement (`/statement`)

**Layout:**
- Filter section (collapsible): year, month, day, type, category dropdowns + clear button
- Data table: columns (ID, date, type, description, category, account, amount, installment), sortable
- Tools section with 3 tabs:
  1. **Edit Single** — select a row, edit fields inline, save/delete
  2. **Bulk Edit** — check rows, change account/category in bulk
  3. **Restructure Installments** — select installment group, change day/amount/count/account

**HTMX:**
- Filter change → re-renders table via `hx-get="/statement?year=X&month=Y..."`
- Edit/delete → HTMX form submission, table fragment refresh
- Bulk edit → form submit, reload

### 6.4 Credit Card Invoices (`/invoices`)

**Layout:**
- Card selector (dropdown)
- Invoice reference month selector (year-month, auto-detect current)
- Metric: total invoice amount
- Due date info
- Table: date, description, category, amount, installment

**Logic (ported from Python):**
- For each credit transaction, determine which invoice it belongs to based on closing day
- If purchase day >= closing day → goes to next month's invoice
- Display grouped by invoice period

### 6.5 Budget Planning (`/budget`)

**Layout:**
- Period selector (year, month)
- Income section: shows real income (auto-calculated from INCOME transactions) + editable field for projections
- Budget rule sliders: Necessities / Wants / Investments (must sum to 100%)
- Category mapping: multiselects for which categories are "Necessities" and which are "Wants"
- Progress bars (3): Necessities, Wants, Investments with color coding (green/ orange/ red)
- Warnings when over budget

**HTMX:**
- Slider change → validates 100% sum
- Month/income change → recalculates budget bars

### 6.6 Settings (`/settings`)

**Layout:**
- 4 tabs: Cards, Accounts/ Banks, Categories, Salaries
- Each tab has an editable table (like spreadsheet) with add/delete rows
- Save button per tab

**Implementation:**
- Each tab is a `<form>` with `hx-post="/settings/cards"` etc.
- Inline editing via Thymeleaf + HTMX

### 6.7 Import/Export (`/import-export`)

**Layout:**
- Export section: "Download Backup (CSV)" button → triggers file download
- Import section: file upload (CSV), preview first rows, confirm button

---

## 7. API Design (Controllers)

Since we use server-side rendering, most controllers return `ModelAndView` for full pages and `Fragment` (HTML) for HTMX partial updates.

### 7.1 Page routes (full views)

| Method | Path | View | Description |
|--------|------|------|-------------|
| GET | `/` | redirect → `/dashboard` | Home |
| GET | `/dashboard` | `dashboard` | Main dashboard |
| GET | `/transactions` | `transactions/list` | Transaction list + new form |
| GET | `/statement` | `statement` | Statement with filters |
| GET | `/invoices` | `invoices` | Credit card invoices |
| GET | `/budget` | `budget` | Budget planning |
| GET | `/settings` | `settings` | Settings manager |
| GET | `/import-export` | `import-export` | Import/export page |

### 7.2 Action routes (form submits / HTMX)

| Method | Path | Action |
|--------|------|--------|
| POST | `/transactions` | Create new transaction (with installment expansion) |
| PUT | `/transactions/{id}` | Update single transaction |
| DELETE | `/transactions/{id}` | Delete single transaction |
| POST | `/transactions/bulk-update` | Bulk update account/category |
| POST | `/transactions/restructure` | Restructure installment group |
| GET | `/dashboard/charts?year=X&month=Y` | Return chart panel fragment |
| GET | `/statement/table?filters...` | Return filtered table fragment |
| POST | `/settings/cards` | Save cards table |
| POST | `/settings/accounts` | Save accounts table |
| POST | `/settings/categories` | Save categories table |
| POST | `/settings/salaries` | Save salaries table |
| POST | `/import/conform` | Confirm CSV import |
| GET | `/export/csv` | Download CSV backup |

---

## 8. Business Logic to Port (Critical Paths)

### 8.1 Transaction creation with installments

```java
// TransactionService.java
public void createTransaction(TransactionDTO dto) {
    if (dto.isRecurring()) {
        for (int i = 0; i < dto.getInstallmentCount(); i++) {
            Transaction t = new Transaction();
            t.setDate(dto.getDate().plusMonths(i));
            t.setAmount(dto.getAmount() / dto.getInstallmentCount());
            t.setInstallment(dto.getType() == INSTALLMENT
                ? (i + 1) + "/" + dto.getInstallmentCount()
                : "Recurring");
            // ... copy rest of fields
            repository.save(t);
        }
    } else {
        Transaction t = mapper.toEntity(dto);
        repository.save(t);
    }
}
```

### 8.2 Invoice calculation

```java
// InvoiceService.java
public String calculateInvoicePeriod(LocalDate purchaseDate, Card card) {
    if (purchaseDate.getDayOfMonth() >= card.getClosingDay()) {
        return purchaseDate.plusMonths(1).format(YearMonth::from);
    }
    return YearMonth.from(purchaseDate).toString();
}
```

### 8.3 Budget rules

```java
// BudgetService.java
public BudgetResult calculateBudget(LocalDate month, BigDecimal income,
                                    int necPct, int wantsPct, int invPct,
                                    List<String> necCategories, List<String> wantsCategories) {
    // Compute actual spending per category
    // Compare against targets
    // Return colored progress bars
}
```

---

## 9. Security

- Single user or simple form-based login via Spring Security
- No OAuth2/ SSO needed for a personal finance tool
- CSRF protection enabled (form submits)
- Session management for login persistence

---

## 10. Development Setup

**Prerequisites:**
- Java 21+
- Maven 3.9+

**Local run:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Testing:**
```bash
mvn test
```

**Build & package:**
```bash
mvn package -DskipTests
java -jar target/yipe-finance-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 11. Migration Roadmap

| Phase | Tasks |
|-------|-------|
| **Phase 1** | Project setup (Maven, dependencies, application.yml, Flyway initial schema) |
| **Phase 2** | Entities + Repositories + DataInitializer |
| **Phase 3** | Layout template + sidebar navigation + CSS |
| **Phase 4** | Transaction module (create, list, edit, delete, installments) |
| **Phase 5** | Statement module (filters, table, bulk edit, installment restructure) |
| **Phase 6** | Dashboard module (metrics, charts, daily x-ray) |
| **Phase 7** | Invoice module (credit card bill calculation) |
| **Phase 8** | Budget module (rule sliders, category mapping, progress bars) |
| **Phase 9** | Settings module (CRUD tables for cards/accounts/categories/salaries) |
| **Phase 10** | Import/Export module (CSV) |
| **Phase 11** | Security (Spring Security, login page) |
| **Phase 12** | Testing (unit + integration) |
| **Phase 13** | Polish (error handling, validation, edge cases) |
