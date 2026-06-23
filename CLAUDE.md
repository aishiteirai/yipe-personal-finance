# YIPE Personal Finances — Session Memory

## Identity
Personal finance control app (Spring Boot from Python/Streamlit). Single-user, server-side rendered with Thymeleaf + HTMX.

## Stack
Java 21 | Spring Boot 3.4.4 | Maven | Spring Data JPA + Hibernate | H2 (dev)/PostgreSQL (prod) | Flyway | Thymeleaf + Bootstrap 5 + HTMX + Chart.js | MapStruct 1.6.3 | Spring Security form login | JUnit 5 + Mockito + AssertJ

Package: `com.yipe.finance` — controller/ | service/ | repository/ | entity/ | dto/ | mapper/ | config/ | exception/

## Status
All 13 migration phases complete. 29 source, 5 test files. Now entering maintenance + enhancement phase.

## Commands
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` — run
- `mvn test` — all tests
- `mvn compile -q` — fast compile
- `mvn test-compile -q` — fast test compile

## Agents
- `@yipe-scaffold create module "name"` — full CRUD module
- `@yipe-test-gen for ClassName` — tests (unit + slice + integration)
- `@yipe-fix <description>` — fix bugs from development.md roadmap

## Common Gotchas
- MapStruct: `componentModel = "spring"` in `@Mapper`, constructor-inject the mapper
- Flyway: never modify existing migrations, create V{next}
- HTMX fragments: return `String` (template path), NOT `@ResponseBody`
- CSRF: use `th:action` on forms, or add hidden `_csrf` input
- Entity fields: Portuguese (`nome`, `data`, `valor`); DTO fields: English
