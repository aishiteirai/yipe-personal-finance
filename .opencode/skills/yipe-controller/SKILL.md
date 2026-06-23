---
name: yipe-controller
description: Controller conventions for YIPE Personal Finances. ModelAndView for pages, String for HTMX fragments, @Valid + BindingResult, error handling.
---

## YIPE Controller Conventions

### Controller Types
| Return Type | Use Case |
|-------------|----------|
| `ModelAndView` | Full page (initial load, redirect after form submit) |
| `String` | HTMX fragment (template fragment path, e.g. `"transactions/fragments :: table"`) |

### Always use `@Controller`, never `@RestController`
```java
@Controller
@RequestMapping("/transactions")
public class TransactionController {
    @GetMapping
    public ModelAndView list() { ... }

    @GetMapping("/table")
    public String tableFragment() { ... }
}
```

### Full Page Pattern
```java
@GetMapping
public ModelAndView list(@RequestParam(defaultValue = "2026") int year) {
    List<TransactionDTO> items = service.findAll(year);
    return new ModelAndView("transactions/list", "transactions", items);
}
```

### HTMX Fragment Pattern
```java
@GetMapping("/table")
public String tableFragment(@RequestParam int year, Model model) {
    model.addAttribute("transactions", service.findAll(year));
    return "transactions/fragments :: table";
}
```
- Method returns `String` (template fragment path), NOT `@ResponseBody`
- Data goes into `Model` or `ModelAndView`

### Form Validation Pattern
```java
@PostMapping
public String save(@Valid @ModelAttribute TransactionDTO dto,
                   BindingResult result, Model model) {
    if (result.hasErrors()) {
        model.addAttribute("dto", dto);
        return "transactions/fragments :: form";
    }
    service.create(dto);
    return "redirect:/transactions";
}
```
- `@Valid` on DTO with `BindingResult` immediately after
- On error: re-render fragment with validation errors (Thymeleaf `th:errors`)
- On success: redirect or return updated fragment

### Error Handling
- `GlobalExceptionHandler` catches `ResourceNotFoundException` → 404 page
- Unhandled exceptions → `error.html` with stack trace in dev mode
- Form validation errors shown inline via `th:errors` and validation summary
