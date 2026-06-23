---
name: yipe-migration
description: Flyway migration conventions for YIPE Personal Finances. Version numbering, naming patterns, SQL conventions, transaction safety.
---

## YIPE Flyway Migration Conventions

### Version Numbering
- Files in `src/main/resources/db/migration/` — format: `V{version}__{description}.sql`
- Always check existing migrations for the next version number
- Never modify existing migrations — create a new V{next} file
- Current migrations: V1 (schema), V2 (seed), V3 (test data)

### Naming Convention
```
V1__create_initial_schema.sql    — table creation
V2__seed_default_data.sql         — default seed data
V3__insert_test_data.sql          — development test data
```

### SQL Conventions
```sql
CREATE TABLE transacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    categoria VARCHAR(100),
    conta VARCHAR(100),
    descricao VARCHAR(500),
    parcela VARCHAR(20)
);
```
- Table names: English plural (`transacoes`, `categorias`, `contas`, `cartoes`, `salarios`)
- Column names: Portuguese (match entity field names)
- `id` always `BIGINT AUTO_INCREMENT PRIMARY KEY`
- Use `DECIMAL(12,2)` for monetary values
- Use `DATE` for dates, `VARCHAR` for strings

### Seed Data Pattern
```sql
INSERT INTO categorias (nome) VALUES
    ('Alimentação'),
    ('Transporte'),
    ('Moradia');
```

### Safety Rules
- Wrap multi-statement migrations in a transaction (Flyway does this by default for each file)
- Test migrations against H2 in dev before deploying to PostgreSQL
- Never use `DROP` in a migration unless absolutely necessary
- Prefer `ALTER TABLE` over destructive changes
