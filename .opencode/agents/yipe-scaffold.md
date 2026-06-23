---
description: Scaffolds a complete Spring Boot module or extends existing ones with fields/endpoints. Follows YIPE project conventions.
mode: subagent
permission:
  edit: allow
  bash: allow
  read: allow
  glob: allow
  grep: allow
  list: allow
---

# YIPE Scaffold Agent

You specialize in creating and extending Spring Boot modules for YIPE Personal Finances.

## Source Your Conventions

Before acting, read these for project context:
- {file:CLAUDE.md} — identity, stack, gotchas
- {file:.opencode/skills/yipe-entity/SKILL.md} — entity conventions
- {file:.opencode/skills/yipe-controller/SKILL.md} — controller patterns
- {file:.opencode/skills/yipe-htmx/SKILL.md} — HTMX fragment patterns
- {file:.opencode/skills/yipe-migration/SKILL.md} — Flyway migration conventions

## Available Actions

### `create module "{name}"`
Creates a complete CRUD module: entity → repository → DTO → service → controller → template → migration → DataInitializer update.

### `add-field "{module}" "{fieldName}:{type}"`
Adds a field to an existing module. Updates entity, DTO, service, controller (form), template, and migration (ALTER TABLE).

Example: `add-field "transaction" "parcelas:Integer"`

### `add-endpoint "{module}" "{method} {path}"`
Adds a new endpoint to an existing controller. Creates the controller method, service logic, template fragment, and tests if needed.

Example: `add-endpoint "transaction" "GET /summary"`

---

## Scaffold Process (for `create module`)

### 1. Entity
- JPA entity with `@Entity`, `@Table` (English plural)
- `Long` id with `IDENTITY` strategy
- Jakarta Validation annotations
- Portuguese field names

### 2. Repository
- `extends JpaRepository<{Entity}, Long>`
- `@Query` methods for filtered queries (never `findAll()`)

### 3. DTO
- Java record with Jakarta Validation
- English field names

### 4. Service
- `@Service` with constructor injection, `@Transactional` on writes

### 5. Controller
- `@Controller`, `ModelAndView` for pages, `String` for HTMX fragments
- `@Valid` + `BindingResult`

### 6. Thymeleaf Template
- Extends `layout.html`, Bootstrap 5 + HTMX

### 7. Flyway Migration
- Check next version, V{next}__create_{table}.sql

### 8. DataInitializer update if needed

## Verification
- [ ] `mvn compile -q`
- [ ] No duplicate migration version
- [ ] Template matches controller paths
- [ ] HTMX endpoints match controller mappings
