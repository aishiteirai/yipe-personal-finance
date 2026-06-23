# YIPE Personal Finances — Skills & Agents Guide

This document explains all the OpenCode skills and custom agents installed
for the YIPE Personal Finances project (Spring Boot migration from Python/Streamlit).

---

## Table of Contents

1. [What Are Skills and Agents?](#what-are-skills-and-agents)
2. [System-Level Skills (Pre-installed)](#system-level-skills-pre-installed)
3. [Community Skills (Installed from GitHub)](#community-skills-installed-from-github)
4. [Custom Agents (Created for YIPE)](#custom-agents-created-for-yipe)
5. [How to Use Skills and Agents](#how-to-use-skills-and-agents)
6. [Where Files Live](#where-files-live)

---

## What Are Skills and Agents?

**Skills** are reusable instruction modules (`.md` files) that guide the AI's
behavior for specific tasks. They are loaded on-demand — the system reads
their metadata (name + description) at startup, and loads the full content
only when the AI decides a skill is relevant. This saves tokens.

**Agents** are specialized AI assistants you can invoke directly with `@name`
during a conversation. They have their own system prompt, tool permissions,
and optionally their own model. Think of them as dedicated teammates for
specific jobs.

---

## System-Level Skills (Pre-installed)

These skills come with the OpenCode environment and are available in every
project. They are not specific to YIPE.

### 1. caveman

Ultra-compressed communication mode. Reduces token usage by ~75% by
stripping articles, filler words, hedging, and pleasantries.

**Levels:**
- `lite` — No filler but keeps full sentences
- `full` (default) — Fragments OK, drops articles
- `ultra` — Abbreviates common words (config, req, res, impl)
- `wenyan-lite/full/ultra` — Classical Chinese compression

**When to use:** Long coding sessions where context budget is tight.
Activate with: "use caveman mode" or `/caveman`.

---

### 2. cavecrew

Delegation system for 3 specialized subagents that return compressed output
(~60% less tokens than vanilla agents).

**Subagents:**

| Subagent | Purpose | Output Format |
|----------|---------|---------------|
| `cavecrew-investigator` | Locate code, find definitions/callers | `path:line — symbol — note` |
| `cavecrew-builder` | Surgical 1-2 file edits | `path:range — change summary` |
| `cavecrew-reviewer` | Review diff for bugs | `path:line: emoji severity: problem. fix.` |

**When to use:** When exploring a large codebase and you want to save
main-context tokens for actual development work.

---

### 3. caveman-commit

Generates commit messages in Conventional Commits format:
`type(scope): description`. Subject ≤50 chars, body only if "why" isn't
obvious.

**When to use:** Before committing changes.

---

### 4. caveman-compress

Compresses memory/context files (CLAUDE.md, project docs, preferences)
into caveman format. Preserves all code, URLs, and technical substance.
Creates a human-readable backup as `FILE.original.md`.

**When to use:** When memory files are getting too large and consuming
context.

---

### 5. caveman-help

Quick-reference card for all caveman modes, skills, and commands.

**When to use:** When you forget how to invoke caveman or what commands
are available.

---

### 6. caveman-review

Ultra-compressed code review comments in one-line format:
`path:line: severity: problem. fix.`

**When to use:** To review a PR or code changes efficiently.

---

### 7. caveman-stats

Shows real token usage and estimated savings for the current session.
Reads directly from the OpenCode session log.

**When to use:** To monitor how much context is being consumed.

---

### 8. customize-opencode

Used to configure OpenCode itself: `opencode.json`, agents, subagents,
skills, plugins, MCP servers, and permission rules.

**When to use:** When editing OpenCode configuration files. Not for
project source code.

---

## Community Skills (Installed from GitHub)

These skills were downloaded from community repositories and installed
globally in `~/.agents/skills/`. They teach the AI best practices for
Java and Spring Boot development.

### 9. java-springboot (from vekzz-dev/opencode-skills)

Comprehensive Spring Boot best practices for production-ready applications.

**Key topics:**
- Constructor injection (never `@Autowired` on fields)
- DTOs (never expose entities to API layer)
- Package by feature (group by domain, not by layer)
- Bean Validation (`@Valid`, `@NotBlank`)
- `@Transactional` at service layer
- Parameterized logging (`{}` not string concat)
- Externalized configuration via `@ConfigurationProperties`
- Actuator and health checks

**Why for YIPE:** The YIPE project uses Spring Boot with Thymeleaf +
HTMX. This skill teaches the AI to generate controller/service/repository
code consistent with Spring Boot idioms. It also warns against antipatterns
like field injection and leaking entities to views.

---

### 10. java-springboot-testing (from vekzz-dev/opencode-skills)

Spring Boot testing with test slices, MockMvcTester, Testcontainers, and
AssertJ.

**Key topics:**
- `@WebMvcTest` for controller testing with MockMvc
- `@DataJpaTest` for repository testing with TestEntityManager
- `@SpringBootTest` for full integration tests
- `@MockBean` for mocking in Spring context
- Testcontainers for PostgreSQL in tests

**Why for YIPE:** Phase 12 of the migration plan is testing. The project
currently has zero unit tests. This skill ensures the AI generates proper
test slices instead of always defaulting to heavy `@SpringBootTest`.

---

### 11. java-junit (from vekzz-dev/opencode-skills)

JUnit 5 best practices: parameterized tests, assertions, mocking, test
organization.

**Key topics:**
- AAA pattern (Arrange, Act, Assert)
- `@DisplayName` for readable test names
- `@ParameterizedTest` with `@ValueSource`, `@CsvSource`, `@MethodSource`
- `assertAll` for grouped assertions
- `@Nested` for hierarchical test organization
- Mockito `@Mock` + `@InjectMocks`

**Why for YIPE:** Complements `java-springboot-testing` with pure JUnit 5
patterns. Used for service-layer unit tests (e.g., `TransactionServiceTest`,
`InvoiceServiceTest`, `BudgetServiceTest`).

---

### 12. spring-boot-engineer (from synapse-ai-hub/farmage/opencode-skills)

Complete Spring Boot engineer workflow: analysis → design → implementation
→ security → testing → deploy.

**Key topics:**
- Six-step workflow with verification gates at each step
- Quick-start templates for entity, repository, service, controller, DTO,
  exception handler, and test slice
- MUST DO and MUST NOT DO rules (constructor injection, validation,
  type-safe config, etc.)
- Reference guide with separate docs for web layer, data access, security,
  cloud native, and testing

**Why for YIPE:** This is the most comprehensive Spring Boot skill. It
provides copy-paste starting points for every layer of the application.
The verification gates (compile → test → health check) ensure code quality.

---

### 13. java-architecture (from Happydong/opencode-java-skills)

Enterprise Java architecture decisions: monolith vs microservices, layered
architecture, hexagonal architecture, package structure.

**Key topics:**
- Architecture decision framework (complex domain? team size? deploy needs?)
- Layered architecture: Presentation → Application → Domain → Infrastructure
- Package by feature (not by layer)
- Ports & Adapters (Hexagonal Architecture)
- Anti-patterns: Anemic Domain Model, God Service, Leaky Abstraction

**Why for YIPE:** The current project uses package-by-layer (controller/,
service/, repository/, entity/). This skill teaches the AI about the
alternative package-by-feature approach (order/, user/, budget/) if the
project evolves in that direction. It also helps avoid common architecture
mistakes.

---

### 14. java-ddd-patterns (from Happydong/opencode-java-skills)

Domain-Driven Design: rich domain model, layered architecture, error codes,
unified response, MapStruct object mapping.

**Key topics:**
- Rich domain model (entities with behavior, not just getters/setters)
- Layered architecture: Controller → Application → Domain → Infrastructure
- ErrorCode enum + BizException pattern
- `SingleResponse<T>` / `PageResponse<T>` unified response
- MapStruct for entity ↔ DTO conversion
- Global exception handler with `@RestControllerAdvice`

**Why for YIPE:** The project already has MapStruct in its dependencies
(pom.xml) but does not use it yet. This skill teaches the AI to generate
MapStruct mappers. The error code pattern is useful for the
`GlobalExceptionHandler` that is planned but not yet implemented.

---

### 15. java-code-style (from Happydong/opencode-java-skills)

Java coding conventions: naming, formatting, defensive programming, logging,
exception handling, annotations.

**Key topics:**
- Naming conventions (PascalCase, camelCase, UPPER_SNAKE_CASE)
- Logging standards (SLF4J, parameterized, level selection)
- Exception handling (custom ErrorCode + BizException)
- Class structure order (constants → fields → init → public → private)
- Annotation style (@Slf4j, @Service, @Transactional)

**Why for YIPE:** Ensures consistency across all phases of the migration.
When different developers or different AI sessions contribute code, this
skill makes sure they follow the same conventions.

---

### 16. java-decoupling (from Happydong/opencode-java-skills)

Code decoupling techniques: dependency injection, interface segregation,
event-driven architecture, mediator pattern, ports and adapters.

**Key topics:**
- Constructor injection (never `new` in business logic)
- Interface segregation (fat interface → multiple small interfaces)
- Breaking circular dependencies (events, interface extraction, mediator)
- Ports and Adapters (Hexagonal Architecture implementation)
- Testability as a decoupling metric

**Why for YIPE:** The `ImportExportController` currently mixes HTTP handling
with business logic (CSV parsing, validation, persistence). This skill
teaches the AI to extract a proper service layer, keeping controllers thin.

---

### 17. java-design-patterns (from Happydong/opencode-java-skills)

Design patterns: creational (factory, builder, singleton), structural
(adapter, decorator, facade), behavioral (strategy, observer, command),
modern Java patterns (Optional, function interfaces).

**Why for YIPE:** Useful for specific business logic challenges like
the invoice calculation (strategy pattern for different card types) or
the budget rule engine (strategy pattern for 50/30/20 rules).

---

### 18. changelog-maintenance (from vekzz-dev/opencode-skills)

Semantic versioning, changelogs, release notes, migration guides.

**Key topics:**
- Keep a Changelog format (Added, Changed, Fixed, Removed, Security)
- Semantic Versioning (MAJOR.MINOR.PATCH)
- Release notes (user-friendly)
- Breaking changes migration guide

**Why for YIPE:** The migration has 13 phases. This skill helps track
progress with proper changelogs and semantic versioning as each phase
is completed.

---

### 19. git-commit (from vekzz-dev/opencode-skills)

Conventional commits with diff analysis, intelligent staging, and message
generation.

**Key topics:**
- Conventional Commit format: `type(scope): description`
- Atomic commits (one logical change per commit)
- The Review Test (does it compile? can I revert it?)
- Body guidelines (explain WHY, not implementation diary)
- Git safety protocol (no force push, no hook skipping)

**Why for YIPE:** Keeps commit history clean and readable during the
migration. Each phase should produce well-structured commits.

---

## Project-Specific Skills (New)

These skills live in `.opencode/skills/` and teach the AI YIPE-specific patterns
that community skills don't cover. They auto-load when the AI detects relevant tasks.

### 20. yipe-htmx

HTMX interaction patterns specific to YIPE templates.

**Key topics:**
- Fragment endpoints (return `String` template path, never `@ResponseBody`)
- CSRF handling with HTMX forms (`th:action` or hidden `_csrf`)
- 3 concrete patterns: filtered table, inline edit, chart refresh
- Loading states with `hx-indicator` and skeleton CSS classes

### 21. yipe-entity

JPA entity conventions specific to YIPE.

**Key topics:**
- Portuguese field names table (`nome`, `data`, `valor`, `tipo`, etc.)
- `@Entity` + `@Table` conventions (English plural table names)
- MapStruct mapper pattern (`componentModel = "spring"`)
- Jakarta Validation annotations

### 22. yipe-controller

Controller conventions specific to YIPE.

**Key topics:**
- `@Controller` vs `@RestController` (always `@Controller`)
- `ModelAndView` for full pages, `String` for HTMX fragments
- `@Valid` + `BindingResult` form validation pattern
- Error handling with `GlobalExceptionHandler`

### 23. yipe-migration

Flyway migration conventions specific to YIPE.

**Key topics:**
- Version numbering (always check next, never modify existing)
- SQL conventions (Portuguese columns, DECIMAL for money, BIGINT for ID)
- Seed data pattern with `INSERT INTO`
- Safety rules (no DROP, ALTER over destructive changes)

### 24. yipe-security

Spring Security configuration for YIPE's single-user setup.

**Key topics:**
- `SecurityFilterChain` with form login
- CSRF protection (enabled by default, forms need token)
- `InMemoryUserDetailsManager` for dev, JDBC/JPA for prod
- Production checklist (env vars, HTTPS, session persistence)

### 25. yipe-test-data

Test data seeding patterns for YIPE.

**Key topics:**
- `DataInitializer` pattern with `@Profile("dev")` guard
- Standard seed data for categories, accounts, cards, salaries
- Multi-month transaction test data with all types
- Relative dates (`LocalDate.now()`) to avoid hardcoded years

---

## Custom Agents (Created for YIPE)

These agents are defined in `.opencode/agents/` as Markdown files. They are
project-specific and should be committed to Git so all contributors can
use them. Invoke with `@name` in any OpenCode conversation.

### 20. @yipe-scaffold

**Type:** Subagent
**Purpose:** Creates a complete Spring Boot module in one command.

When you invoke `@yipe-scaffold create module "budget"`, it generates:
1. Entity (`entity/Budget.java`) with JPA annotations and Portuguese fields
2. Repository (`repository/BudgetRepository.java`)
3. DTO (`dto/BudgetDTO.java`) as a Java record with validation
4. Service (`service/BudgetService.java`) with `@Transactional`
5. Controller (`controller/BudgetController.java`) with Thymeleaf + HTMX
6. Thymeleaf template (`templates/budget.html`)
7. Flyway migration (`db/migration/V{next}__create_budget.sql`)
8. Updates `DataInitializer.java` if seed data is needed

**Why created:** The YIPE project has 7+ modules (Dashboard, Transactions,
Statement, Invoices, Budget, Settings, Import/Export). Each follows the
same pattern. This agent automates the boilerplate so you focus on business
logic.

**Invocation:** `@yipe-scaffold <action> <module-name>`
- Actions: `create`, `add-field`, `add-endpoint`

---

### 21. @yipe-test-gen

**Type:** Subagent
**Purpose:** Generates JUnit 5 + Mockito tests for any class.

When you invoke `@yipe-test-gen for TransactionService`, it generates:
1. Unit test (`TransactionServiceTest.java`) with `@ExtendWith(MockitoExtension.class)`
2. Controller slice test (`TransactionControllerTest.java`) with `@WebMvcTest`
3. Repository data test (`TransactionRepositoryTest.java`) with `@DataJpaTest`
4. Integration test (`TransactionIntegrationTest.java`) with `@SpringBootTest`

Each test follows the AAA pattern with `@DisplayName`, AssertJ assertions,
`@Nested` for method grouping, and Mockito verifications. Edge cases and
exception paths are included.

**Why created:** Phase 12 of the migration is testing. The project currently
has <5 test files. This agent dramatically accelerates test coverage.

**Invocation:** `@yipe-test-gen for <ClassName>`
- Example: `@yipe-test-gen for InvoiceService`

---

### 22. @yipe-fix

**Type:** Subagent
**Purpose:** Diagnoses and fixes bugs using the known issues in development.md.

When you invoke `@yipe-fix the Settings rename is missing @Transactional`, it:
1. Reads `development.md` for the known issue and expected fix
2. Reads the affected files to understand the code
3. Implements the minimal fix following project conventions
4. Verifies with `mvn compile -q` and `mvn test -q`

**Why created:** The project has 30+ known issues across critical, performance,
test, UX, security, and architecture categories. This agent automates the
diagnose-fix-verify cycle using the roadmap as a source of truth.

**Invocation:** `@yipe-fix <description of bug>`
- Reads `development.md` to find the matching known issue
- Example: `@yipe-fix findAll loads entire table in DashboardService`

---

## How to Use Skills and Agents

### Skills

Skills are loaded automatically. The AI reads their metadata and pulls the
full content only when relevant. You don't need to do anything special.

To verify a skill is installed:
```bash
ls ~/.agents/skills/<skill-name>/SKILL.md
```

### Agents

**Subagents** (like `@yipe-scaffold` and `@yipe-test-gen`):
Invoke them in any message with `@`:
```
@yipe-scaffold create module "invoice"
```

**Primary agents** (Build and Plan):
Switch between them with the **Tab** key during a session.

### Caveman Mode

To activate compressed communication:
```
/caveman            # activates full caveman mode
/caveman lite       # lighter compression
/caveman ultra      # maximum compression
```

To disable:
```
stop caveman
```

---

## Where Files Live

### Skills (System — pre-installed)
```
~/.agents/skills/
├── cavecrew/
├── caveman/
├── caveman-commit/
├── caveman-compress/
├── caveman-help/
├── caveman-review/
└── caveman-stats/
```

### Skills (Community — installed by us)
```
~/.agents/skills/
├── changelog-maintenance/     # vekzz-dev
├── git-commit/                # vekzz-dev
├── java-architecture/         # Happydong
├── java-code-style/           # Happydong
├── java-ddd-patterns/         # Happydong
├── java-decoupling/           # Happydong
├── java-design-patterns/      # Happydong
├── java-junit/                # vekzz-dev
├── java-springboot/           # vekzz-dev
├── java-springboot-testing/   # vekzz-dev
└── spring-boot-engineer/      # synapse-ai-hub
```

### Skills (Project-specific)
```
.opencode/skills/
├── yipe-htmx/                 # HTMX fragment patterns
├── yipe-entity/               # JPA entity conventions
├── yipe-controller/           # Controller patterns
├── yipe-migration/            # Flyway migration conventions
├── yipe-security/             # Spring Security setup
└── yipe-test-data/            # Test data seeding
```

### Agents (Project-specific)
```
.opencode/agents/
├── yipe-scaffold.md           # Scaffold CRUD modules
├── yipe-test-gen.md           # Generate tests
└── yipe-fix.md                # Fix bugs from roadmap
```

### Other Config
```
opencode.json              # project-level OpenCode config (optional)
YIPE-SKILLS-AND-AGENTS.md  # this file
```
