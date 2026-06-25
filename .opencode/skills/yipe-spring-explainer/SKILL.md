---
name: yipe-spring-explainer
description: Explica conceitos do Spring Boot, arquitetura em camadas e finalidade de cada componente no contexto do projeto YIPE Personal Finances.
---

## Sobre esta skill

Esta skill ensina conceitos de Spring Boot de forma didática. Use quando o usuário perguntar "como funciona...", "qual a finalidade de...", "me explica...", ou quiser entender a arquitetura geral.

---

## 1. O que é Spring Boot?

Spring Boot é um framework Java que **autoconfigura** um projeto Spring. Em vez de configurar manualmente um web server, conexão com banco, segurança etc., você adiciona dependências (starters) e o Spring Boot configura tudo com base nelas.

**Problema que resolve:** Antes do Spring Boot, configurar um projeto Spring exigia dezenas de XMLs, configurações de `web.xml`, `applicationContext.xml`, `persistence.xml` etc. Spring Boot elimina quase toda essa configuração explícita.

**Exemplo no YIPE:** O `pom.xml` declara `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, e o Spring Boot automaticamente:
- Sobe um Tomcat embutido (porta 8080)
- Configura o Hibernate + DataSource (H2 em dev, PostgreSQL em prod)
- Ativa o filtro de segurança do Spring Security
- Disponibiliza o Thymeleaf como template engine

---

## 2. Inversão de Controle (IoC) e Injeção de Dependência (DI)

### IoC (Inversão de Controle)
Em vez de você criar objetos com `new`, o **container Spring** cria e gerencia o ciclo de vida dos objetos (beans). Você só declara as dependências e o container as injeta.

### DI (Injeção de Dependência)
O container entrega as dependências prontas para sua classe. No YIPE, usamos **injeção por construtor**:

```java
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final TransactionService transactionService;

    // Spring injeta automaticamente via construtor
    public DashboardController(DashboardService dashboardService,
                                TransactionService transactionService) {
        this.dashboardService = dashboardService;
        this.transactionService = transactionService;
    }
}
```

**Por que isso importa:** Seu código não acopla com implementações concretas. Fica fácil trocar implementações, mockar em testes, e gerenciar escopos (singleton, request, session).

### Anotações de estereótipo (quem vira bean)

| Anotação | Onde usar | Finalidade |
|----------|-----------|------------|
| `@Component` | Qualquer classe | Bean genérico |
| `@Service` | Service layer | Indica lógica de negócio |
| `@Repository` | Repository layer | Acesso a dados (Spring Data JPA adiciona tratamento de exceções) |
| `@Controller` | Controller layer | MVC — retorna view/template |
| `@RestController` | Controller layer | API REST — retorna JSON/XML |

**No YIPE:** usamos `@Controller` (NUNCA `@RestController`) porque as views são Thymeleaf. Fragmentos HTMX retornam `String` com caminho do template.

---

## 3. Arquitetura em Camadas (Layered Architecture)

### Fluxo de uma requisição no YIPE

```
Browser (Chrome)
    │
    ▼  HTTP Request (GET /dashboard)
Controller Layer  (@Controller)
    │   Valida entrada, chama service, adiciona atributos ao Model
    ▼
Service Layer    (@Service)
    │   Lógica de negócio, cálculos, orquestração
    ▼
Repository Layer (@Repository)
    │   Acesso a dados (Spring Data JPA)
    ▼
Database (H2 / PostgreSQL)
    │
    ▼  Retorna dados
Repository → Service → Controller
    │
    ▼  Retorna view (Thymeleaf template)
Controller renderiza HTML com dados
    │
    ▼  HTTP Response (HTML)
Browser renderiza a página
```

### Responsabilidade de cada camada

| Camada | Responsabilidade | O que NÃO fazer |
|--------|------------------|-----------------|
| **Controller** | Receber request, validar (`@Valid`), chamar service, retornar view/fragment | Nunca colocar lógica de negócio ou acesso a banco |
| **Service** | Regras de negócio, cálculos, `@Transactional`, orquestrar repositórios | Nunca receber/acessar `HttpServletRequest`, `HttpSession` |
| **Repository** | Consultas SQL/JPQL, `@Query`, Spring Data métodos derivados | Nunca ter lógica de negócio |
| **Entity** | Mapeamento O/R, validação de campos, relações JPA | Nunca expor para a view (use DTOs) |

### Exemplo concreto no YIPE (fluxo do Dashboard)

1. **Browser** faz GET `/dashboard?ano=2026&mes=6`
2. **DashboardController** recebe parâmetros, chama `DashboardService`
3. **DashboardService** chama `TransactionRepository.findByYearAndTypes()` e faz cálculos (saldo geral, gasto crédito hoje, etc.)
4. **DashboardController** coloca resultados no `Model` e retorna `"dashboard"` (template Thymeleaf)
5. **Thymeleaf** renderiza o HTML com gráficos Chart.js, tabelas, cards
6. **Browser** exibe a página

---

## 4. Spring Data JPA + Hibernate

### ORM (Object-Relational Mapping)
Mapeia tabelas do banco para objetos Java. Você trabalha com objetos, não com SQL (embora possa escrever SQL quando precisar).

### Entity (exemplo do YIPE)

```java
@Entity
@Table(name = "transacoes")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data", nullable = false)
    private LocalDate date;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    // getters/setters — omitidos
}
```

**Convenção YIPE:** campos da entidade em português (`data`, `valor`, `tipo`), getters/setters e DTOs em inglês (`getDate()`, `getAmount()`, `getType()`).

### Repository

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Spring Data deriva a query do nome do método
    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

    // Query customizada JPQL
    @Query("SELECT DISTINCT YEAR(t.date) FROM Transaction t ORDER BY YEAR(t.date)")
    List<Integer> findDistinctYears();
}
```

**Como funciona:** O Spring Data JPA implementa a interface em runtime. Você só declara os métodos — não precisa escrever implementação.

### Por que separar Entity de DTO?

- **Entity** reflete a estrutura do banco (nomes PT-BR, relações JPA, lazy loading)
- **DTO** reflete o que a view precisa (nomes EN, apenas campos necessários, sem lazy loading)
- **Mapper (MapStruct)** converte um no outro em compile-time, sem reflection em runtime

---

## 5. Spring Security

### O que faz
Gerencia autenticação (quem é você) e autorização (o que você pode fazer).

### Configuração YIPE

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
            )
            .logout(logout -> logout.logoutSuccessUrl("/login"));
        return http.build();
    }
}
```

### Fluxo de login
1. Usuário acessa `/dashboard` → não autenticado → Spring redireciona para `/login`
2. Usuário preenche form → POST `/login` com `username` + `password`
3. Spring verifica credenciais → se OK, cria sessão e redireciona para `/dashboard`
4. Requests seguintes incluem cookie de sessão → Spring autentica automaticamente

### CSRF (Cross-Site Request Forgery)
- Spring Security ativa CSRF por padrão
- Todo form POST/PUT/DELETE precisa enviar um token CSRF
- No Thymeleaf, `th:action` no `<form>` adiciona o token automaticamente

---

## 6. Thymeleaf + HTMX

### Thymeleaf (server-side template engine)
Processa templates HTML no servidor, substitui atributos `th:*` antes de enviar ao browser.

**Exemplo YIPE:**
```html
<div th:each="transacao : ${transactions}">
    <span th:text="${transacao.description}">descrição</span>
    <span th:text="${#numbers.formatDecimal(transacao.amount, 0, 2)}">0,00</span>
</div>
```

### Layout Dialect
Reutiliza layout comum (sidebar, header) em todas as páginas:

```html
<!-- layout.html define a estrutura -->
<html layout:decorate="~{layout}">
<body>
    <section layout:fragment="content">
        <!-- conteúdo específico da página -->
    </section>
</body>
</html>
```

### HTMX (dinamismo sem JavaScript)
Faz requisições AJAX declarativamente via atributos HTML. O servidor retorna HTML (fragmento), não JSON.

**No YIPE:**
```html
<!-- Mudou o filtro → recarrega só a tabela, não a página inteira -->
<select name="mes" hx-get="/statement/table" hx-target="#table-body" hx-trigger="change">
```

**Por que Thymeleaf + HTMX em vez de React/Vue?**
- Sem build tool (webpack/vite) — tudo server-side
- Lógica de negócio fica em Java, não duplicada em JS
- Ideal para 1 desenvolvedor mantendo o projeto
- HTMX 2.x é 14KB minified — não precisa de SPA

---

## 7. MapStruct

### O que é
Gerador de código de mapeamento em compile-time. Converte Entity ↔ DTO sem reflection.

```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // Spring gera a implementação em compile-time
    TransactionDTO toDto(Transaction transaction);
    Transaction toEntity(TransactionDTO dto);
}
```

**Por que não manual ou ModelMapper?**
- **Manual:** boilerplate repetitivo, propenso a erros
- **ModelMapper:** reflection em runtime, lento, difícil de debugar
- **MapStruct:** código gerado em compile-time, zero overhead, tipos verificados

---

## 8. Flyway

### O que é
Versionamento de schema do banco. Cada migração é um arquivo SQL numerado que só roda uma vez.

```
db/migration/
├── V1__create_initial_schema.sql    -- Cria tabelas
├── V2__seed_default_data.sql        -- Dados padrão (categorias, contas)
├── V3__seed_test_data.sql           -- Dados de teste (transações)
```

**Regra de ouro:** Nunca modifique uma migração existente. Crie V{proximo_numero} para alterações.

---

## 9. Mapa Visual da Arquitetura YIPE

```
┌─────────────────────────────────────────────────────────┐
│                     Browser                             │
│  Thymeleaf (HTML renderizado) + HTMX (AJAX declarativo) │
│  Bootstrap 5 (CSS) + Chart.js (gráficos)                │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────────────┐
│              Spring Boot (Tomcat embutido)              │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ @Controller  │  │  @Service    │  │ @Repository  │  │
│  │ valida +     │──► lógica de    │──► acesso a     │  │
│  │ retorna view │  │ negócio      │  │ dados (JPA)  │  │
│  └──────────────┘  └──────────────┘  └──────┬───────┘  │
│                                              │          │
│  ┌──────────────┐  ┌──────────────┐         │          │
│  │ @Entity      │  │ MapStruct    │         │          │
│  │ mapeia tabela│  │ Entity ↔ DTO │         │          │
│  └──────────────┘  └──────────────┘         │          │
│                                              │          │
│  ┌──────────────┐  ┌──────────────┐         │          │
│  │ Security     │  │ Flyway       │         │          │
│  │ login + CSRF │  │ migrations   │         │          │
│  └──────────────┘  └──────────────┘         │          │
└──────────────────────────────────────────────┼──────────┘
                                               │ JDBC
                    ┌──────────────────────────▼─────────┐
                    │  H2 (dev) / PostgreSQL (prod)       │
                    │  ┌────────────────────────────────┐ │
                    │  │ transacoes │ categorias        │ │
                    │  │ contas     │ cartoes           │ │
                    │  │ salarios   │ flyway_schema_history │
                    │  └────────────────────────────────┘ │
                    └────────────────────────────────────┘
```

### Dependência entre camadas

```
Controller → Service → Repository → Entity → Database
     │           │           │
     ▼           ▼           ▼
    DTOs       DTOs        JPA/Hibernate
```

**Regra:** Controller depende de Service + DTOs. Service depende de Repository + DTOs. Repository depende de Entity. Controller **NUNCA** acessa Repository ou Entity diretamente.

---

## 10. Glossário Rápido

| Termo | Significado |
|-------|-------------|
| **Bean** | Objeto gerenciado pelo container Spring |
| **IoC** | Container controla ciclo de vida, não você |
| **DI** | Container injeta dependências prontas |
| **AOP** | Programação orientada a aspectos (usado em `@Transactional`, `@Secured`) |
| **JPA** | Spec Java para ORM |
| **Hibernate** | Implementação mais popular do JPA |
| **JPQL** | SQL orientado a objetos (consulta entidades, não tabelas) |
| **ORM** | Mapeamento objeto-relacional (tabela ↔ classe) |
| **SSR** | Server-Side Rendering (HTML gerado no servidor) |
| **CSRF** | Token que previne falsificação de requisição entre sites |
| **BCrypt** | Algoritmo de hash para senhas (lento de propósito) |
| **Flyway** | Versionamento de schema do banco |
| **MapStruct** | Gerador de mapeamento Entity↔DTO em compile-time |

---

## Como usar esta skill

Pergunte no chat qualquer coisa como:

- "Me explica como funciona o fluxo de uma requisição no Spring"
- "Qual a finalidade do @Service?"
- "Por que usar DTO em vez de Entity na view?"
- "Como o Spring Security protege o login?"
- "Diferença entre @Controller e @RestController"
- "O que é injeção de dependência?"
- "Me mostra o mapa da arquitetura do YIPE novamente"
