# YIPE Personal Finances — Project Memory

## Identity
Personal finance control app (Spring Boot migration from Python/Streamlit). Single-user, server-side rendered with Thymeleaf + HTMX.

## Stack
Java 21 | Spring Boot 3.4.4 | Maven | Spring Data JPA + Hibernate | H2 (dev) / PostgreSQL (prod) | Flyway | Thymeleaf + Bootstrap 5 + HTMX + Chart.js | MapStruct 1.6.3 | Spring Security form login | JUnit 5 + Mockito + AssertJ

## Project Structure
Package `com.yipe.finance`. Package-by-layer: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `mapper/`, `config/`, `exception/`.

Key paths:
- `src/main/resources/templates/` — Thymeleaf templates extending `layout.html`
- `src/main/resources/db/migration/` — Flyway SQL migrations (V1__, V2__, etc.)
- `src/main/resources/application.yml` — base config + dev/prod profiles

## Status
All 13 migration phases complete. 29 Java source files, 5 test files.
- Phases 1-11: Full feature parity with original Python/Streamlit app
- Phase 12: Tests (TransactionServiceTest, InvoiceServiceTest, BudgetServiceTest, DashboardControllerTest, YipeApplicationTests)
- Phase 13: Polish (GlobalExceptionHandler, error.html, form validation feedback)

## Conventions

### Code
- Constructor injection (no `@Autowired` on fields)
- DTOs as Java records (`@NotBlank`, `@NotNull`, `@Positive` validation)
- `@Transactional` on service write methods
- `@Slf4j` + parameterized logging (`{}`)
- MapStruct for entity↔DTO mapping (compile-time)
- Portuguese field names in entities: `nome`, `data`, `valor`, `tipo`, `descricao`, `conta`, `categoria`, `parcela`

### Controllers
- `@Controller` (not `@RestController`), return `ModelAndView` for full pages
- HTMX fragments return strings (template fragment paths)
- `@Valid` on DTO parameters with `BindingResult`

### Templates
- Extend `layout.html` via `th:replace="layout :: main(title=..., content=...)`
- Bootstrap 5 classes + HTMX attributes (`hx-get`, `hx-post`, `hx-target`, `hx-swap`)
- Form errors via `th:errors` and validation summary

### Database
- English table names (`transacoes`, `categorias`, `contas`, `cartoes`, `salarios`)
- Flyway for migrations — always check next version number before adding
- `id` BIGINT AUTO_INCREMENT PRIMARY KEY

## Commands
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` — run locally
- `mvn test` — run all tests
- `mvn compile -q` — fast compile check
- `mvn test-compile -q` — fast test compile check

## Git
Conventional Commits: `type(scope): description` (≤50 chars). Types: feat, fix, docs, refactor, test, chore. Every commit must compile.

## Agents
- `@yipe-scaffold create module "name"` — scaffold full CRUD module
- `@yipe-test-gen for ClassName` — generate unit + slice + integration tests

## Common Gotchas
- MapStruct mappers: `componentModel = "spring"` in `@Mapper`, `@Autowired` the mapper interface
- Flyway: never modify existing migrations, create V{next} files
- HTMX: controller methods returning fragments must NOT use `@ResponseBody` — return string template path
- Form submit: always include CSRF token (`th:action` adds it automatically, raw `<form>` needs `th:action` or hidden `_csrf`)
- Field names in entities are Portuguese; DTO field names can be English
