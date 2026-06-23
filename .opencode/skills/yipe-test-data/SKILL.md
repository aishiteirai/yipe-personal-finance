---
name: yipe-test-data
description: Test data seeding patterns for YIPE Personal Finances. V3 migration, DataInitializer conventions, mock transaction patterns.
---

## YIPE Test Data Conventions

### DataInitializer Pattern
`src/main/java/com/yipe/finance/config/DataInitializer.java`

```java
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    // ... other repos

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;
        // seed default data
    }
}
```
- `@Profile("dev")` — only runs in dev profile
- Guard with `count() > 0` check to avoid duplicate seeding
- `@Transactional` for batch inserts

### Category Seed Data
```java
List.of("Alimentação", "Transporte", "Moradia", "Saúde",
        "Educação", "Lazer", "Vestuário", "Assinaturas",
        "Impostos", "Investimentos", "Salário", "Reserva",
        "VR")
```

### Account Seed Data
```java
List.of("Nubank", "Inter", "C6", "Itaú", "VR")
```

### Card Seed Data
```java
// name, bank, closingDay, dueDay
new Card("Nubank", "Nubank", 3, 8),
new Card("Inter", "Inter", 15, 22),
new Card("C6", "C6", 20, 28)
```

### Salary Seed Data
```java
// name, day, amount, account
new Salary("Salário", 5, new BigDecimal("5000.00"), "Nubank")
```

### Transaction Test Data Patterns
- Use multiple months for cross-period testing (e.g., Jan-Jun 2026)
- Include all types: DEBIT, CREDIT, VR, INVESTMENT, RESERVE, INCOME
- Include installment transactions (e.g., "1/12", "2/12")
- Include recurring transactions ("Recurring")
- Vary amounts for realistic aggregates

### Relative Dates (Avoid Hardcoded Years)
```java
LocalDate.now().withMonth(1).withDayOfMonth(15)
// instead of
LocalDate.of(2026, 1, 15)
```
