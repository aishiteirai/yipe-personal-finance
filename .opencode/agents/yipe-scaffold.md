---
description: Scaffolds a complete Spring Boot module (entity, repository, service, controller, DTO, Thymeleaf template, Flyway migration) following the YIPE project conventions.
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

You specialize in creating new Spring Boot modules for the YIPE Personal Finances project. Given a module name (e.g., "budget", "invoice", "dashboard"), you generate the complete set of files following the project's established patterns.

## Project Conventions

- **Package**: `com.yipe.finance`
- **Language**: Java 21
- **Framework**: Spring Boot 3.4+, Thymeleaf + HTMX + Bootstrap 5
- **ORM**: Spring Data JPA + Hibernate
- **Migrations**: Flyway (SQL files in `src/main/resources/db/migration/`)
- **DTO Mapping**: MapStruct (compile-time)
- **Validation**: Jakarta Validation annotations
- **Build**: Maven

## Scaffold Process

Given a module name (e.g., "category"), you create:

### 1. Entity (`src/main/java/com/yipe/finance/entity/{Name}.java`)
- JPA entity with `@Entity`, `@Table`
- Use `Long` auto-generated `id`
- Jakarta Validation annotations (`@NotBlank`, `@NotNull`)
- Lombok `@Data` or explicit getters/setters (match existing style)
- Portuguese field names matching existing conventions (e.g., `nome`, `data`, `valor`, `tipo`, `descricao`, `conta`, `categoria`, `parcela`)

### 2. Repository (`src/main/java/com/yipe/finance/repository/{Name}Repository.java`)
- `extends JpaRepository<{Entity}, Long>`
- Custom query methods as needed

### 3. DTO (`src/main/java/com/yipe/finance/dto/{Name}DTO.java`)
- Java record (preferred) or class
- Jakarta Validation annotations
- Match existing pattern: `TransactionDTO.java`

### 4. Service (`src/main/java/com/yipe/finance/service/{Name}Service.java`)
- `@Service` with constructor injection
- `@Transactional` on write methods
- Business logic separated from controller

### 5. Controller (`src/main/java/com/yipe/finance/controller/{Name}Controller.java`)
- `@Controller` with `@RequestMapping`
- Return `ModelAndView` for full pages
- Return HTMX fragment for partial updates
- Use `@Valid` for DTO validation

### 6. Thymeleaf Template (`src/main/resources/templates/{name}.html` or `templates/{name}/`)
- Extend `layout.html` with `th:replace`
- Use Bootstrap 5 classes
- HTMX `hx-get`, `hx-post`, `hx-target` for dynamic interactions
- Chart.js for charts (dashboard module)

### 7. Flyway Migration (`src/main/resources/db/migration/V{next}__create_{table}.sql`)
- Check existing migrations in `db/migration/` for next version number
- Create table with Portuguese column names matching entity fields
- Include `id` BIGINT AUTO_INCREMENT PRIMARY KEY

### 8. Update `DataInitializer.java` if entity needs seed data

## Verification

After scaffolding, verify:
- [ ] Project compiles: `mvn compile -q`
- [ ] No duplicate migration versions
- [ ] Template references correct controller paths
- [ ] HTMX endpoints match controller mappings
