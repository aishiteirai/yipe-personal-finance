---
name: yipe-entity
description: JPA entity conventions for YIPE Personal Finances. Portuguese field names, table naming, MapStruct mapping, validation.
---

## YIPE Entity Conventions

### Naming
| Layer | Convention | Example |
|-------|------------|---------|
| Table name (SQL) | English plural | `transacoes`, `categorias`, `contas`, `cartoes`, `salarios` |
| Entity class | Portuguese singular PascalCase | `Transaction` (for `transacoes`) |
| Field names | Portuguese | `nome`, `data`, `valor`, `tipo`, `descricao`, `conta`, `categoria`, `parcela` |
| DTO field names | English (or match entity) | `name`, `date`, `amount`, `type` |

### Standard Entity Structure
```java
@Entity
@Table(name = "transacoes")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    // getters/setters or Lombok @Data
}
```

- `id` is always `Long` with `IDENTITY` auto-generation
- Use Jakarta Validation on entity fields (`@NotBlank`, `@NotNull`, `@Positive`)
- No `@Data` on entities if you need `@EqualsAndHashCode` control — prefer explicit getters/setters

### MapStruct Mapper Pattern
```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionDTO toDto(Transaction entity);
    Transaction toEntity(TransactionDTO dto);
}
```
- Always `componentModel = "spring"`
- Inject mapper via constructor: `private final TransactionMapper mapper;`
- Field name mapping: if DTO field differs from entity, use `@Mapping(target = "nome", source = "name")`

### Common Portuguese Fields
| Entity Field | Type | Description |
|-------------|------|-------------|
| `nome` | String | Name |
| `data` | LocalDate | Date |
| `valor` | BigDecimal | Amount |
| `tipo` | String | Type (DEBIT, CREDIT, etc.) |
| `descricao` | String | Description |
| `conta` | String | Account name |
| `categoria` | String | Category name |
| `parcela` | String | Installment (e.g. "1/12") |
| `diaFechamento` | Integer | Closing day (card) |
| `diaVencimento` | Integer | Due day (card) |
